plugins {
    alias(libs.plugins.android.application)
}

android {

    buildFeatures {
        viewBinding = true;
    }

    namespace = "com.example.tvandmovies"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tvandmovies"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    implementation (libs.blurview)
    // ---ROOM ADATBÁZIS ---
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
}