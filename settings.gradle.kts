pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/abbhasin/audiobook-models")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/abbhasin/audiobook-models")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        google()
        mavenCentral()
        jcenter()
    }
}

rootProject.name = "audiobook"
include(":app")
 