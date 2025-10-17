plugins {
    id("com.android.application") version "8.10.1" apply false
    kotlin("android") version "1.9.22" apply false
    kotlin("kapt") version "1.9.22" apply false
}

buildscript {
    dependencies {
        classpath ("com.google.gms:google-services:4.4.4") // o la versión más reciente
    }
}