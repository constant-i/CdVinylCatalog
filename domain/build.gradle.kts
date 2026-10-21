plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    // api, а не implementation: Flow и PagingData участвуют в публичных
    // сигнатурах интерфейсов репозиториев, поэтому нужны потребителям :domain.
    api(libs.kotlinx.coroutines.core)
    api(libs.androidx.paging.common)
    // Use cases помечены @Inject; аннотация участвует в их публичном API
    api(libs.javax.inject)
    testImplementation(libs.junit)
}