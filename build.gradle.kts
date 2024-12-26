@file: Suppress("PropertyName", "SpellCheckingInspection")

import java.text.SimpleDateFormat
import java.util.*

val minecraft_version: String by project
val mappings_version: String by project
val fabric_loader_version: String by project
val malilib_version: String by project
val mod_menu_version: String by project
val kotlin_loader_version: String by project

plugins {
    kotlin("jvm") version "2.0.21"
    id("fabric-loom") version "1.7-SNAPSHOT"
}

kotlin {
    jvmToolchain(21)
}

repositories {
    maven("https://masa.dy.fi/maven")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://jitpack.io")
    mavenCentral()
}

version = (project.property("mod_version") as String).let {
    if (it.endsWith("-dev")) {
        return@let "$it.${SimpleDateFormat("yyyyMMdd.HHmmss").format(Date())}"
    } else it
}

group = "${project.property("group")}${project.property("mod_id")}"

base {
    archivesName.set("${project.property("mod_file_name")}-${project.property("minecraft_version_out")}")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$mappings_version:v2")
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc:fabric-language-kotlin:$kotlin_loader_version")
    implementation("com.google.code.findbugs:jsr305:3.0.2")

    // modImplementation "fi.dy.masa.malilib:malilib-fabric-${project.minecraft_version_out}:${project.malilib_version}"
    modImplementation("com.github.sakura-ryoko:malilib:$malilib_version")

    // Fabric API. This is technically optional, but you probably want it anyway.
    // include(modApi(fabricApi.module("fabric-api-base", project.fabric_api_version)))

    modCompileOnly("com.terraformersmc:modmenu:$mod_menu_version")
    modCompileOnly(files("libs/EssentialGUI-1.10.1+1.21.jar"))
}

tasks {
    processResources {
        exclude("**/*.xcf")
        exclude("**/xcf")

        val properties = mapOf(
            "mod_version" to project.version,
            "kotlin_loader_version" to kotlin_loader_version,
        )
        inputs.properties(properties)
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    withType<JavaCompile>()
        .configureEach {
            options.encoding = "UTF-8"
            options.release.set(21)
        }

    withType<AbstractArchiveTask>()
        .configureEach {
            isPreserveFileTimestamps = true
        }
}

