plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.miso.vinilo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.miso.vinilo"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig field to configure the base URL per build type
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Override BASE_URL in release if you want a different production endpoint
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
        }
        debug {
            // ensure debug has the development URL (emulator host)
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        // Enable BuildConfig fields declared via buildConfigField
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit & Moshi for network service adapter (use version catalog)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    // Coil for image loading in Compose
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Test dependencies: MockWebServer and coroutines test
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    // MockK for unit tests
    testImplementation("io.mockk:mockk:1.13.5")
    // AndroidX Arch testing (InstantTaskExecutorRule) to allow LiveData/ViewModel unit tests on JVM
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}