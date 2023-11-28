// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    kotlin("kapt") version "1.9.20" apply false
    kotlin("plugin.serialization") version "1.9.20" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
true // Needed to make the Suppress annotation work for the plugins block