plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kai.cdvinylcatalog"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kai.cdvinylcatalog"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // R8 + подпись debug-ключом, чтобы APK ставился на устройство.
            // Для публикации понадобится свой keystore.
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // Debug с включённым R8: те же правила, что на релизе, но с отладчиком
        // и логами. Собирается только по требованию: :app:assembleMinifiedDebug
        create("minifiedDebug") {
            initWith(getByName("debug"))
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks += listOf("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Наши модули
    implementation(project(":domain"))
    implementation(project(":data:network"))
    implementation(project(":data:database"))
    implementation(project(":data:files"))
    implementation(project(":common-ui"))
    implementation(project(":feature:add"))
    implementation(project(":feature:collection"))

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}