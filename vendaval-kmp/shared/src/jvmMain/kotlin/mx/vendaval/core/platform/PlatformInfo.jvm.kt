package mx.vendaval.core.platform

actual fun currentPlatform(): PlatformInfo = PlatformInfo(
    kind = PlatformKind.DESKTOP,
    label = buildString {
        append(System.getProperty("os.name") ?: "Desktop")
        System.getProperty("os.version")?.let { append(" ").append(it) }
        append(" · JVM ").append(System.getProperty("java.version") ?: "?")
    },
)
