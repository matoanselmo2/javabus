plugins {
    id("java")
    id("maven-publish")
}

group = "me.mato"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            val owner = (findProperty("gpr.owner") as String?)
                ?: System.getenv("GITHUB_REPOSITORY_OWNER")
                ?: throw GradleException("Missing GitHub owner. Set gpr.owner or GITHUB_REPOSITORY_OWNER.")
            val repo = (findProperty("gpr.repo") as String?)
                ?: System.getenv("GITHUB_REPOSITORY")?.substringAfter('/')
                ?: rootProject.name

            url = uri("https://maven.pkg.github.com/$owner/$repo")
            credentials {
                username = (findProperty("gpr.user") as String?)
                    ?: System.getenv("GITHUB_ACTOR")
                    ?: System.getenv("GITHUB_USERNAME")
                    ?: throw GradleException("Missing GitHub username. Set gpr.user or GITHUB_ACTOR/GITHUB_USERNAME.")
                password = (findProperty("gpr.key") as String?)
                    ?: System.getenv("GITHUB_TOKEN")
                    ?: throw GradleException("Missing GitHub token. Set gpr.key or GITHUB_TOKEN.")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
