pluginManagement {
    repositories {
        google()
        jcenter()
        mavenCentral()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url =uri("https://maven.google.com")
        }
        maven { url =uri("https://jitpack.io") }
        maven { url =uri("https://maven.google.com") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            url =uri("https://maven.google.com")
        }
        maven { url =uri("https://jitpack.io") }
        maven { url =uri("https://maven.google.com") }
    }
}

rootProject.name = "GalleryApp"
include(":app",":gallerylib")