plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // ✳️ Hilt usa KAPT
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)

    // ✳️ Room usa KSP
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.empresa.libra_users"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.empresa.libra_users"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
}

dependencies {
    // ---------- Compose base ----------
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")

    // Navegación Compose
    implementation(libs.navigation.compose)

    // Lifecycle para Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // ---------- Corrutinas / util ----------
    implementation(libs.coroutines.android)

    // (Opcional) Red / almacenamiento si los usas
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.datastore.prefs)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.coil.compose)

    // ---------- Room (KSP) ----------
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)   // ✅ SOLO KSP para Room

    // ---------- Hilt (KAPT) ----------
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ---------- Tests / Debug ----------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// KAPT para Hilt
kapt {
    correctErrorTypes = true
}

// KSP para Room (opcional, para exportar esquemas)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}