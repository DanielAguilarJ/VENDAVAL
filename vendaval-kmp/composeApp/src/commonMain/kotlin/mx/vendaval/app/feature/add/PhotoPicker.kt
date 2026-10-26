package mx.vendaval.app.feature.add

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * One image the user picked for a submission, held in memory until the space is saved.
 *
 * Platform-neutral on purpose (a display name + raw bytes): the form never needs to
 * know how the bytes were obtained, so each platform can supply its own chooser
 * without leaking UIKit / Android / AWT types into common code.
 */
class PickedImage(val name: String, val bytes: ByteArray)

/**
 * Opens the platform's image chooser and returns the selected image, or `null` when
 * the user cancels. Implementations are provided per platform; [NoOpPhotoPicker] is
 * the safe default on a platform where a real chooser is not wired yet, so the form
 * degrades to "no photos" instead of failing to build.
 */
interface PhotoPicker {
    suspend fun pick(): PickedImage?
}

/** Picker that always returns `null` — the default until a platform wires a chooser. */
object NoOpPhotoPicker : PhotoPicker {
    override suspend fun pick(): PickedImage? = null
}

/**
 * The picker in scope for the submission form. Defaults to [NoOpPhotoPicker]; each
 * platform's entry point provides a real chooser where one is wired (Desktop supplies
 * an AWT file chooser). Mirrors the `LocalPhotoStore` / `LocalStrings` pattern so the
 * form reads it without a constructor dependency.
 */
val LocalPhotoPicker: ProvidableCompositionLocal<PhotoPicker> =
    staticCompositionLocalOf { NoOpPhotoPicker }
