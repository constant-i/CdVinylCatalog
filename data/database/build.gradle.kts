plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kai.cdvinylcatalog.data.database"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro") // создай файл, если его нет
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

// Room экспортирует JSON-описание схемы в schemas/.
// Без него не с чем сравнивать при написании миграции,
// а без миграции изменение схемы стирает коллекцию.
// Файлы schemas/**/*.json коммитятся в репозиторий.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Зависимость от домена
    implementation(project(":domain"))

    // Room
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}