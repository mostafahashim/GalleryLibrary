# GalleryLibrary
<img src="https://github.com/mostafahashim/GalleryLibrary/blob/main/tutorial/features.png"/>


[![](https://jitpack.io/v/mostafahashim/GalleryLibrary.svg)](https://jitpack.io/#mostafahashim/GalleryLibrary)

# Demo

<p align="left">
<img src="https://github.com/mostafahashim/GalleryLibrary/blob/main/tutorial/video_1.gif" width="300" height="600"/>
<img src="https://github.com/mostafahashim/GalleryLibrary/blob/main/tutorial/video_2.gif" width="300" height="600"/>
</p>

<p align="left">
<img src="https://github.com/mostafahashim/GalleryLibrary/blob/main/tutorial/video_3.gif" width="300" height="600"/>
<img src="https://github.com/mostafahashim/GalleryLibrary/blob/main/tutorial/video_4.gif" width="300" height="600"/>
</p>

# Setup

### Step 1. Add the JitPack repository to your build file

### Add it in your root build.gradle at the end of repositories or settings.gradle:

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

implementation 'com.github.mostafahashim:GalleryLibrary:{current_version}'

```

# Usage

```kotlin
fun openGallery() {
    val galleryModels = ArrayList<GalleryModel>()
    galleryModels.addAll(binding.viewModel!!.galleryModels)

    GalleryLib(this).showGallery(
        isDialog = binding.viewModel?.isDialog?.value ?: false,
        isOpenEdit = binding.viewModel?.isOpenEdit?.value ?: false,
        selectionType = if (binding.viewModel?.isShowImages?.value!! && binding.viewModel?.isShowVideos?.value!!) GalleryConstants.GalleryTypeImagesAndVideos
        else if (binding.viewModel?.isShowImages?.value!!) GalleryConstants.GalleryTypeImages
        else if (binding.viewModel?.isShowVideos?.value!!) GalleryConstants.GalleryTypeVideos
        else GalleryConstants.GalleryTypeImagesAndVideos,
        //locale only for activity view, dialog view will work on application locale
        locale = if (binding.viewModel?.isRTL?.value!!) "ar" else "en",
        maxSelectionCount = if (binding.viewModel?.count?.value?.isNotEmpty() == true) binding.viewModel?.count?.value?.toInt()!!
        else 1,
        gridColumnsCount = if (binding.viewModel?.columns?.value?.isNotEmpty() == true) binding.viewModel?.columns?.value?.toInt()!!
        else 4,
        selected = galleryModels,
        onResultCallback = object : OnResultCallback {
            override fun onResult(list: ArrayList<GalleryModel>) {
                binding.viewModel?.galleryModels = list
                binding.viewModel?.recyclerGalleryAdapter?.setList(
                    binding.viewModel?.galleryModels ?: ArrayList()
                )
            }
            override fun onDismiss() {

            }
        },
        galleryResultLauncher = galleryResultLauncher
    )
}

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