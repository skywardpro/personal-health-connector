package app.personalhealthconnector

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.text.DateFormat
import java.util.Date

const val PRIVACY_URL = "https://skywardpro.github.io/personal-health-connector/PRIVACY"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App() } }
    }
}

@Composable
private fun App() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Welcome", "Huawei", "Data and place", "Export")
    Scaffold(bottomBar = {
        NavigationBar {
            titles.forEachIndexed { i, t ->
                NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = {}, label = { Text(t) })
            }
        }
    }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (tab) {
                0 -> Welcome { tab = 1 }
                1 -> HuaweiSignIn(prefs)
                2 -> DataAndPlace(prefs)
                else -> ExportStatus(prefs)
            }
        }
    }
}

@Composable
private fun Welcome(next: () -> Unit) {
    val ctx = LocalContext.current
    Text("Personal Health Connector", style = MaterialTheme.typography.headlineSmall)
    Text("This app copies your own health data from Huawei Health to a place you choose: your Google Drive or a folder on this phone.")
    Text("What it reads: sleep records, read-only, through Huawei's official Health Service Kit, only after you sign in with HUAWEI ID and agree.")
    Text("Where it goes: only to the destinations you pick. There is no developer server, no analytics and no accounts. The app never writes to Huawei Health.")
    OutlinedButton(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL))) }) { Text("Read the privacy policy") }
    Button(onClick = next) { Text("I understand, continue") }
}

@Composable
private fun HuaweiSignIn(prefs: Prefs) {
    val ctx = LocalContext.current
    var signedIn by remember { mutableStateOf(prefs.huaweiSignedIn) }
    var problem by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        problem = HuaweiHealth.parseSignIn(r.data)
        signedIn = problem == null
        prefs.huaweiSignedIn = signedIn
    }
    Text("Huawei account", style = MaterialTheme.typography.titleLarge)
    Text(if (signedIn) "Signed in with HUAWEI ID." else "Not signed in.")
    Text(
        if (prefs.huaweiGranted) "Access to sleep data: granted."
        else "Access to sleep data: not granted yet. Huawei has not approved the app's access, so export will not return data until it does."
    )
    problem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Button(onClick = {
        try { launcher.launch(HuaweiHealth.signInIntent(ctx as Activity)) }
        catch (e: Exception) { problem = "Huawei sign-in is not available on this phone (Huawei Mobile Services is missing)." }
    }) { Text("Sign in with HUAWEI ID") }
}

@Composable
private fun DataAndPlace(prefs: Prefs) {
    val ctx = LocalContext.current
    var sleep by remember { mutableStateOf(prefs.sleepEnabled) }
    var drive by remember { mutableStateOf(prefs.driveEnabled) }
    var folder by remember { mutableStateOf(prefs.folderEnabled) }
    var folderUri by remember { mutableStateOf(prefs.folderUri) }
    var account by remember { mutableStateOf(prefs.driveAccount) }
    var message by remember { mutableStateOf<String?>(null) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            prefs.folderUri = uri.toString(); folderUri = uri.toString()
            prefs.folderEnabled = true; folder = true
        }
    }
    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) { prefs.driveAccount = "Google account"; account = "Google account"; prefs.driveEnabled = true; drive = true }
        else message = "Google Drive access was not approved."
    }

    Text("Data to export", style = MaterialTheme.typography.titleLarge)
    DataRow("Sleep", sleep, true) { sleep = it; prefs.sleepEnabled = it }
    for (n in listOf("Steps", "Heart rate", "Blood oxygen (SpO2)", "Stress")) DataRow(n, false, false) {}

    HorizontalDivider()
    Text("Where to save (one or both)", style = MaterialTheme.typography.titleLarge)

    Text("Google Drive", style = MaterialTheme.typography.titleMedium)
    if (!GoogleDrive.configured) {
        Text("Google Drive is not configured in this build.")
    } else if (account != null) {
        Text("Linked: $account. Files go to the folder \"${GoogleDrive.FOLDER}\" that this app creates.")
        Switch(drive, { drive = it; prefs.driveEnabled = it })
        OutlinedButton(onClick = { prefs.driveAccount = null; prefs.driveEnabled = false; account = null; drive = false }) { Text("Unlink") }
    } else {
        Text("The app can only see files it creates itself (drive.file access).")
        Button(onClick = {
            GoogleAuth.authorize(ctx as Activity, { r ->
                val sender = GoogleAuth.consentSender(r)
                if (sender != null) consent.launch(IntentSenderRequest.Builder(sender).build())
                else { prefs.driveAccount = "Google account"; account = "Google account"; prefs.driveEnabled = true; drive = true }
            }, { message = it })
        }) { Text("Link Google account") }
    }

    Text("Folder on this phone", style = MaterialTheme.typography.titleMedium)
    Text(if (folderUri == null) "No folder chosen." else "Folder chosen.")
    if (folderUri != null) Switch(folder, { folder = it; prefs.folderEnabled = it })
    Button(onClick = { folderPicker.launch(null) }) { Text(if (folderUri == null) "Choose folder" else "Change folder") }
    message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun DataRow(name: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(name)
            if (!enabled) Text("coming soon", style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked, onChange, enabled = enabled)
    }
}

@Composable
private fun ExportStatus(prefs: Prefs) {
    val ctx = LocalContext.current
    var minutes by remember { mutableIntStateOf(prefs.scheduleMinutes) }
    var running by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val set = { m: Int -> minutes = m; prefs.scheduleMinutes = m; ExportWorker.schedule(ctx, m) }

    Text("Schedule", style = MaterialTheme.typography.titleLarge)
    Text(if (minutes < 0) "Automatic export is off." else "Daily at %02d:%02d.".format(minutes / 60, minutes % 60))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            val start = if (minutes < 0) 8 * 60 else minutes
            TimePickerDialog(ctx, { _, h, m -> set(h * 60 + m) }, start / 60, start % 60, true).show()
        }) { Text("Choose time") }
        OutlinedButton(onClick = { set(-1) }, enabled = minutes >= 0) { Text("Turn off") }
    }

    HorizontalDivider()
    Text("Status", style = MaterialTheme.typography.titleLarge)
    key(refresh) {
        val last = prefs.lastExportAt
        Text(if (last == 0L) "No export yet." else "Last export: ${DateFormat.getDateTimeInstance().format(Date(last))}")
        if (last != 0L) Text("Nights exported: ${prefs.lastNights}")
        prefs.lastError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    Button(enabled = !running, onClick = {
        running = true
        Thread { ExportWorker.runAndRecord(ctx.applicationContext); (ctx as Activity).runOnUiThread { running = false; refresh++ } }.start()
    }) { Text(if (running) "Exporting..." else "Export now") }
}
