package com.aytngr.build_logic.convention
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidApplicationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                compileSdk = 37

                defaultConfig {
                    applicationId = "com.aytngr.collect"
                    minSdk = 26
                    targetSdk = 36
                    versionCode = 1
                    versionName = "1.0"
                    testInstrumentationRunner =
                        "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile(
                                "proguard-android-optimize.txt"
                            ),
                            "proguard-rules.pro"
                        )
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                lint {
                    checkDependencies = true
                    abortOnError = true
                    baseline = file("lint-baseline.xml")
                }
            }
        }
    }
}