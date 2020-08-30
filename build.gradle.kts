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

    javadoc {
        val opt = options as StandardJavadocDocletOptions
        opt.addStringOption("Xdoclint:none", "-quiet")

        opt.encoding("UTF-8")
        opt.charSet("UTF-8")
        opt.source("8")
        opt.links("https://docs.oracle.com/javase/8/docs/api/")
    }
}
