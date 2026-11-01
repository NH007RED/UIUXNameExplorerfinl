import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.nh.uiuxexplorer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nh.uiuxexplorer"
        minSdk = 26
        targetSdk = 34
        versionCode = 19
        versionName = "19.0.0"

        // Ultra Compressor: keep only English resources from AndroidX/Material libraries
        resourceConfigurations += setOf("en")
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    signingConfigs {
        create("release") {
            val ksFile = file(keystoreProps.getProperty("storeFile", "release-keystore.jks"))
            storeFile = ksFile
            storePassword = keystoreProps.getProperty("storePassword", "NHExplorer@2026")
            keyAlias = keystoreProps.getProperty("keyAlias", "nh_uiux_release")
            keyPassword = keystoreProps.getProperty("keyPassword", "NHExplorer@2026")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        debug {
            // Ultra Compressor enabled on debug APK as well so assembleDebug produces a slim ~3 MB APK
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            val relSigning = signingConfigs.getByName("release")
            if (relSigning.storeFile?.exists() == true) {
                signingConfig = relSigning
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            // Ultra Compressor: R8 code shrinking + unused icon stripping + resource shrinking + PNG crunching
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = true
            val relSigning = signingConfigs.getByName("release")
            if (relSigning.storeFile?.exists() == true) {
                signingConfig = relSigning
            }
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
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = false
    }
    packaging {
        jniLibs {
            // Ultra Compressor: DEFLATE-compress native libraries inside the APK
            useLegacyPackaging = true
        }
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/*.version",
                "META-INF/*.kotlin_module",
                "META-INF/kotlin-project-structure-metadata.json",
                "META-INF/proguard/*",
                "META-INF/maven/**",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
                "**/*.kotlin_builtins"
            )
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.2")

    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
