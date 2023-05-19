import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import java.util.*

plugins {
    kotlin("js") version "1.8.10"
    id("app.cash.sqldelight") version "2.0.0-alpha05"
}

group = "cz.sazel.sqldelight.node.sqlite3"
version = "1.0-SNAPSHOT"

val localProperties = Properties().apply {
    try {
        load(project.rootProject.file("local.properties").inputStream())
    } catch (e: java.io.IOException) {
        System.err.println("Can't read local.properties, skipping")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    js {
        binaries.executable()
        nodejs {
            dependencies {
                implementation("cz.sazel.sqldelight:node-sqlite3-driver-js:0.1.5")
            }
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.example")
            generateAsync.set(true)
        }
    }
}

val bindingsInstall = tasks.register("sqlite3BindingsInstall") {
    doLast {
        val sqlite3moduleDir = buildDir.resolve("js/node_modules/sqlite3")
        if (!sqlite3moduleDir.resolve("lib/binding").exists()) {
            exec {
                workingDir = sqlite3moduleDir
                val commandLine = "${yarn.yarnSetupTaskProvider.get().destination.absolutePath}/bin/yarn"
                commandLine(commandLine)
            }
        }
    }
}.get()
tasks["kotlinNpmInstall"].finalizedBy(bindingsInstall)


