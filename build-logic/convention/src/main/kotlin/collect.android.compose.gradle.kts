import com.android.build.api.dsl.CommonExtension
import com.example.build_logic.convention.libs
import org.gradle.kotlin.dsl.dependencies

apply(plugin = "org.jetbrains.kotlin.plugin.compose")

val extension = extensions.getByType(CommonExtension::class.java)
extension.buildFeatures.compose = true

dependencies {
    val bom = libs.findLibrary("androidx-compose-bom").get()
    "implementation"(platform(bom))
    "androidTestImplementation"(platform(bom))

    "implementation"(libs.findLibrary("androidx-compose-ui").get())
    "implementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
    "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
    "implementation"(libs.findLibrary("androidx-compose-material3").get())
    "implementation"(libs.findLibrary("androidx-activity-compose").get())
    "implementation"(libs.findLibrary("androidx-compose-navigation").get())
    "implementation"(libs.findLibrary("compose-material-icons").get())
}
