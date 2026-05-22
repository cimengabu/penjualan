buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")

    classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
    }
}

plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}