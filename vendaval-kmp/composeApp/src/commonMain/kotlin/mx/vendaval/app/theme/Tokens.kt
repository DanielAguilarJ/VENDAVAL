package mx.vendaval.app.theme

import androidx.compose.ui.graphics.Color

/**
 * Colour tokens.
 *
 * ## What changed from the first revision, and why
 *
 * The original port copied `styles.css` literally: near-black `#030712` under a
 * single bright cyan `#00f2fe` that ended up on the primary button, the sync
 * badge, distances, card borders and focus rings all at once. When one accent
 * appears everywhere it stops carrying meaning, and the combination of
 * blue-black plus electric cyan plus translucent rounded cards is the exact
 * signature of a generated interface.
 *
 * Three deliberate moves:
 *
 * 1. **The surface ramp got warmer and gained steps.** `#0A0E17` through
 *    `#1B2433` reads as considered rather than absolute. Four steps let cards sit
 *    on the background by tone alone, so the 8%-alpha hairline borders that were
 *    doing all the separation work become optional polish.
 * 2. **The accent is now reserved for interaction.** It appears on controls the
 *    user can act on. Nothing decorative uses it.
 * 3. **Venue hues were desaturated one step.** They keep the identity of the web
 *    map — a Teatro is still pink, a Casa still orange — but stop competing with
 *    the content at small sizes.
 */
internal object Tokens {

    // ── Dark surface ramp ──────────────────────────────────────────────────
    /** App background. */
    val Ink = Color(0xFF0A0E17)

    /** Sunken areas: search field wells, inactive tracks. */
    val Ink2 = Color(0xFF111827)

    /** Card and sheet fill. Opaque, so text contrast is predictable. */
    val Card = Color(0xFF151C29)

    /** Raised or selected card. */
    val CardHi = Color(0xFF1B2433)

    /** Hairline. Kept subtle because tone already separates the surfaces. */
    val Line = Color(0x14FFFFFF)
    val LineStrong = Color(0x2EFFFFFF)

    // ── Text ──────────────────────────────────────────────────────────────
    val Text = Color(0xFFF3F5F8)
    val Muted = Color(0xFF9AA6B8)
    val Faint = Color(0xFF61708A)

    // ── Interaction ───────────────────────────────────────────────────────
    /**
     * The single accent: a warm clay terracotta.
     *
     * It replaced a bright cyan (`#00E0D0`, itself a slightly deepened `#00f2fe`
     * from the web build). Electric cyan over blue-black is the most overused
     * palette in generated interfaces, and no amount of per-screen work overrode
     * it — the app read as a template because of this one token.
     *
     * Both lightness values here are load-bearing rather than taste:
     * - `Accent` carries **Ink** text as a filled surface (`onPrimary = Ink`), so
     *   it has to stay light enough for that. #E06A42 on #0A0E17 measures 5.80:1.
     * - `AccentDeep` is the light theme's primary and carries **white**, so it has
     *   to stay dark enough. #A8431F under white measures 6.0:1.
     *
     * Both clear WCAG AA for normal text, which the raw cyan could only do in one
     * direction.
     */
    val Accent = Color(0xFFE06A42)
    val AccentDeep = Color(0xFFA8431F)
    val AccentGlow = Color(0x1FE06A42)

    // ── Venue hues ────────────────────────────────────────────────────────
    // Desaturated one step from the web values, which were tuned for 8 px map
    // dots rather than for type and 74 dp covers.
    val VenueCasa = Color(0xFFE8853C)       // web #f97316
    val VenueCentro = Color(0xFF5B8DEF)     // web #3b82f6
    val VenueGaleria = Color(0xFFA971F0)    // web #a855f7
    val VenueTeatro = Color(0xFFE8629B)     // web #ec4899
    val VenueComunitario = Color(0xFF3FBF8F) // web #10b981
    val VenueOtro = Color(0xFF8494AB)

    /** Darker partner for each hue, used as the second stop of a cover gradient. */
    val VenueCasaDeep = Color(0xFFC1601F)
    val VenueCentroDeep = Color(0xFF3E6BC4)
    val VenueGaleriaDeep = Color(0xFF7F4BC9)
    val VenueTeatroDeep = Color(0xFFC2427B)
    val VenueComunitarioDeep = Color(0xFF2A9670)
    val VenueOtroDeep = Color(0xFF5D6B80)

    // ── Status ────────────────────────────────────────────────────────────
    val Warning = Color(0xFFE8A93C)
    val Danger = Color(0xFFF2555A)

    // ── Light theme ───────────────────────────────────────────────────────
    // The source design has no light mode, so this is a derivation: the ramp
    // inverts and the accent darkens to hold 4.5:1 against paper, which the raw
    // cyan cannot do.
    val Paper = Color(0xFFF7F8FA)
    val PaperSunken = Color(0xFFEDF0F4)
    val PaperCard = Color(0xFFFFFFFF)
    val PaperLine = Color(0x14101828)
    val PaperLineStrong = Color(0x29101828)
    val InkText = Color(0xFF0D1421)
    val InkMuted = Color(0xFF4A5768)
    val InkFaint = Color(0xFF7A889B)
}
