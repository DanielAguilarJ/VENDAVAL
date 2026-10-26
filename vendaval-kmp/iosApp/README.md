# iOS host

The Swift sources, the `Info.plist` and the Kotlin framework configuration in this
repository are complete. What is **not** here is `iosApp.xcodeproj`.

That is a deliberate omission, not an oversight: an Xcode project file is a large
generated artifact, and it was not possible to produce a verified one on the machine
this project was built on (Command Line Tools only, no Xcode). A hand-written
`project.pbxproj` that has never been opened by Xcode is more likely to waste your time
than to save it.

## Wiring it up (once, on a machine with Xcode)

1. **Enable the Apple targets.** In `gradle.properties`:

   ```properties
   vendaval.enableIos=true
   ```

   This turns on `iosArm64`, `iosSimulatorArm64` and `iosX64` in both modules and starts
   producing the `VendavalApp` framework.

2. **Build the framework** so Xcode has something to link against:

   ```bash
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```

3. **Create the app target.** In Xcode: *File → New → Project → iOS → App*, named
   `iosApp`, interface SwiftUI, language Swift. Save it into this `iosApp/` directory so
   it picks up the existing sources.

4. **Replace the generated sources** with the ones already here: `iOSApp.swift`,
   `ContentView.swift` and `Info.plist`. Delete Xcode's own `ContentView.swift`.

5. **Link the Kotlin framework.** In the app target's *Build Settings*:

   - `FRAMEWORK_SEARCH_PATHS` →
     `$(SRCROOT)/../composeApp/build/bin/iosSimulatorArm64/debugFramework`
   - `OTHER_LDFLAGS` → `$(inherited) -framework VendavalApp`

6. **Add a build phase** so the framework is rebuilt with the app. *Build Phases → New
   Run Script Phase*, placed before *Compile Sources*:

   ```bash
   cd "$SRCROOT/.."
   ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
   ```

   Then set `Enable User Script Sandboxing` to `NO`, otherwise the script cannot reach
   the Gradle cache.

## What is already handled in Kotlin

- **Compose entry point** — `MainViewController()` in
  `composeApp/src/iosMain/kotlin/mx/vendaval/app/MainViewController.kt`. It also starts
  Koin, guarded against double initialisation because SwiftUI can recreate the
  representable.
- **Storage** — `IosStoragePaths` writes to Application Support inside the sandbox.
- **Location** — `IosLocationProvider` uses Core Location's cached fix and triggers the
  authorisation prompt. It depends on `NSLocationWhenInUseUsageDescription`, which is
  already in the `Info.plist` here.
- **Hand-off** — `IosExternalLauncher` opens Apple Maps, the dialler and Safari, and
  copies share text to the pasteboard.
- **Networking** — Ktor's Darwin engine is wired into `iosMain`.

## Honest status

The iOS source set is written and reviewed but **has never been compiled**. Kotlin/Native
needs the iOS SDKs that ship with Xcode, so `linkDebugFrameworkIosSimulatorArm64` could
not run here. Expect to fix a small number of Kotlin/Native interop details on the first
build — the most likely candidates are the `@OptIn(ExperimentalForeignApi::class)`
annotations around `CLLocationCoordinate2D.useContents` and the `openURL` overload
signature, both in `IosPlatformServices.kt`.

Android and desktop, by contrast, are verified: both compile, the shared test suite is
green, and the desktop app has been run.
