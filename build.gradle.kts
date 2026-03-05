plugins {
    id("net.neoforged.moddev") version "2.0.80"
    id("com.almostreliable.almostgradle") version "1.5.0"
}

almostgradle.setup {
    withSourcesJar = false
    testMod = true
    dataGen = "src/main/generated"
}

neoForge {
    runs {
        configureEach {
            systemProperties = mapOf(
                "guideme.${almostgradle.modId}.guide.sources" to file("guidebook").absolutePath,
                "guideme.${almostgradle.modId}.guide.sourcesNamespace" to almostgradle.modId,
            )
        }

        create("guide") {
            client()
            systemProperty("guideme.showOnStartup", "${almostgradle.modId}:guide!${almostgradle.modId}:${almostgradle.modId}.md")
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

tasks.withType<Jar> {
    from("guidebook") {
        into("assets/guides/${almostgradle.modId}/guide")
    }
}
