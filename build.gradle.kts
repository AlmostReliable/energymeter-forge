plugins {
    id("net.neoforged.moddev") version "2.0.80"
    id("com.almostreliable.almostgradle") version "1.5.0"
}

almostgradle.setup {
    withSourcesJar = false
    testMod = true
    dataGen = "src/main/generated"
}

repositories {
    // CC: Tweaked
    maven("https://maven.squiddev.cc/")

    mavenLocal()
}

dependencies {
    // CC: Tweaked
    compileOnly("cc.tweaked:cc-tweaked-${almostgradle.minecraftVersion}-common-api:${almostgradle.getProperty("cctVersion")}")
}
