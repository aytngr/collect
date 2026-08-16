plugins {
    id("collect.android.library")
    id("collect.android.compose")
    id("collect.android.hilt")
}

android {
    namespace = "com.aytngr.feature.home"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
}
