package mx.vendaval.core.platform

import android.os.Build

actual fun currentPlatform(): PlatformInfo = PlatformInfo(
    kind = PlatformKind.ANDROID,
    label = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
)
