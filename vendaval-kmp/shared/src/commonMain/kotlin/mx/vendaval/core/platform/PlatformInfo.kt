package mx.vendaval.core.platform

/** The three deployment surfaces the project targets. */
enum class PlatformKind {
    ANDROID,
    IOS,
    DESKTOP,
}

/**
 * Host description, surfaced in the settings screen and attached to bug
 * reports. Kept as plain data so it can be asserted on in common tests.
 */
data class PlatformInfo(
    val kind: PlatformKind,
    val label: String,
) {
    /** Touch-first platforms get larger hit targets and gesture-driven maps. */
    val isTouchPrimary: Boolean get() = kind != PlatformKind.DESKTOP
}

expect fun currentPlatform(): PlatformInfo
