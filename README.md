# GalleryLibrary

android library for gallery images and videos, and capture camera image and video

[![](https://jitpack.io/v/mostafahashim/GalleryLibrary.svg)](https://jitpack.io/#mostafahashim/GalleryLibrary)

## Step 1. Add the JitPack repository to your build file
## Add it in your root build.gradle at the end of repositories:
 ```groovy
  dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                mavenCentral()
                maven { url 'https://jitpack.io' }
            }
    }
```

#Step 2. Add the dependency

```groovy

    implementation 'com.github.mostafahashim:GalleryLibrary:1.1.5'

```
# Usage
```kotlin
val galleryModels = ArrayList<GalleryModel>()
        galleryModels.addAll(binding.viewModel!!.galleryModels)

        GalleryLib(this).showGallery(
            isDialog = binding.viewModel?.isDialog?.value ?: false,
            selectionType = GalleryConstants.GalleryTypeImages,
            locale = Preferences.getApplicationLocale(),
            maxSelectionCount = 40,
            selected = galleryModels,
            onResultCallback = object : OnResultCallback {
                override fun onResult(list: ArrayList<GalleryModel>) {
                    binding.viewModel?.galleryModels = list
                    binding.viewModel?.recyclerGalleryAdapter?.setList(
                        binding.viewModel?.galleryModels ?: ArrayList()
                    )
                }
            },
            galleryResultLauncher = galleryResultLauncher
        )

```
and this activity result if you need to launch activity not dialog
```kotlin
val galleryResultLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK
        ) {
            //back from gallery activity
            val galleryModels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.extras?.getParcelableArrayList(
                        GalleryConstants.selected,
                        GalleryModel::class.java
                    ) as ArrayList<GalleryModel>
                } else {
                    result.data?.extras?.get(GalleryConstants.selected) as ArrayList<*>
                }
            if (galleryModels.isNotEmpty()) {
                binding.viewModel?.galleryModels = galleryModels as ArrayList<GalleryModel>
                binding.viewModel?.recyclerGalleryAdapter?.setList(
                    binding.viewModel?.galleryModels ?: ArrayList()
                )
            }
        }
    }
```