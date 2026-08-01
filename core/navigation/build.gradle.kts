plugins {
    id("collect.android.library")
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.example.core.navigation"
}

dependencies {
    implementation(libs.androidx.compose.navigation)
    implementation(libs.kotlinx.serialization.json)
}
