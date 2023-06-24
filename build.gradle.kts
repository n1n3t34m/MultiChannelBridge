import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"

}

group = "ru.nineteam"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
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
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

