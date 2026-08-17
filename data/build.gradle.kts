plugins {
    id("collect.android.library")
    id("collect.android.hilt")
    alias(libs.plugins.room)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.aytngr.data"
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    androidTestImplementation(libs.room.testing)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
}
