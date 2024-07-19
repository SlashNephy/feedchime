plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-cio:1.6.8")
    implementation("io.ktor:ktor-client-serialization:1.6.8")
    implementation("com.rometools:rome:1.18.0")
    implementation("org.jsoup:jsoup:1.15.2")
    implementation("com.charleskorn.kaml:kaml:0.46.0")

    implementation("org.jetbrains.exposed:exposed-jdbc:0.38.2")
    implementation("org.jetbrains.exposed:exposed-java-time:0.38.2")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")

    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
                apiVersion = "1.6"
                languageVersion = "1.6"
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings {
            progressiveMode = true
            // optIn()
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.feedchime.MainKt")
    }
}
