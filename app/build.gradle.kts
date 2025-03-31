import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.defaultphoneapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.defaultphoneapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keyPropertiesFile =
        file("${project.rootProject.rootDir.absolutePath}/keystore/key.properties")
    val keyProperties = Properties()
    keyProperties.load(FileInputStream(keyPropertiesFile))
    println("Jenkins keyProperties[key_file] = ${keyProperties["release.key_file"]}")
    signingConfigs {
        create("release") {
            keyAlias = keyProperties.getProperty("release.key_alias")
            keyPassword = keyProperties.getProperty("release.key_password")
            storeFile = file(keyProperties.getProperty("release.key_file"))
            storePassword = keyProperties.getProperty("release.store_password")
        }
        getByName("debug") {
            keyAlias = keyProperties.getProperty("debug.key_alias")
            keyPassword = keyProperties.getProperty("debug.key_password")
            storeFile = file(keyProperties.getProperty("debug.key_file"))
            storePassword = keyProperties.getProperty("debug.store_password")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            matchingFallbacks.addAll(listOf("release", "debug"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildTypes {

    }
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}