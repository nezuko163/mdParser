plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.nezuko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
        events("passed", "failed", "skipped") // можно добавить "standard_out", "standard_error"
    }
}

kotlin {
    jvmToolchain(21)
}