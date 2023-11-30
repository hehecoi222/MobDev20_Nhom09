@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.mobdev20.nhom09.quicknote"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mobdev20.nhom09.quicknote"
        minSdk = 24
        targetSdk = 33
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
    buildFeatures {
        compose = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("net.lingala.zip4j:zip4j:2.9.0")
    implementation("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation(platform("com.google.firebase:firebase-auth:9.4.0"))
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")

    kapt(libs.hilt.android.compiler)
    kapt(libs.kotlinx.metadata.jvm)
    implementation(libs.androidx.datastore.preferences)
    debugImplementation(libs.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

kapt {
    correctErrorTypes = true
}
