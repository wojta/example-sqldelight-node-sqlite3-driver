import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.Companion.kotlinNodeJsExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import java.util.*

plugins {
    kotlin("multiplatform") version "2.0.20-Beta1"
    id("app.cash.sqldelight") version "2.0.2"
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

kotlin {
    js(IR) {
        useCommonJs()
        binaries.executable()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {}
        val jsMain by getting {
            dependencies {
                implementation("cz.sazel.sqldelight:node-sqlite3-driver-js:0.3.2")
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
                val yarnPath="${yarn.yarnSetupTaskProvider.get().destination.absolutePath}/bin"
                val nodePath="${kotlinNodeJsExtension.nodeJsSetupTaskProvider.get().destination.absolutePath}/bin"
                environment(
                    "PATH",
                    System.getenv("PATH") + ":$yarnPath:$nodePath"
                )
                var commandLine = "$yarnPath/yarn"
                commandLine(commandLine)
            }
        }
    }
}.get()
tasks["kotlinNpmInstall"].finalizedBy(bindingsInstall)

tasks.register("cleanDb") {
    doLast {
        file("build/js/packages/example-sqldelight-node-sqlite3-driver/test.db").delete()
    }
}
