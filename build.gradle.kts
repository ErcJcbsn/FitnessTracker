// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.4.1" apply false
    // Downgrading Kotlin to a stable 1.9.x version
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    // Matching the KSP version to the Kotlin version
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false
}