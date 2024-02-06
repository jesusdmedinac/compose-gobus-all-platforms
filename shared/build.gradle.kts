plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.realmKotlin)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
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
            api(libs.kotlinx.serialization.json)
            implementation(libs.coroutines.core)
            api(libs.kotlinx.datetime)
            api(libs.koin.core)
            api(libs.koin.core.coroutines)
            api(libs.firebase.auth)
            api(libs.firebase.firestore)
        }
        androidMain.dependencies {
            implementation(libs.coroutines.android)
        }
        jvmMain.dependencies {
            implementation(libs.coroutines.core.jvm)
        }
    }

    jvmToolchain(11)
}

android {
    namespace = "com.jesusdmedinac.gobus.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
