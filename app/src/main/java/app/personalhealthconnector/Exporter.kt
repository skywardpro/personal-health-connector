package app.personalhealthconnector

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** One day of raw data, as JSON text. */
class DayFile(val name: String, val json: String)

class UserFacingException(message: String) : Exception(message)

/** Reads data and writes it to the chosen destinations. Throws UserFacingException with plain words. */
object Exporter {
    fun run(context: Context): Int {
        val prefs = Prefs(context)
        if (!prefs.driveEnabled && !prefs.folderEnabled) throw UserFacingException("Choose a destination first.")
        val days = HuaweiHealth.readSleepDays(context) // throws "no access yet" until Huawei approves
        if (prefs.folderEnabled) writeToFolder(context, prefs.folderUri, days)
        if (prefs.driveEnabled) GoogleDrive.upload(context, days)
        return days.size
    }

    private fun writeToFolder(context: Context, uri: String?, days: List<DayFile>) {
        val dir = uri?.let { DocumentFile.fromTreeUri(context, Uri.parse(it)) }
            ?: throw UserFacingException("The phone folder is not chosen.")
        if (!dir.canWrite()) throw UserFacingException("The phone folder is no longer writable. Choose it again.")
        for (d in days) {
            val f = dir.findFile(d.name) ?: dir.createFile("application/json", d.name.removeSuffix(".json"))
            f?.let { context.contentResolver.openOutputStream(it.uri, "wt")?.use { o -> o.write(d.json.toByteArray()) } }
        }
    }
}

/** Google Drive through plain REST. Scope drive.file: the app only sees files it created itself. */
object GoogleDrive {
    const val SCOPE = "https://www.googleapis.com/auth/drive.file"
    const val FOLDER = "Personal Health Connector"
    val configured get() = BuildConfig.GOOGLE_CLIENT_ID.isNotEmpty()

    fun upload(context: Context, days: List<DayFile>) {
        if (!configured) throw UserFacingException("Google Drive is not configured in this build.")
        val token = GoogleAuth.tokenBlocking(context)
            ?: throw UserFacingException("Google Drive access needs your approval. Open the app and link the account again.")
        val folderId = findOrCreateFolder(token)
        for (d in days) {
            val existing = find(token, "name='${d.name}' and '$folderId' in parents and trashed=false")
            if (existing == null) {
                val meta = "{\"name\":\"${d.name}\",\"parents\":[\"$folderId\"]}"
                val b = "phc-boundary"
                val body = "--$b\r\nContent-Type: application/json\r\n\r\n$meta\r\n--$b\r\nContent-Type: application/json\r\n\r\n${d.json}\r\n--$b--"
                call(token, "POST", "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart", body, "multipart/related; boundary=$b")
            } else {
                call(token, "PATCH", "https://www.googleapis.com/upload/drive/v3/files/$existing?uploadType=media", d.json, "application/json")
            }
        }
    }

    private fun findOrCreateFolder(token: String): String =
        find(token, "name='$FOLDER' and mimeType='application/vnd.google-apps.folder' and trashed=false")
            ?: idOf(call(token, "POST", "https://www.googleapis.com/drive/v3/files",
                "{\"name\":\"$FOLDER\",\"mimeType\":\"application/vnd.google-apps.folder\"}", "application/json"))!!

    private fun find(token: String, q: String): String? =
        idOf(call(token, "GET", "https://www.googleapis.com/drive/v3/files?fields=files(id)&q=" + URLEncoder.encode(q, "UTF-8"), null, null), optional = true)

    private fun idOf(json: String, optional: Boolean = false): String? =
        Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            ?: if (optional) null else throw UserFacingException("Google Drive gave an unexpected answer.")

    private fun call(token: String, method: String, url: String, body: String?, type: String?): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = if (method == "PATCH") "POST" else method
            if (method == "PATCH") c.setRequestProperty("X-HTTP-Method-Override", "PATCH")
            c.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", type)
                c.outputStream.use { it.write(body.toByteArray()) }
            }
            if (c.responseCode !in 200..299) throw UserFacingException("Google Drive refused the request (code ${c.responseCode}).")
            return c.inputStream.bufferedReader().readText()
        } finally {
            c.disconnect()
        }
    }
}
