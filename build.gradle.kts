// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}

buildscript {
    dependencies {
        classpath("com.huawei.agconnect:agcp:1.6.3.300")
    }
}
