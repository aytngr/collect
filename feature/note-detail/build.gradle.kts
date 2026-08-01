plugins {
    id("collect.android.library")
    id("collect.android.compose")
    id("collect.android.hilt")
}

android {
    namespace = "com.example.feature.note_detail"
}

dependencies {
    implementation(libs.coil.compose)

    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
}
