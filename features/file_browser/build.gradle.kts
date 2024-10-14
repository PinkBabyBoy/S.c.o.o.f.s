plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("plugin.serialization") version libs.versions.kotlin
}

android {
    namespace = "ru.barinov.file_browser"
    compileSdk = 34

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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.paging:paging-compose:3.3.2")
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
//    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    implementation(libs.libaums)
    implementation(libs.androidx.ui)
    implementation(libs.compose.appcompanist)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.android.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(project(":features:protected_enter:routes"))
    implementation(project(":file_works:read_worker"))
    implementation(project(":file_process_worker"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(project(":core"))
    implementation(project(":providers:external_data"))
    implementation(project(":providers:internal_data"))
    implementation(project(":cryptography"))
    implementation(project(":permission_manager"))
    implementation(project(":transaction_manager"))
}