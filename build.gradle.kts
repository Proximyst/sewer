import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    java
    `java-library`
    id("org.checkerframework") version "0.5.9"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.5"
}

group = "com.proximyst"
version = "0.4.0"

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

if (System.getenv("BINTRAY_USER") != null) {
    bintray {
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_KEY")

        publish = true
        override = true
        setPublications("maven")

        pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
            repo = "sewer"
            name = "sewer"
            userOrg = "proximyst"
            setLicenses("LGPL-3.0")
            vcsUrl = "https://github.com/Proximyst/sewer"

            version(delegateClosureOf<BintrayExtension.VersionConfig> {
                name = "${rootProject.version}"
            })
        })
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "${rootProject.group}"
                artifactId = rootProject.name
                version = "${rootProject.version}"
                from(components["java"])

                pom {
                    licenses {
                        license {
                            name.set("GNU Lesser Public License, Version 3.0")
                            url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("Proximyst")
                            name.set("Mariell Hoversholm")
                            email.set("proximyst@proximy.st")
                        }
                    }

                    scm {
                        url.set("https://github.com/Proximyst/sewer")
                    }
                }
            }
        }
    }
}
