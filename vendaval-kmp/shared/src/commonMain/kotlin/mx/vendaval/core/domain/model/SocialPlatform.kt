package mx.vendaval.core.domain.model

/**
 * A social network a venue can list on its community-authored profile.
 *
 * A closed set on purpose: the submission form offers one field per platform, and a
 * known key lets the detail screen pick the right icon and build the right link.
 * Unknown keys arriving off the wire are dropped rather than guessed at, so a typo in
 * a shared table can never surface as a bogus platform in the UI.
 */
enum class SocialPlatform(val label: String) {
    FACEBOOK("Facebook"),
    INSTAGRAM("Instagram"),
    X("X"),
    WHATSAPP("WhatsApp"),
    TIKTOK("TikTok"),
    YOUTUBE("YouTube"),
    ;

    companion object {
        /** Case-insensitive lookup by enum name, used when decoding the cached/wire map. */
        fun fromKey(raw: String?): SocialPlatform? =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
    }
}
