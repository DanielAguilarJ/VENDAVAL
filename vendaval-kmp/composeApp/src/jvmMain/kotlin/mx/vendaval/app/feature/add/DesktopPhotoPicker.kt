package mx.vendaval.app.feature.add

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Desktop image chooser backed by AWT's native [FileDialog].
 *
 * The dialog is modal and blocking, so it runs off the Compose UI thread on
 * [Dispatchers.IO]; a cancelled dialog (no file chosen) yields `null`. Only common
 * raster formats the decoder can read are offered, and a read failure degrades to
 * `null` rather than throwing into the form.
 */
class DesktopPhotoPicker : PhotoPicker {
    override suspend fun pick(): PickedImage? = withContext(Dispatchers.IO) {
        val dialog = FileDialog(null as Frame?, "Selecciona una imagen", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name ->
            val n = name.lowercase()
            n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png") || n.endsWith(".webp")
        }
        dialog.isVisible = true

        val directory = dialog.directory
        val fileName = dialog.file
        if (directory == null || fileName == null) return@withContext null

        runCatching {
            val file = File(directory, fileName)
            PickedImage(name = file.name, bytes = file.readBytes())
        }.getOrNull()
    }
}
