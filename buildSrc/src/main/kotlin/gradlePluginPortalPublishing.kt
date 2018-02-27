import com.gradle.publish.PluginBundleExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

private fun Any?.ifValuePresent() = this?.toString()?.takeUnless { it.isEmpty() }

fun Project.publishToGradlePluginPortal() {
    apply { plugin("com.gradle.plugin-publish") }

    // Take the JAR from either a project-specific property '%project.name%-jar' or from
    // the Maven-like layout passed as the 'prebuiltMavenRepo' property:
    val prebuiltJarPath: String? =
        project.findProperty("${project.name}-jar").ifValuePresent() ?:
        // otherwise use the 'prebuiltJar' property as a Maven repo root:
        project.findProperty("prebuiltMavenRepo").ifValuePresent()?.let { prebuiltJarsDir ->
            val subfolder = project.group.toString().replace(".", "/") + "/" + project.name + "/" + version
            val jarName = "${project.name}-$version.jar"
            "$prebuiltJarsDir/$subfolder/$jarName"
        }

    if (prebuiltJarPath != null) {
        println("Dropping archives and using pre-built artifact for ${project.name}: $prebuiltJarPath")
        configurations.remove(configurations.getByName("archives"))
        val archives = configurations.create("archives")
        archives.artifacts.removeAll { it !is org.gradle.plugins.signing.Signature }
        artifacts.add("archives", file(prebuiltJarPath)) {
            name = project.name
        }
    }

    findProperty("publishPluginsVersion")?.let { publishPluginsVersion ->
        configurations.getByName("archives").artifacts.all {
            version = publishPluginsVersion
        }
    }

    tasks.getByName("publishPlugins").doFirst {
        val kotlinVersion = property("kotlinVersion").toString()
        require(!kotlinVersion.endsWith("SNAPSHOT")) {
            "Kotlin version is a snapshot version $kotlinVersion. Snapshot versions should not be published."
        }
    }

    the<PluginBundleExtension>().apply {
        website = "https://kotlinlang.org/"
        vcsUrl = "https://github.com/JetBrains/kotlin/"
        description = "Kotlin plugins for Gradle"
        tags = listOf("kotlin")

        mavenCoordinates {
            groupId = "org.jetbrains.kotlin"
        }
    }
}
