package app.personalhealthconnector

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.huawei.hmf.tasks.Tasks
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.HealthDataTypes
import com.huawei.hms.hihealth.data.Scopes
import com.huawei.hms.hihealth.options.HealthRecordReadOptions
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/** The only place that talks to Huawei Health Service Kit. Read-only, device-side, no client secret. */
object HuaweiHealth {
    /** Only the sleep read scope is requested. More scopes are added when Huawei approves them. */
    private val scopes = listOf(Scope(Scopes.HEALTHKIT_SLEEP_READ))
    private const val DAYS_BACK = 30L

    fun signInIntent(activity: Activity): Intent {
        val params = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setScopeList(scopes).createParams()
        return HuaweiIdAuthManager.getService(activity, params).signInIntent
    }

    /** Parses the sign-in result. Returns null on success, or a plain-words problem. */
    fun parseSignIn(data: Intent?): String? {
        val task = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
        return if (task.isSuccessful) null
        else "Huawei sign-in did not complete. Huawei Mobile Services must be installed and the app approved by Huawei."
    }

    /** Reads sleep records of the last days and groups them into one JSON file per day. */
    fun readSleepDays(context: Context): List<DayFile> {
        val end = System.currentTimeMillis()
        val options = HealthRecordReadOptions.Builder()
            .setTimeInterval(end - TimeUnit.DAYS.toMillis(DAYS_BACK), end, TimeUnit.MILLISECONDS)
            .readByDataType(HealthDataTypes.DT_HEALTH_RECORD_SLEEP)
            .build()
        val records = try {
            Tasks.await(HuaweiHiHealth.getHealthRecordController(context).getHealthRecord(options), 60, TimeUnit.SECONDS)
                .healthRecords
        } catch (e: Exception) {
            throw UserFacingException("No access to Huawei Health sleep data yet. Sign in with HUAWEI ID and wait until Huawei approves the app.")
        }
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).apply { timeZone = TimeZone.getDefault() }
        return records.groupBy { day.format(Date(it.getEndTime(TimeUnit.MILLISECONDS))) }.map { (d, list) ->
            val items = list.joinToString(",") { r ->
                val fields = r.fieldValues.entries.joinToString(",") { "\"${esc(it.key)}\":\"${esc(it.value.toString())}\"" }
                "{\"start\":\"${iso.format(Date(r.getStartTime(TimeUnit.MILLISECONDS)))}\"," +
                    "\"end\":\"${iso.format(Date(r.getEndTime(TimeUnit.MILLISECONDS)))}\",\"fields\":{$fields}}"
            }
            DayFile("sleep-$d.json", "{\"type\":\"sleep\",\"date\":\"$d\",\"records\":[$items]}")
        }
    }

    private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
}
