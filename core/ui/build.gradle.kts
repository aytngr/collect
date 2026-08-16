plugins {
    id("collect.android.library")
    id("collect.android.compose")
}

android {
    namespace = "com.aytngr.core.ui"
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)

    implementation(project(":domain"))
    implementation(project(":core:designsystem"))
}
