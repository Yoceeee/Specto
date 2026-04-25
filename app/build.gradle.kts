import java.util.Properties
import java.io.FileInputStream

val properties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { properties.load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {

    buildFeatures {
        viewBinding = true;
        buildConfig = true;
    }

    namespace = "com.example.tvandmovies"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tvandmovies"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // az API beolvasása
        val tmdbApiKey = properties.getProperty("TMDB_API_KEY", "\"\"")

        // változó generálása
        buildConfigField("String", "TMDB_API_KEY", tmdbApiKey)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.glide)
    implementation(libs.cronet.embedded)
    implementation(libs.androidx.swiperefreshlayout)
    annotationProcessor(libs.compiler)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.chip.navigation.bar)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.arch.core.testing)
    implementation(libs.androidx.espresso.idling.resource)
    implementation (libs.blurview)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.androidx.palette)

    // ---ROOM ADATBÁZIS ---
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    // --- Credentials ---
    implementation(libs.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)
}