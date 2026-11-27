package app.personalhealthconnector

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit

/** Google Identity Services authorization for the drive.file scope only. */
object GoogleAuth {
    private fun request() = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(GoogleDrive.SCOPE))).build()

    /** Starts the authorization. The result either carries a token or a consent screen to show. */
    fun authorize(activity: Activity, onResult: (AuthorizationResult) -> Unit, onError: (String) -> Unit) {
        Identity.getAuthorizationClient(activity).authorize(request())
            .addOnSuccessListener(onResult)
            .addOnFailureListener { onError("Google sign-in failed. Check your connection and try again.") }
    }

    fun consentSender(r: AuthorizationResult): IntentSender? = r.pendingIntent?.intentSender

    /** For the background worker: a token only if the user already approved, never shows UI. */
    fun tokenBlocking(context: Context): String? = try {
        val r = Tasks.await(Identity.getAuthorizationClient(context).authorize(request()), 30, TimeUnit.SECONDS)
        if (r.hasResolution()) null else r.accessToken
    } catch (e: Exception) { null }
}
