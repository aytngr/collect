plugins {
    `kotlin-dsl`
}

group = "com.aytngr.collect.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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
            implementationClass = "com.aytngr.build_logic.convention.AndroidApplicationPlugin"
        }
        register("androidLibrary") {
            id = "collect.android.library"
            implementationClass = "com.aytngr.build_logic.convention.AndroidLibraryPlugin"
        }
        register("androidHilt") {
            id = "collect.android.hilt"
            implementationClass = "com.aytngr.build_logic.convention.AndroidHiltPlugin"
        }
        register("androidCompose") {
            id = "collect.android.compose"
            implementationClass = "com.aytngr.build_logic.convention.AndroidComposePlugin"
        }
    }
}