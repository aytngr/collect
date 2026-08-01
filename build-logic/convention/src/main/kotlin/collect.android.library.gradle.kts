import com.android.build.gradle.LibraryExtension
import com.example.build_logic.convention.libs
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

apply(plugin = "com.android.library")
apply(plugin = "org.jetbrains.kotlin.android")

extensions.configure<LibraryExtension> {
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    "implementation"(libs.findLibrary("androidx-core-ktx").get())
    "implementation"(libs.findLibrary("androidx-appcompat").get())
    "implementation"(libs.findLibrary("material").get())
    "testImplementation"(libs.findLibrary("junit").get())
    "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
    "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
}
