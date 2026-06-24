plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation(project(":api"))

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17")
    compileOnly("net.citizensnpcs:citizens-main:2.0.42-SNAPSHOT") {
        isTransitive = false
    }

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-jdk14:2.0.16")
}

tasks.shadowJar {
    archiveBaseName.set("2high2try-core")
    archiveClassifier.set("")
    minimize {
        exclude(dependency("org.slf4j:.*"))
    }
    relocate("com.zaxxer.hikari", "com.twohigh.libs.hikari")
    relocate("org.slf4j", "com.twohigh.libs.slf4j")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
