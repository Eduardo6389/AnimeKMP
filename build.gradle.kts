plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.sqldelight) apply false
}

tasks.register("ktlintCheck") {
    dependsOn(":shared:ktlintCheck", ":androidApp:ktlintCheck")
}

tasks.register("detekt") {
    dependsOn(":shared:detekt", ":androidApp:detekt")
}
