plugins {
    kotlin("jvm") version "2.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-network:2.3.12")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}