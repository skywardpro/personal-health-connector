package app.personalhealthconnector

import android.content.Context

/** Small settings store. Holds no health data, only choices and the last export result. */
class Prefs(context: Context) {
    private val p = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var sleepEnabled: Boolean
        get() = p.getBoolean("sleep", true)
        set(v) = p.edit().putBoolean("sleep", v).apply()
    var driveEnabled: Boolean
        get() = p.getBoolean("drive", false)
        set(v) = p.edit().putBoolean("drive", v).apply()
    var driveAccount: String?
        get() = p.getString("driveAccount", null)
        set(v) = p.edit().putString("driveAccount", v).apply()
    var folderEnabled: Boolean
        get() = p.getBoolean("folder", false)
        set(v) = p.edit().putBoolean("folder", v).apply()
    var folderUri: String?
        get() = p.getString("folderUri", null)
        set(v) = p.edit().putString("folderUri", v).apply()
    /** Minutes after midnight for the daily export, or -1 for off. */
    var scheduleMinutes: Int
        get() = p.getInt("schedule", -1)
        set(v) = p.edit().putInt("schedule", v).apply()
    var lastExportAt: Long
        get() = p.getLong("lastAt", 0)
        set(v) = p.edit().putLong("lastAt", v).apply()
    var lastNights: Int
        get() = p.getInt("lastNights", 0)
        set(v) = p.edit().putInt("lastNights", v).apply()
    var huaweiSignedIn: Boolean
        get() = p.getBoolean("hwSignedIn", false)
        set(v) = p.edit().putBoolean("hwSignedIn", v).apply()
    var huaweiGranted: Boolean
        get() = p.getBoolean("hwGranted", false)
        set(v) = p.edit().putBoolean("hwGranted", v).apply()
    var lastError: String?
        get() = p.getString("lastError", null)
        set(v) = p.edit().putString("lastError", v).apply()
}
