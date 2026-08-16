plugins {
    id("collect.android.library")
    id("collect.android.hilt")
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.aytngr.domain"
}

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)

    implementation(libs.kotlinx.serialization.json)
}
