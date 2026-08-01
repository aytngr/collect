plugins {
    `kotlin-dsl`
}

group = "com.example.taskflow.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "collect.android.application"
            implementationClass = "com.example.build_logic.convention.AndroidApplicationPlugin"
        }
        register("androidLibrary") {
            id = "collect.android.library"
            implementationClass = "com.example.build_logic.convention.AndroidLibraryPlugin"
        }
        register("androidHilt") {
            id = "collect.android.hilt"
            implementationClass = "com.example.build_logic.convention.AndroidHiltPlugin"
        }
        register("androidCompose") {
            id = "collect.android.compose"
            implementationClass = "com.example.build_logic.convention.AndroidComposePlugin"
        }
    }
}