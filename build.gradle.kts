plugins {
    java
    `java-library`
    id("org.checkerframework") version "0.5.9"
}

group = "com.proximyst"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testImplementation("junit:junit:4.13")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    compileJava {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = sourceCompatibility
    }
}
