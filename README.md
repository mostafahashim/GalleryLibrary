# GalleryLibrary
android library for gallery images and videos, and capture camera image and video

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}

 Step 2. Add the dependency
 dependencies {
	        implementation 'com.github.mostafahashim:GalleryLibrary:Tag'
	}
