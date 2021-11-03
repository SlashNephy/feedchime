plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

object Versions {
    const val Ktor = "1.6.5"
    const val Rome = "1.15.0"
    const val Jsoup = "1.14.3"
    const val kaml = "0.34.0"

    const val Exposed = "0.32.1"
    const val SQLiteJDBC = "3.30.1"

    const val KotlinLogging = "2.0.11"
    const val Logback = "1.2.3"
}

object Libraries {
    const val KtorClientCIO = "io.ktor:ktor-client-cio:${Versions.Ktor}"
    const val KtorClientSerialization = "io.ktor:ktor-client-serialization:${Versions.Ktor}"
    const val Rome = "com.rometools:rome:${Versions.Rome}"
    const val Jsoup = "org.jsoup:jsoup:${Versions.Jsoup}"
    const val kaml = "com.charleskorn.kaml:kaml:${Versions.kaml}"

    const val ExposedCore = "org.jetbrains.exposed:exposed-core:${Versions.Exposed}"
    const val ExposedJDBC = "org.jetbrains.exposed:exposed-jdbc:${Versions.Exposed}"
    const val SqliteJDBC = "org.xerial:sqlite-jdbc:${Versions.SQLiteJDBC}"

    const val KotlinLogging = "io.github.microutils:kotlin-logging:${Versions.KotlinLogging}"
    const val LogbackCore = "ch.qos.logback:logback-core:${Versions.Logback}"
    const val LogbackClassic = "ch.qos.logback:logback-classic:${Versions.Logback}"

    val ExperimentalAnnotations = setOf(
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
        "kotlin.io.path.ExperimentalPathApi",
        "kotlin.time.ExperimentalTime",
        "kotlin.ExperimentalStdlibApi",
        "kotlinx.coroutines.FlowPreview"
    )
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(Libraries.KtorClientCIO)
    implementation(Libraries.KtorClientSerialization)
    implementation(Libraries.Rome)
    implementation(Libraries.Jsoup)
    implementation(Libraries.kaml)

    implementation(Libraries.ExposedCore)
    implementation(Libraries.ExposedJDBC)
    implementation(Libraries.SqliteJDBC)

    implementation(Libraries.KotlinLogging)
    implementation(Libraries.LogbackCore)
    implementation(Libraries.LogbackClassic)
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_11.toString()
                apiVersion = "1.5"
                languageVersion = "1.5"
                allWarningsAsErrors = true
                verbose = true
            }
        }
    }

    sourceSets.all {
        languageSettings.progressiveMode = true

        Libraries.ExperimentalAnnotations.forEach {
            languageSettings.optIn(it)
        }
    }
}

task<JavaExec>("run") {
    dependsOn("build")

    group = "application"
    mainClass.set("blue.starry.feedchime.MainKt")
    classpath(configurations.runtimeClasspath, tasks.jar)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.feedchime.MainKt")
    }
}
