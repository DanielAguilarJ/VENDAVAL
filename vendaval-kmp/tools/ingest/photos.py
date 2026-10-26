#!/usr/bin/env python3
"""
Resolve a licence-clean photograph for each venue in SeedCatalogue.kt.

Runs OUTSIDE the app, on demand, and writes a reviewable manifest. It does not
run on a user's device, for two reasons: the app would break in the field
whenever an upstream response shape changed, and every install would multiply
the load we put on Wikimedia.

The output is deliberately a *proposal*, not a fact. A wrong photo here is worse
than no photo: it asserts "this is what that building looks like" to someone who
may be trying to find it. So candidates must clear `judge()` before they are
written, and anything that fails falls back to the app's generated letter cover.

Sources, in order of trust:
  1. Wikidata P18 -- an editor explicitly bound that image to that place.
  2. Commons file-name search -- inference; needs the guard to be worth anything.

Usage:
    python3 tools/ingest/photos.py            # write the manifest
    python3 tools/ingest/photos.py --dry-run  # print decisions, write nothing
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import subprocess
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass, asdict
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
SEED = REPO / "shared/src/commonMain/kotlin/mx/vendaval/core/data/seed/SeedCatalogue.kt"
OUT = REPO / "shared/src/commonMain/kotlin/mx/vendaval/core/data/seed/VenuePhotoSeed.kt"
ASSETS = REPO / "composeApp/src/commonMain/composeResources/files/venues"
CACHE = Path("/tmp/vendaval-ingest-cache")
MAX_RETRIES = 6

# Longest edge of the stored image. The largest thing the UI draws is the detail
# hero at roughly 720 px on a 3x phone; the 64 dp card cover needs a fraction of
# that. Storing more would be paid for twice — once in the binary and again in
# memory, since `decodeImageBytes` cannot downsample.
ASSET_WIDTH = 720
ASSET_QUALITY = 72

USER_AGENT = "VendavalKMP/1.0 (Colectivo Vendaval; cartografia cultural Aguascalientes)"
COMMONS_API = "https://commons.wikimedia.org/w/api.php"
WIKIDATA_SPARQL = "https://query.wikidata.org/sparql"

# Rendered width we ask Commons for. The app's decodeImageBytes() has no
# downsampling on any platform, so a full-resolution 4000x3000 original would be
# decoded whole into memory -- an OOM on a mid-range phone, not a slow frame.
# Asking for a thumbnail moves that problem upstream where it costs nothing.
THUMB_WIDTH = 900

# Only these may ship. Anything else -- unknown, "fair use", all-rights-reserved --
# is dropped rather than guessed about.
ALLOWED_LICENCE = re.compile(r"\b(cc[ -]?(by|by-sa|0|zero)|public domain|pd|cc-by)", re.I)

# Words too common in this domain to identify anything. "Casa de la Cultura de
# Calvillo" is distinguished by "calvillo", not by "casa".
GENERIC = {
    "casa", "cultura", "cultural", "centro", "museo", "teatro", "biblioteca",
    "galeria", "arte", "artes", "espacio", "instituto", "sala", "publica",
    "publico", "central", "de", "del", "la", "el", "los", "las", "y", "en",
    "aguascalientes", "ags", "mexico", "municipal", "estatal", "nacional",
}

# The kind-of-thing word. A photograph of a cultural centre should say so; this
# is what rejects a portrait that merely shares a personal name with the venue
# ("Maria de Jesus Padilla Romo.jpg" for "Casa de la Cultura Jesus Maria").
HEAD_NOUNS = ("teatro", "museo", "biblioteca", "casa", "centro", "galeria", "instituto", "sala")

# Subjects that are demonstrably not the building, seen in real results.
BLACKLIST = (
    "director", "directora", "retrato", "portrait", "presidente", "gobernador",
    "alcalde", "busto", "firma", "acta", "oficio", "logo", "escudo", "libro",
    "republica mexicana", "tomo", "pagina", "mapa de", "diploma", "cartel",
)

# Prefers a picture *of* the place over a picture taken *at* the place.
FRAMING_BONUS = ("fachada", "vista", "exterior", "edificio", "patio", "entrada", "frente", "interior")

# Markers that the subject is something happening at the venue -- a play, an
# exhibition, a poster -- rather than the venue. Not fatal on their own: a photo
# captioned "Galeria Benjamin Manzo durante el XLII ENAJ" really is the gallery.
# What separates the two is whether the title *leads* with the place.
EVENT_MARKERS = (
    " en el ", " en la ", " durante ", "concierto", "recital", "funcion",
    "obra de", "kermes", "exposicion", "muestra de", "festival", "presentacion",
)
LEAD_TOKENS = 3

IMAGE_EXT = (".jpg", ".jpeg", ".png", ".webp", ".tif", ".tiff")


def fold(text: str) -> str:
    """Lowercase, strip accents, collapse to single-spaced alphanumerics."""
    decomposed = unicodedata.normalize("NFD", text.lower())
    stripped = "".join(c for c in decomposed if unicodedata.category(c) != "Mn")
    return re.sub(r"[^a-z0-9]+", " ", stripped).strip()


def http_json(url: str, params: dict[str, str]) -> dict:
    """GET JSON, cached on disk, retrying through rate limits.

    Wikimedia rate-limits hard and WDQS periodically drops to 1 request/minute
    during an outage. Without the cache a re-run re-earns every 429 from
    scratch, and a partial run silently looks like "no photo exists" -- which is
    exactly the failure this whole script exists to avoid. So: persist every
    successful response, and treat 429/503 as "wait", never as "absent".
    """
    query = urllib.parse.urlencode(params)
    full = f"{url}?{query}"

    CACHE.mkdir(parents=True, exist_ok=True)
    key = hashlib.sha256(full.encode("utf-8")).hexdigest()[:32]
    cached = CACHE / f"{key}.json"
    if cached.exists():
        return json.loads(cached.read_text(encoding="utf-8"))

    request = urllib.request.Request(
        full, headers={"User-Agent": USER_AGENT, "Accept": "application/json"}
    )
    delay = 5.0
    for attempt in range(MAX_RETRIES):
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                payload = json.loads(response.read().decode("utf-8"))
            cached.write_text(json.dumps(payload), encoding="utf-8")
            return payload
        except urllib.error.HTTPError as exc:
            if exc.code not in (429, 503) or attempt == MAX_RETRIES - 1:
                raise
            wait = float(exc.headers.get("Retry-After") or delay)
            print(f"    rate-limited ({exc.code}), waiting {wait:.0f}s "
                  f"[{attempt + 1}/{MAX_RETRIES}]", file=sys.stderr)
            time.sleep(wait)
            delay = min(delay * 2, 90)
    raise RuntimeError("unreachable")


# --------------------------------------------------------------------------- #
# Seed parsing
# --------------------------------------------------------------------------- #

@dataclass(frozen=True)
class Venue:
    id: int
    name: str
    lat: float
    lng: float


SEED_ENTRY = re.compile(
    r"id\s*=\s*(?P<id>\d+),.*?"
    r'name\s*=\s*"(?P<name>(?:[^"\\]|\\.)*)",.*?'
    r"lat\s*=\s*(?P<lat>-?\d+\.\d+),\s*"
    r"lng\s*=\s*(?P<lng>-?\d+\.\d+)",
    re.S,
)


def read_seed() -> list[Venue]:
    source = SEED.read_text(encoding="utf-8")
    venues = [
        Venue(
            id=int(m.group("id")),
            name=m.group("name").replace('\\"', '"').replace("\\\\", "\\").strip(),
            lat=float(m.group("lat")),
            lng=float(m.group("lng")),
        )
        for m in SEED_ENTRY.finditer(source)
    ]
    if not venues:
        raise SystemExit(f"parsed no venues out of {SEED} -- the seed shape changed")
    return venues


# --------------------------------------------------------------------------- #
# The guard
# --------------------------------------------------------------------------- #

@dataclass
class Verdict:
    accepted: bool
    score: int
    reason: str


def distinctive_tokens(name: str) -> list[str]:
    return [t for t in fold(name).split() if t not in GENERIC and len(t) >= 4]


def judge(venue_name: str, file_title: str) -> Verdict:
    """Decide whether `file_title` plausibly depicts `venue_name`.

    Errs towards rejection. The cost of a false negative is a letter placeholder;
    the cost of a false positive is the app lying about a real place.
    """
    title = fold(file_title)
    name = fold(venue_name)

    if not file_title.lower().endswith(IMAGE_EXT):
        return Verdict(False, 0, "not an image file")

    for term in BLACKLIST:
        if term in title:
            return Verdict(False, 0, f"subject looks like '{term}', not the building")

    if not any(noun in title for noun in HEAD_NOUNS):
        return Verdict(False, 0, "title names no kind of venue")

    tokens = distinctive_tokens(venue_name)
    if tokens:
        missing = [t for t in tokens if t not in title]
        if missing:
            return Verdict(False, 0, f"missing identifying token(s): {', '.join(missing)}")
    elif name not in title:
        # Nothing distinctive to match on (e.g. "Centro de Artes Visuales"),
        # so demand the whole name verbatim.
        return Verdict(False, 0, "generic name absent verbatim")

    # An event marker is only acceptable when the title still leads with the
    # place. "Galeria Benjamin Manzo durante el XLII ENAJ" is the gallery;
    # "Lisistrata en el teatro Antonio Leal y Romero" is a play, and
    # "Gran Kermes ... en Hogar Espejo" is a poster for something else entirely.
    padded = f" {title} "
    if any(marker in padded for marker in EVENT_MARKERS):
        lead = " ".join(title.split()[:LEAD_TOKENS])
        leads_with_place = any(n in lead for n in HEAD_NOUNS) or \
            any(hint in lead for hint in FRAMING_BONUS)
        if not leads_with_place:
            return Verdict(False, 0, "depicts an event at the venue, not the venue")

    score = 1
    if name in title:
        score += 3
    score += sum(2 for hint in FRAMING_BONUS if hint in title)
    return Verdict(True, score, "ok")


# --------------------------------------------------------------------------- #
# Sources
# --------------------------------------------------------------------------- #

_WIKIDATA_INDEX: list[tuple[str, str]] | None = None

# Aguascalientes state, generously boxed. One query for the whole catalogue:
# 27 per-venue `wikibase:around` calls is 27 chances to be rate-limited, and
# WDQS drops to 1 req/min during an outage, which made a full run take half an
# hour and look like a coverage failure.
STATE_BOX = ("Point(-102.95 21.55)", "Point(-101.75 22.55)")


def wikidata_index() -> list[tuple[str, str]]:
    """Every geolocated Wikidata item in the state that has a P18 image.

    Returns (folded label, Commons file title) pairs, fetched once.
    """
    global _WIKIDATA_INDEX
    if _WIKIDATA_INDEX is not None:
        return _WIKIDATA_INDEX

    sparql = f"""
    SELECT DISTINCT ?itemLabel ?image WHERE {{
      SERVICE wikibase:box {{
        ?item wdt:P625 ?coord .
        bd:serviceParam wikibase:cornerSouthWest "{STATE_BOX[0]}"^^geo:wktLiteral .
        bd:serviceParam wikibase:cornerNorthEast "{STATE_BOX[1]}"^^geo:wktLiteral .
      }}
      ?item wdt:P18 ?image .
      SERVICE wikibase:label {{ bd:serviceParam wikibase:language "es,en". }}
    }} LIMIT 1500
    """
    try:
        data = http_json(WIKIDATA_SPARQL, {"query": sparql, "format": "json"})
    except Exception as exc:
        print(f"  wikidata index unavailable ({exc}); falling back to Commons only",
              file=sys.stderr)
        _WIKIDATA_INDEX = []
        return _WIKIDATA_INDEX

    index = []
    for row in data.get("results", {}).get("bindings", []):
        filename = urllib.parse.unquote(row["image"]["value"].rsplit("/", 1)[1])
        index.append((fold(row["itemLabel"]["value"]), f"File:{filename}"))
    print(f"  wikidata: {len(index)} geolocated items with a photo in the state box")
    _WIKIDATA_INDEX = index
    return index


def wikidata_candidates(venue: Venue) -> list[str]:
    """P18 images whose Wikidata label matches this venue by name.

    Name match only -- deliberately. Proximity alone would happily attach the
    cathedral's photograph to the library across the street.
    """
    wanted = fold(venue.name)
    return [
        title for label, title in wikidata_index()
        if label == wanted or (len(label) > 8 and (label in wanted or wanted in label))
    ]


def commons_candidates(venue: Venue, limit: int = 8) -> list[str]:
    try:
        data = http_json(COMMONS_API, {
            "action": "query", "format": "json", "list": "search",
            "srnamespace": "6", "srlimit": str(limit),
            "srsearch": f"{venue.name} Aguascalientes",
        })
    except Exception as exc:
        print(f"    commons search failed: {exc}", file=sys.stderr)
        return []
    return [hit["title"] for hit in data.get("query", {}).get("search", [])]


def file_metadata(title: str) -> dict | None:
    """Resolve a Commons file title to a thumbnail URL plus its licence + author."""
    try:
        data = http_json(COMMONS_API, {
            "action": "query", "format": "json", "titles": title,
            "prop": "imageinfo",
            "iiprop": "url|size|extmetadata|mime",
            "iiurlwidth": str(THUMB_WIDTH),
        })
    except Exception as exc:
        print(f"    imageinfo failed for {title}: {exc}", file=sys.stderr)
        return None

    pages = data.get("query", {}).get("pages", {})
    for page in pages.values():
        info = (page.get("imageinfo") or [{}])[0]
        if not info:
            continue
        if not str(info.get("mime", "")).startswith("image/"):
            return None
        meta = info.get("extmetadata", {})
        licence = meta.get("LicenseShortName", {}).get("value", "")
        if not ALLOWED_LICENCE.search(licence):
            return None
        artist = re.sub(r"<[^>]+>", "", meta.get("Artist", {}).get("value", "")).strip()
        # The API appends utm_* analytics params to the thumbnail URL; the app has
        # no business sending those on every card that scrolls into view.
        thumb = (info.get("thumburl") or info.get("url") or "").split("?", 1)[0]
        return {
            "title": title,
            "thumbUrl": thumb,
            "width": info.get("thumbwidth") or info.get("width"),
            "height": info.get("thumbheight") or info.get("height"),
            "licence": licence,
            "artist": artist or "Wikimedia Commons",
            "sourceUrl": info.get("descriptionurl", ""),
        }
    return None


# --------------------------------------------------------------------------- #
# Resolution
# --------------------------------------------------------------------------- #

def resolve(venue: Venue, verbose: bool) -> dict | None:
    seen: set[str] = set()
    scored: list[tuple[int, str, str]] = []

    for source, titles in (("wikidata", wikidata_candidates(venue)),
                           ("commons", commons_candidates(venue))):
        for title in titles:
            if title in seen:
                continue
            seen.add(title)
            verdict = judge(venue.name, title[5:])  # drop the "File:" prefix
            if verbose:
                mark = "OK  " if verdict.accepted else "drop"
                print(f"    {mark} [{source}] {title[5:][:64]}  -- {verdict.reason}")
            if verdict.accepted:
                # Wikidata's explicit binding outranks an inferred name match.
                bonus = 4 if source == "wikidata" else 0
                scored.append((verdict.score + bonus, source, title))

    for _, source, title in sorted(scored, key=lambda t: -t[0]):
        meta = file_metadata(title)
        if meta:
            meta["spaceId"] = venue.id
            meta["venueName"] = venue.name
            meta["source"] = source
            return meta
        if verbose:
            print(f"    drop [{source}] {title[5:][:64]}  -- licence not redistributable")
        time.sleep(0.1)
    return None


def download_asset(photo: dict) -> tuple[str, int, int] | None:
    """Fetch the chosen photo, downscale it, and write it into the app's resources.

    The app is offline-first — its own status line says "showing saved data" — so
    fetching photos over the network at display time fails exactly the users the
    app is for. Wikimedia also rate-limits: a cold start asking for thirteen
    thumbnails at once earns HTTP 429 on more than half of them, and because the
    shared HTTP client does not treat a non-2xx as an error, the app decodes the
    error page, gets null, and renders "this place has no photo". A wrong absence
    that looks exactly like a right one.

    So the bytes ship in the binary. Downscaled to ASSET_WIDTH because that is the
    largest size the UI actually draws, and because `decodeImageBytes` has no
    downsampling on any platform: whatever is stored here is what a device holds
    in memory, full size.

    Returns (relative resource path, width, height), or None if anything failed.
    """
    ASSETS.mkdir(parents=True, exist_ok=True)
    name = f"{photo['spaceId']}.jpg"
    target = ASSETS / name

    request = urllib.request.Request(
        photo["thumbUrl"], headers={"User-Agent": USER_AGENT}
    )
    raw = CACHE / f"raw-{name}"

    # Same backoff as http_json, and for the same reason: a 429 here silently
    # becomes "this venue has no photograph", which is a lie the UI cannot detect.
    # urllib raises HTTPError for 4xx, so the status check below never sees a 429 —
    # it has to be caught.
    if not raw.exists():
        delay = 5.0
        for attempt in range(MAX_RETRIES):
            try:
                with urllib.request.urlopen(request, timeout=90) as response:
                    if response.status != 200:
                        print(f"    download failed: HTTP {response.status}", file=sys.stderr)
                        return None
                    raw.write_bytes(response.read())
                break
            except urllib.error.HTTPError as exc:
                if exc.code not in (429, 503) or attempt == MAX_RETRIES - 1:
                    print(f"    download failed: HTTP {exc.code}", file=sys.stderr)
                    return None
                wait = float(exc.headers.get("Retry-After") or delay)
                print(f"    image rate-limited ({exc.code}), waiting {wait:.0f}s "
                      f"[{attempt + 1}/{MAX_RETRIES}]", file=sys.stderr)
                time.sleep(wait)
                delay = min(delay * 2, 90)
            except Exception as exc:
                print(f"    download failed: {exc}", file=sys.stderr)
                return None
        else:
            return None

    # sips ships with macOS, so the ingestor needs no image library installed.
    # -Z fits the longest edge, preserving aspect ratio.
    resize = subprocess.run(
        ["sips", "-Z", str(ASSET_WIDTH), "-s", "format", "jpeg",
         "-s", "formatOptions", str(ASSET_QUALITY), str(raw), "--out", str(target)],
        capture_output=True, text=True,
    )
    if resize.returncode != 0 or not target.exists():
        print(f"    resize failed: {resize.stderr.strip()}", file=sys.stderr)
        return None

    probe = subprocess.run(
        ["sips", "-g", "pixelWidth", "-g", "pixelHeight", str(target)],
        capture_output=True, text=True,
    )
    dimensions = dict(
        re.findall(r"(pixelWidth|pixelHeight):\s*(\d+)", probe.stdout)
    )
    width = int(dimensions.get("pixelWidth", 0))
    height = int(dimensions.get("pixelHeight", 0))
    if width == 0 or height == 0:
        print("    could not read dimensions back", file=sys.stderr)
        return None

    return f"files/venues/{name}", width, height


def kotlin_string(value: str) -> str:
    """Escape a Python string for a Kotlin double-quoted literal."""
    return (value.replace("\\", "\\\\")
                 .replace('"', '\\"')
                 .replace("$", "\\$")
                 .replace("\n", " ")
                 .strip())


def emit_kotlin(photos: list[dict], total: int) -> str:
    pct = 100 * len(photos) // total if total else 0
    lines = [
        "package mx.vendaval.core.data.seed",
        "",
        "import mx.vendaval.core.domain.model.SpaceId",
        "import mx.vendaval.core.domain.model.VenuePhoto",
        "",
        "/**",
        " * GENERATED FILE — do not edit by hand.",
        " *",
        " * Regenerate with `python3 tools/ingest/photos.py`. Last run resolved",
        f" * {len(photos)} of {total} spaces ({pct}%); the rest have no freely-licensed",
        " * photograph and keep their generated letter cover.",
        " *",
        " * Every entry cleared the relevance guard in the ingestor: the file title must",
        " * name the kind of venue, carry all of the venue's distinguishing words, and not",
        " * describe an event held there. That guard exists because the naive first result",
        " * for real venues in this catalogue included a civil servant's portrait, a poster",
        " * for an unrelated event, and a page from an 1846 book — each of which would have",
        " * been published as 'this is what that building looks like'.",
        " *",
        " * The images themselves live in `composeResources/files/venues/`, downscaled at",
        f" * ingest time to {ASSET_WIDTH} px. They ship in the binary rather than being",
        " * fetched at display time: the app is offline-first, and Wikimedia rate-limits a",
        " * burst of requests with a 429 whose HTML body decodes to null — indistinguishable",
        " * from a venue that genuinely has no photograph.",
        " *",
        " * Licences are CC BY-SA only. The artist and licence must be shown wherever the",
        " * image is, which is why they travel inside VenuePhoto.",
        " */",
        "object VenuePhotoSeed {",
        "",
        "    val photos: Map<SpaceId, VenuePhoto> = listOf(",
    ]
    for p in photos:
        lines += [
            "        VenuePhoto(",
            f"            spaceId = SpaceId({p['spaceId']}L),",
            f"            assetPath = \"{kotlin_string(p['assetPath'])}\",",
            f"            width = {p['assetWidth']},",
            f"            height = {p['assetHeight']},",
            f"            licence = \"{kotlin_string(p['licence'])}\",",
            f"            artist = \"{kotlin_string(p['artist'])}\",",
            f"            sourceUrl = \"{kotlin_string(p['sourceUrl'])}\",",
            f"        ), // {kotlin_string(p['venueName'])}",
        ]
    lines += [
        "    ).associateBy { it.spaceId }",
        "",
        "    fun forSpace(id: SpaceId): VenuePhoto? = photos[id]",
        "}",
        "",
    ]
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true", help="print decisions, write nothing")
    parser.add_argument("--quiet", action="store_true", help="only print the summary")
    args = parser.parse_args()

    venues = read_seed()
    print(f"{len(venues)} venues in the seed catalogue\n")

    photos: list[dict] = []
    for venue in venues:
        if not args.quiet:
            print(f"  {venue.name}")
        found = resolve(venue, verbose=not args.quiet)
        if found and not args.dry_run:
            asset = download_asset(found)
            if asset is None:
                found = None          # no bytes on disk means no photo, full stop
            else:
                found["assetPath"], found["assetWidth"], found["assetHeight"] = asset
        if found:
            photos.append(found)
            if args.quiet:
                print(f"  FOTO {venue.name[:42]:<42} {found['licence']}")
        elif args.quiet:
            print(f"  ---- {venue.name[:42]:<42} (letter cover)")
        time.sleep(0.4)  # be a polite Wikimedia client

    pct = 100 * len(photos) // len(venues)
    print(f"\nresolved {len(photos)}/{len(venues)} ({pct}%) -- "
          f"{len(venues) - len(photos)} fall back to the generated letter cover")

    if args.dry_run:
        print("\n--dry-run: nothing written")
        return 0

    photos.sort(key=lambda p: p["spaceId"])
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(emit_kotlin(photos, len(venues)), encoding="utf-8")

    kept = {f"{p['spaceId']}.jpg" for p in photos}
    for stale in ASSETS.glob("*.jpg"):
        if stale.name not in kept:
            stale.unlink()
            print(f"removed stale asset {stale.name}")

    total_kb = sum(f.stat().st_size for f in ASSETS.glob("*.jpg")) // 1024
    print(f"wrote {OUT.relative_to(REPO)}")
    print(f"wrote {len(kept)} images into {ASSETS.relative_to(REPO)} ({total_kb} KB total)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
