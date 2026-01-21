plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.spring") version "2.3.0" apply false
    `maven-publish`
}

allprojects {
    group = "co.trall"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

configure(subprojects.filter { it.name != "spring-inertia-demoapp" }) {
    apply(plugin = "maven-publish")

    configure<JavaPluginExtension> {
        withSourcesJar()
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/whytrall/spring-inertia")
                credentials {
                    username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                    password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
                }
            }
        }
        publications {
            create<MavenPublication>("gpr") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set(project.description)
                    url.set("https://github.com/whytrall/spring-inertia")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }
                    developers {
                        developer {
                            id.set("whytrall")
                            name.set("Sasha Blashenkov")
                            email.set("git-commits@trall.co")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/whytrall/spring-inertia.git")
                        developerConnection.set("scm:git:ssh://github.com/whytrall/spring-inertia.git")
                        url.set("https://github.com/whytrall/spring-inertia")
                    }
                }
            }
        }
    }
}
