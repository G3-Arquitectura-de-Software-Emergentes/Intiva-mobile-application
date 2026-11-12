import java.net.URI
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // Hilt plugin for dependency injection
    alias(libs.plugins.hilt)

    // KSP plugin for annotation processing
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.android)

    // Firebase plugin for using Firebase services in the app
    id("com.google.gms.google-services")

    // Firebase plugin for using Firebase services in the app
    id("com.google.firebase.appdistribution")
}

val debugGateway = providers.gradleProperty("intivaDebugGateway")
    .orElse("http://10.0.2.2:8080/api/v1/").get()
val releaseGateway = providers.gradleProperty("intivaGateway")
    .orElse(providers.environmentVariable("INTIVA_GATEWAY_URL"))
    .orElse("https://intiva.invalid/api/v1/").get()

fun gatewayLiteral(value: String, httpsOnly: Boolean): String {
    val uri = URI.create(value)
    require(uri.host != null && uri.userInfo == null && uri.query == null && uri.fragment == null &&
        uri.path == "/api/v1/" && (uri.scheme == "https" || !httpsOnly && uri.scheme == "http")) {
        "Gateway URL must end in /api/v1/; release requires HTTPS."
    }
    return "\"$value\""
}

android {
    namespace = "com.resolum.intiva"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.resolum.intiva"
        minSdk = 26
        targetSdk = 36
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", gatewayLiteral(debugGateway, false))
        }
        create("docker") {
            initWith(getByName("debug"))

            buildConfigField(
                "String",
                "BASE_URL",
                gatewayLiteral(debugGateway, false)
            )

            matchingFallbacks += listOf("debug")
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", gatewayLiteral(releaseGateway, true))

            firebaseAppDistribution {
                // Specifices the release notes file.
                artifactType = "APK"
                // The email address of the person responsible for the release.
                testers = "faridce14@gmail.com, sdiaz4519@gmail.com, didier.sebas80@gmail.com, juarezleonn2000@gmail.com, smithtorresapolinario@gmail.com"
                // Notes for the release.
                releaseNotes = "Testing Build for Intiva App"
            }
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

dependencies {
    // Core dependencies
    implementation(libs.androidx.core.ktx)

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.animation)

    // Retrofit and Gson Converter
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Compose Navigation dependency
    implementation(libs.androidx.navigation.compose)

    // Coil dependency for image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // View Model dependency for usage of view models in the MVVM pattern
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver.v4120)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Hilt dependencies for dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Room dependencies for local data storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Camara X dependencies for camera functionality
    implementation(libs.androidx.camera.camera2)
    // CameraX View dependencies for using CameraX's view-based APIs, such as PreviewView
    implementation(libs.androidx.camera.view)
    // CameraX lifecycle dependencies for managing camera lifecycle
    implementation(libs.androidx.camera.lifecycle)
    // CameraX ML Kit Vision dependencies for integrating ML Kit's vision APIs with CameraX
    implementation(libs.androidx.camera.mlkit.vision)

    // Datastore dependencies for local data storage
    implementation(libs.androidx.datastore.preferences)

    // Material Icons dependency for using Material Design icons in the app
    implementation(libs.androidx.compose.material.icons.extended)

    // Firebase dependencies for using Firebase services in the app
    implementation(platform(libs.firebase.bom))

    // ML Kit Barcode Scanning dependency for QR code scanning
    implementation(libs.mlkit.barcode.scanning)

    // Guava (required by CameraX ProcessCameraProvider)
    implementation(libs.guava.android)

    // Firebase Cloud Messaging dependency for handling push notifications
    implementation(libs.firebase.messaging)

    // WorkManager dependency for background sync
    implementation(libs.androidx.work.runtime.ktx)
}
