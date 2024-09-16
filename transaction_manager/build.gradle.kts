plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "ru.barinov.transaction_manager"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(project(":file_works:write_worker"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    implementation(project(":core"))
    implementation(project(":cryptography"))
    implementation(project(":file_works"))
}