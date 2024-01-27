plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.realmKotlin)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.realm.base)
            api(libs.realm.sync)
            api(libs.kotlinx.serialization.json)
            implementation(libs.coroutines.core)
            api("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
        }
        androidMain.dependencies {
            implementation(libs.coroutines.android)
        }
        jvmMain.dependencies {
            implementation(libs.coroutines.core.jvm)
        }
    }
}

android {
    namespace = "com.jesusdmedinac.gobus.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
