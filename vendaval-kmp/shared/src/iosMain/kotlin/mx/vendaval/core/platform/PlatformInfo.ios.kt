package mx.vendaval.core.platform

import platform.UIKit.UIDevice

actual fun currentPlatform(): PlatformInfo = PlatformInfo(
    kind = PlatformKind.IOS,
    label = with(UIDevice.currentDevice) { "$systemName $systemVersion" },
)
