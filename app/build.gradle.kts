import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Public Android OAuth client ID (not a secret). Override with google.clientId=... in local.properties; empty = Drive disabled.

val googleClientId: String = rootProject.file("local.properties").takeIf { it.exists() }
    ?.readLines()?.firstOrNull { it.startsWith("google.clientId=") }?.substringAfter("=")?.trim() ?: "802062018625-63iq41488a0a5mkcbhnarmt28fpllgk8.apps.googleusercontent.com"

android {
    namespace = "app.personalhealthconnector"
    compileSdk = 34
    defaultConfig {
        applicationId = "app.personalhealthconnector"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
    }
    // Google matches package + signing SHA-1, so debug builds use the release key when keystore.properties exists.
    val ks = Properties().apply { rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }
    if (ks.isEmpty) {
        logger.warn("keystore.properties not found: using the default debug key (Google sign-in will not match).")
    } else {
        signingConfigs.getByName("debug") {
            storeFile = file(ks.getProperty("storeFile")); storeType = "pkcs12"
            storePassword = ks.getProperty("password"); keyAlias = ks.getProperty("alias"); keyPassword = ks.getProperty("password")
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.huawei.hms:health:6.16.0.300")
}
