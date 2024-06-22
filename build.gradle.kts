import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"

}

group = "ru.nineteam"
version = "1.2-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name= "arim-mvn-agpl3"
        url = uri("https://mvn-repo.arim.space/affero-gpl3/")
    }
    maven {
        name= "arim-mvn-lgpl3"
        url = uri("https://mvn-repo.arim.space/lesser-gpl3/")
    }
    maven {
        name= "arim-mvn-gpl3"
        url = uri("https://mvn-repo.arim.space/gpl3/")
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("VelocityBridge")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "ru.nineteam.TelegramBridge.TelegramBridge"))
        }
    }
}
tasks {
    build {
        dependsOn(shadowJar)
    }
}

dependencies {
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("space.arim.libertybans:bans-api:1.1.0-M3")

    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

