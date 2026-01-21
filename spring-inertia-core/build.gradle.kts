plugins {
    kotlin("jvm")
}

description = "Inertia.js Spring adapter - Core (no Spring dependencies)"

dependencies {
    // No dependencies - pure Kotlin

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
