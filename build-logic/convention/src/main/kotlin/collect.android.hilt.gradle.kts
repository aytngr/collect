import com.example.build_logic.convention.libs
import org.gradle.kotlin.dsl.dependencies

apply(plugin = "com.google.dagger.hilt.android")
apply(plugin = "com.google.devtools.ksp")

dependencies {
    "implementation"(libs.findLibrary("hilt-android").get())
    "ksp"(libs.findLibrary("hilt-compiler").get())
    "implementation"(libs.findLibrary("hilt-navigation-compose").get())
}
