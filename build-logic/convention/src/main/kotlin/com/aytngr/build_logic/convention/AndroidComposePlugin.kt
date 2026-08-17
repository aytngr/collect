package com.aytngr.build_logic.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            pluginManager.withPlugin("com.android.application") {
                extensions.configure(ApplicationExtension::class.java) {
                    buildFeatures {
                        compose = true
                    }
                }
                configureComposeDependencies()
            }

            pluginManager.withPlugin("com.android.library") {
                extensions.configure(LibraryExtension::class.java) {
                    buildFeatures {
                        compose = true
                    }
                }
                configureComposeDependencies()
            }
        }
    }

//    private fun configureComposeExtension(extension: CommonExtension<*, *, *, *, *, *>) {
//        extension.buildFeatures {
//            compose = true
//        }
//    }

    private fun Project.configureComposeDependencies() {
        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("implementation", libs.findLibrary("androidx-compose-ui").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling").get())
            add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
            add("implementation", libs.findLibrary("androidx-compose-material3").get())
            add("implementation", libs.findLibrary("androidx-activity-compose").get())
            add("implementation", libs.findLibrary("androidx-compose-navigation").get())
            add("implementation", libs.findLibrary("compose-material-icons").get())
        }
    }
}