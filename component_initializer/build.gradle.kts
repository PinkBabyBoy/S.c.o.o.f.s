plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "ru.barinov.component_initializer"
    compileSdk = 35

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.startup)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(project(":core"))
    implementation(project(":features:protected_enter"))
    implementation(project(":password_manager"))
    implementation(project(":preferences"))
    implementation(project(":cryptography"))
    implementation(project(":usb_connection"))
    implementation(project(":providers:external_data"))
    implementation(project(":permission_manager"))
    implementation(project(":providers:internal_data"))
    implementation(project(":transaction_manager"))
    implementation(project(":features:file_browser"))
    implementation(project(":file_works:read_worker"))
    implementation(project(":file_works:write_worker"))
    implementation(project(":file_process_worker"))
    implementation(project(":data:plain_explorer"))
    implementation(project(":data:crypto_container_explorer"))

}