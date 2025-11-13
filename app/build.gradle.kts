plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("jacoco")
}

android {
    namespace = "com.miso.vinilo"
    compileSdk {
        version = release(36)
    }

    lint {
        xmlReport = true
        htmlReport = true
        sarifReport = false
        abortOnError = false
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    defaultConfig {
        applicationId = "com.miso.vinilo"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Use the deployed backend URL by default
        buildConfigField("String", "BASE_URL", "\"https://vinyls-backend-8fb6b230b5f0.herokuapp.com/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "BASE_URL", "\"https://vinyls-backend-8fb6b230b5f0.herokuapp.com/\"")
        }
        debug {
            // ensure debug has the development URL (emulator host)
            // Point debug builds at the deployed API as requested
            buildConfigField("String", "BASE_URL", "\"https://vinyls-backend-8fb6b230b5f0.herokuapp.com/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Enable core library desugaring so java.time (and other JDK APIs) are available on older APIs
        isCoreLibraryDesugaringEnabled = true
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
    // Compose LiveData interop (provides observeAsState for LiveData)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.foundation)
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
    ksp(libs.moshi.kotlin.codegen)

    // Coil for image loading in Compose
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Koin for dependency injection
    implementation("io.insert-koin:koin-android:4.1.1")
    implementation("io.insert-koin:koin-androidx-compose:4.1.1")

    // Test dependencies: MockWebServer and coroutines test
    // Use MockWebServer 4.9.3 to match OkHttp 4.x (5.x requires OkHttp 5 runtime which can cause NoClassDefFoundError)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
    // Ensure OkHttp 4.x is available for androidTest runtime
    androidTestImplementation("com.squareup.okhttp3:okhttp:4.9.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    // MockK for unit tests
    testImplementation("io.mockk:mockk:1.14.6")
    // AndroidX Arch testing (InstantTaskExecutorRule) to allow LiveData/ViewModel unit tests on JVM
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    // Koin test helpers
    testImplementation("io.insert-koin:koin-test:4.1.1")
    testImplementation("io.insert-koin:koin-test-junit4:4.1.1")
    // Junit and Espresso config
    testImplementation("junit:junit:4.12")
    androidTestImplementation("com.android.support.test:runner:1.0.1")
    androidTestImplementation("com.android.support.test.espresso:espresso-core:3.0.1")
    // Core library desugaring to support java.time on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Conscrypt to provide an up-to-date TLS provider / truststore on older Android versions
    implementation("org.conscrypt:conscrypt-android:2.5.2")

    // Google Play Services Base (contains ProviderInstaller)
    implementation("com.google.android.gms:play-services-base:18.2.0")
}

// Jacoco Configuration
jacoco {
    toolVersion = "0.8.10"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports { xml.required.set(true); html.required.set(true); csv.required.set(false) }

    val fileFilter = listOf("**/R.class","**/R$*.class","**/BuildConfig.*","**/Manifest*.*","**/*Test*.*","android/**/*.*","**/*$*")
    val ktDebug = fileTree("$buildDir/tmp/kotlin-classes/debug") { exclude(fileFilter) }
    val javaDebug = fileTree("$buildDir/intermediates/javac/debug/classes") { exclude(fileFilter) }
    classDirectories.setFrom(files(ktDebug, javaDebug))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/**/testDebugUnitTest.exec")
    })
}