plugins {
    java
}

subprojects {
    apply(plugin = "java-library")

    group = "com.twohigh"
    version = "0.1.0-SNAPSHOT"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://maven.enginehub.org/repo/")
        maven("https://maven.citizensnpcs.co/repo")
        maven("https://repo.extendedclip.com/releases/")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
