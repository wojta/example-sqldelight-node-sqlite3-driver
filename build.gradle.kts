import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin.Companion.kotlinNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import java.util.*

plugins {
    kotlin("multiplatform") version "2.1.20-Beta1"
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
    mavenLocal()
    mavenCentral()
}

kotlin {
    js {
        useCommonJs()
        binaries.executable()
        nodejs()
    }

    sourceSets {
        val commonMain by getting {}
        val jsMain by getting {
            dependencies {
                implementation("cz.sazel.sqldelight:node-sqlite3-driver-js:0.4.1")
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
        val sqlite3moduleDir = layout.buildDirectory.get().dir("js/node_modules/sqlite3").asFile
        if (!sqlite3moduleDir.resolve("build").exists()) {
            exec {
                workingDir = sqlite3moduleDir
                val yarnExecutable = yarn.environment.executable
                val yarnPath = file(yarnExecutable).parent
                val nodePath = file(kotlinNodeJsEnvSpec.executable).parent
                environment(
                    "PATH", System.getenv("PATH") + ":$yarnPath:$nodePath"
                )
                commandLine(yarnExecutable)
            }
        }
    }
}.get()
tasks["kotlinNpmInstall"].finalizedBy(bindingsInstall)

tasks.register("run") {
    dependsOn("cleanDb")
    dependsOn("jsNodeDevelopmentRun")
}

tasks.register("cleanDb") {
    doLast {
        file("build/js/packages/example-sqldelight-node-sqlite3-driver/test.db").delete()
    }
}
