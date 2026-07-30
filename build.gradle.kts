plugins {
    id("net.neoforged.moddev") version "2.0.141"
    id("com.almostreliable.almostgradle") version "2.2.0"
}

almostgradle.setup {
    withSourcesJar = false
    dataGen = "src/main/generated"
    tests {
        testMod = true
        gameTests = true
        testFramework = true
    }
}

neoForge {
    runs {
        configureEach {
            systemProperties.putAll(
                mapOf(
                    "guideme.${almostgradle.modId}.guide.sources" to file("guidebook").absolutePath,
                    "guideme.${almostgradle.modId}.guide.sourcesNamespace" to almostgradle.modId,
                )
            )
        }

        create("guide") {
            client()
            systemProperty("guideme.showOnStartup", "${almostgradle.modId}:guide")
        }

        named("datagen_server") {
            programArguments.add("--uncached")
        }
    }
}

repositories {
    // CC: Tweaked
    maven("https://maven.squiddev.cc/")
}

dependencies {
    // CC: Tweaked
    compileOnly("cc.tweaked:cc-tweaked-${almostgradle.minecraftVersion}-common-api:${almostgradle.getProperty("cctVersion")}")
    // GuideME
    runtimeOnly("org.appliedenergistics:guideme:${almostgradle.getProperty("guideMeVersion")}")
}

tasks.test {
    failOnNoDiscoveredTests = false
}

tasks.withType<Jar> {
    from("guidebook") {
        into("assets/${almostgradle.modId}/guides/${almostgradle.modId}/guide")
    }
}
