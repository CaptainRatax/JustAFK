import java.io.DataInputStream
import java.io.File
import java.util.zip.ZipFile
import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    java
}

group = "pt.captainratax"
version = "1.0.1"
description = "A lightweight and configurable AFK plugin for Minecraft servers."

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "spigotmc"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

val legacySpigotApi by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations.named("compileClasspath") {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

configurations.named("testCompileClasspath") {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.84-stable")
    add(legacySpigotApi.name, "org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

val pluginVersion = version.toString()

tasks.processResources {
    filteringCharset = "UTF-8"
    inputs.property("version", pluginVersion)
    filesMatching("plugin.yml") {
        expand("version" to pluginVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(8)
    options.compilerArgs.add("-Xlint:-options")
}

val compileLegacyJava by tasks.registering(JavaCompile::class) {
    description = "Checks the main source against the Spigot 1.8.8 API."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    source = sourceSets.main.get().java
    classpath = legacySpigotApi
    destinationDirectory.set(layout.buildDirectory.dir("classes/java/legacy"))
}

val verifyApiBytecodeParity by tasks.registering {
    description = "Compares the Paper 26.2 and Spigot 1.8.8 class files."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(tasks.compileJava, compileLegacyJava)

    doLast {
        fun classesIn(root: File): Map<String, ByteArray> =
            root.walkTopDown()
                .filter { it.isFile && it.extension == "class" }
                .associate { classFile ->
                    classFile.relativeTo(root).invariantSeparatorsPath to classFile.readBytes()
                }

        val paperClasses = classesIn(
            tasks.compileJava.get().destinationDirectory.get().asFile
        )
        val legacyClasses = classesIn(
            compileLegacyJava.get().destinationDirectory.get().asFile
        )

        check(paperClasses.isNotEmpty()) {
            "The compatibility builds produced no class files."
        }
        check(paperClasses.keys == legacyClasses.keys) {
            "Paper and legacy API builds produced different class sets."
        }

        val differences = paperClasses.keys.filter { className ->
            !paperClasses.getValue(className).contentEquals(
                legacyClasses.getValue(className)
            )
        }
        check(differences.isEmpty()) {
            "Paper and legacy API bytecode differs for: ${differences.joinToString()}"
        }
    }
}

val verifyCompatibility by tasks.registering {
    description = "Checks the JAR metadata and Java 8 bytecode target."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(tasks.jar)

    doLast {
        val archive = tasks.jar.get().archiveFile.get().asFile
        val incompatibleClasses = mutableListOf<String>()
        var classCount = 0

        ZipFile(archive).use { jar ->
            val entries = jar.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.name.endsWith(".class")) {
                    continue
                }
                classCount++

                DataInputStream(jar.getInputStream(entry)).use { classFile ->
                    check(classFile.readInt() == 0xCAFEBABE.toInt()) {
                        "${entry.name} is not a valid class file."
                    }
                    classFile.readUnsignedShort()
                    val majorVersion = classFile.readUnsignedShort()
                    if (majorVersion != 52) {
                        incompatibleClasses.add("${entry.name} (major $majorVersion)")
                    }
                }
            }

            val pluginYmlEntry = checkNotNull(jar.getEntry("plugin.yml")) {
                "plugin.yml is missing from the JAR."
            }
            val pluginYml = jar.getInputStream(pluginYmlEntry)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            check(pluginYml.contains("version: '$pluginVersion'")) {
                "plugin.yml does not contain version $pluginVersion."
            }
            check(pluginYml.contains("api-version: '1.13'")) {
                "plugin.yml does not declare the 1.13 compatibility baseline."
            }
        }

        check(classCount > 0) {
            "The JAR contains no class files."
        }
        check(incompatibleClasses.isEmpty()) {
            "The JAR contains classes that do not target Java 8: ${incompatibleClasses.joinToString()}"
        }
    }
}

val verifyNoRuntimeDependencies by tasks.registering {
    description = "Checks that the plugin declares no runtime dependencies."
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    doLast {
        val runtimeDependencies = configurations.runtimeClasspath.get().allDependencies
        check(runtimeDependencies.isEmpty()) {
            "Unexpected runtime dependencies: ${runtimeDependencies.joinToString()}"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.check {
    dependsOn(
        compileLegacyJava,
        verifyApiBytecodeParity,
        verifyCompatibility,
        verifyNoRuntimeDependencies
    )
}

tasks.jar {
    archiveBaseName.set("JustAFK")
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = pluginVersion
    }
}
