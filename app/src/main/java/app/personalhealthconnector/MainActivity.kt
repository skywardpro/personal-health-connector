package app.personalhealthconnector

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

const val PRIVACY_URL = "https://skywardpro.github.io/personal-health-connector/PRIVACY"

private val LightColors = lightColorScheme(
    primary = Color(0xFF00695C), onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB), onPrimaryContainer = Color(0xFF00201C),
    secondaryContainer = Color(0xFFD7EBE6), tertiaryContainer = Color(0xFFFFE9B5),
    surface = Color(0xFFF6FAF9), surfaceContainer = Color(0xFFFFFFFF), background = Color(0xFFF6FAF9),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FD9C8), onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005046), onPrimaryContainer = Color(0xFFB2F1E5),
    secondaryContainer = Color(0xFF2B4742), tertiaryContainer = Color(0xFF5A4300),
    surface = Color(0xFF0E1514), surfaceContainer = Color(0xFF18211F), background = Color(0xFF0E1514),
)

@Composable
private fun PhcTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { PhcTheme { App() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Welcome" to Icons.Filled.Info, "Huawei" to Icons.Filled.Person, "Data" to Icons.Filled.Settings, "Export" to Icons.Filled.Refresh)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CenterAlignedTopAppBar(title = { Text(if (tab == 0) "Personal Health Connector" else tabs[tab].first, fontWeight = FontWeight.SemiBold) }) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, (t, icon) ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icon, null) }, label = { Text(t) })
                }
            }
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (tab) {
                0 -> Welcome { tab = 1 }
                1 -> HuaweiSignIn(prefs)
                2 -> DataAndPlace(prefs)
                else -> ExportStatus(prefs)
            }
        }
    }
}

// ---- Small building blocks ----

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

/** State chip: [ok] positive, otherwise neutral (not an error; errors use ErrorText). */
@Composable
private fun StatusChip(text: String, ok: Boolean) {
    val c = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(50), color = if (ok) c.primaryContainer else c.surfaceVariant) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (ok) Icons.Filled.CheckCircle else Icons.Filled.Info, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Body(text: String) = Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun ErrorText(text: String) = Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onChange, enabled = enabled)
    }
}

// ---- Screens ----

@Composable
private fun Welcome(next: () -> Unit) {
    val ctx = LocalContext.current
    Text("Your health data, copied to your own place.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
    SectionCard("What it reads") {
        Body("Sleep records from Huawei Health, read-only, through Huawei's official Health Service Kit. Only after you sign in with HUAWEI ID and agree.")
    }
    SectionCard("Where it goes") {
        Body("Only to the places you pick: your Google Drive or a folder on this phone. No developer server, no analytics, no accounts. The app never writes to Huawei Health.")
    }
    OutlinedButton(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL))) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Read the privacy policy") }
    Button(onClick = next, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("I understand, continue") }
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
    SectionCard("HUAWEI ID") {
        StatusChip(if (signedIn) "Signed in" else "Not signed in", signedIn)
        Button(onClick = {
            try { launcher.launch(HuaweiHealth.signInIntent(ctx as Activity)) }
            catch (e: Exception) { problem = "Huawei sign-in is not available on this phone (Huawei Mobile Services is missing)." }
        }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Sign in with HUAWEI ID") }
        problem?.let { ErrorText(it) }
    }
    SectionCard("Access to sleep data") {
        StatusChip(if (prefs.huaweiGranted) "Granted" else "Not granted yet", prefs.huaweiGranted)
        if (!prefs.huaweiGranted) Body("Huawei has not approved the app's access yet. Export will return data as soon as it does; nothing is lost in the meantime.")
    }
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

    SectionCard("Data to export") {
        SwitchRow("Sleep", "Nights, stages and times", sleep) { sleep = it; prefs.sleepEnabled = it }
        for (n in listOf("Steps", "Heart rate", "Blood oxygen (SpO2)", "Stress")) SwitchRow(n, "Coming soon", false, enabled = false) {}
    }
    SectionCard("Google Drive") {
        if (!GoogleDrive.configured) {
            StatusChip("Not configured", false)
            Body("Google Drive is not configured in this build.")
        } else if (account != null) {
            StatusChip("Linked: $account", true)
            Body("Files go to the folder \"${GoogleDrive.FOLDER}\" that this app creates. The app sees only files it created.")
            SwitchRow("Save to Google Drive", null, drive) { drive = it; prefs.driveEnabled = it }
            OutlinedButton(onClick = { prefs.driveAccount = null; prefs.driveEnabled = false; account = null; drive = false }, Modifier.heightIn(min = 48.dp)) { Text("Unlink") }
        } else {
            StatusChip("Not linked", false)
            Body("Uses the narrow drive.file access: the app can only see files it creates itself.")
            Button(onClick = {
                GoogleAuth.authorize(ctx as Activity, { r ->
                    val sender = GoogleAuth.consentSender(r)
                    if (sender != null) consent.launch(IntentSenderRequest.Builder(sender).build())
                    else { prefs.driveAccount = "Google account"; account = "Google account"; prefs.driveEnabled = true; drive = true }
                }, { message = it })
            }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Link Google account") }
        }
    }
    SectionCard("Folder on this phone") {
        StatusChip(if (folderUri == null) "No folder chosen" else "Folder chosen", folderUri != null)
        if (folderUri != null) SwitchRow("Save to this folder", null, folder) { folder = it; prefs.folderEnabled = it }
        FilledTonalButton(onClick = { folderPicker.launch(null) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (folderUri == null) "Choose folder" else "Change folder") }
    }
    message?.let { ErrorText(it) }
}

@Composable
private fun ExportStatus(prefs: Prefs) {
    val ctx = LocalContext.current
    var minutes by remember { mutableIntStateOf(prefs.scheduleMinutes) }
    var running by remember { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val set = { m: Int -> minutes = m; prefs.scheduleMinutes = m; ExportWorker.schedule(ctx, m) }
    fun pickTime() {
        val start = if (minutes < 0) 8 * 60 else minutes
        TimePickerDialog(ctx, { _, h, m -> set(h * 60 + m) }, start / 60, start % 60, true).show()
    }

    SectionCard("Schedule") {
        SwitchRow("Export every day", if (minutes < 0) "Off" else "At %02d:%02d".format(minutes / 60, minutes % 60), minutes >= 0) { if (it) pickTime() else set(-1) }
        if (minutes >= 0) OutlinedButton(onClick = { pickTime() }, Modifier.heightIn(min = 48.dp)) { Text("Change time") }
    }
    SectionCard("Last export") {
        key(refresh) {
            val last = prefs.lastExportAt
            if (last == 0L) {
                StatusChip("No export yet", false)
                Body("Press \"Export now\" once a destination is chosen.")
            } else {
                StatusChip("Exported", true)
                Body("${DateFormat.getDateTimeInstance().format(Date(last))}\nNights exported: ${prefs.lastNights}")
            }
            prefs.lastError?.let { ErrorText(it) }
        }
    }
    Button(
        enabled = !running,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        onClick = {
            running = true
            Thread { ExportWorker.runAndRecord(ctx.applicationContext); (ctx as Activity).runOnUiThread { running = false; refresh++ } }.start()
        },
    ) { Text(if (running) "Exporting..." else "Export now") }
}
