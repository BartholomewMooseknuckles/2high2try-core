plugins {
    application
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    implementation("net.dv8tion:JDA:5.6.1") {
        exclude(module = "opus-java")
    }
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("org.yaml:snakeyaml:2.3")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

application {
    mainClass.set("com.twohigh.testbot.TestBotMain")
}

tasks.shadowJar {
    archiveBaseName.set("2high2try-testbot")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
