plugins {
    id("collect.android.library")
    id("collect.android.hilt")
}

android {
    namespace = "com.aytngr.core.common"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
