// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "5.1.0.4882"
}

sonarqube {
    properties {
        property("sonar.projectKey", "misw4203-vinilos-mobile")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.login", "sqp_e9d9029ae920e45648d1c10ba02b80ed36b770c5")

        // Evita escanear artefactos generados
        property("sonar.exclusions", "**/build/**,**/.gradle/**")

    }
}

project(":app") {
    sonarqube {
        properties {
            // Rutas RELATIVAS al módulo :app
            property("sonar.java.binaries",
                "build/tmp/kotlin-classes/debug,build/intermediates/javac/debug/classes")

            property("sonar.coverage.jacoco.xmlReportPaths",
                "build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")

            property("sonar.androidLint.reportPaths",
                "build/reports/lint-results-debug.xml")
        }
    }
}

// Make the :sonar task depend on the :app:jacocoTestReport task
tasks.named("sonar") {
    dependsOn(
        ":app:testDebugUnitTest",
        ":app:jacocoTestReport",
        ":app:lintDebug"
    )
}