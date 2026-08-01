package com.example.build_logic.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 26
                    testInstrumentationRunner =
                        "androidx.test.runner.AndroidJUnitRunner"

                    consumerProguardFiles("consumer-rules.pro")
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
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }

            dependencies {
                add(
                    "implementation",
                    libs.findLibrary("androidx-core-ktx").get()
                )

                add(
                    "implementation",
                    libs.findLibrary("androidx-appcompat").get()
                )

                add(
                    "implementation",
                    libs.findLibrary("material").get()
                )

                add(
                    "testImplementation",
                    libs.findLibrary("junit").get()
                )

                add(
                    "androidTestImplementation",
                    libs.findLibrary("androidx-junit").get()
                )

                add(
                    "androidTestImplementation",
                    libs.findLibrary("androidx-espresso-core").get()
                )
            }
        }
    }
}