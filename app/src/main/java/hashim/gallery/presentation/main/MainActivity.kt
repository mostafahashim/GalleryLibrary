package hashim.gallery.presentation.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import hashim.gallery.MyApplication
import hashim.gallery.R
import hashim.gallery.databinding.ActivityMainBinding
import hashim.gallery.util.LocaleHelper
import hashim.gallery.util.Preferences
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.view.galleryActivity.GalleryActivity
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), MainViewModel.Observer {

    lateinit var binding: ActivityMainBinding
    var application: MyApplication by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateLocale()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        application = getApplication() as MyApplication
        binding.viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.viewModel?.observer = this
        binding.lifecycleOwner = this
    }

    fun updateLocale() {
        //update activities locale
        if (Preferences.getApplicationLocale().compareTo("ar") == 0) {
            forceRTLIfSupported()
        } else {
            forceLTRIfSupported()
        }
        //Update the locale here before loading the layout to get localized strings for activity.
        LocaleHelper.updateLocale(this)
    }

    private fun forceRTLIfSupported() {
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    private fun forceLTRIfSupported() {
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        super.applyOverrideConfiguration(
            LocaleHelper.applyOverrideConfiguration(
                baseContext,
                overrideConfiguration
            )
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleHelper.updateLocale(newBase)
        )
    }

    override fun openGallery() {
        Intent(this, GalleryActivity::class.java).also {
            it.putExtra(GalleryConstants.maxSelectionCount, 4)
            it.putExtra(GalleryConstants.showType, GalleryConstants.GalleryTypeImages)
            val galleryModels = ArrayList<GalleryModel>()
            galleryModels.addAll(binding.viewModel!!.galleryModels)
            it.putExtra(GalleryConstants.selected, galleryModels)
            it.putExtra(GalleryConstants.Language, Preferences.getApplicationLocale())
            galleryResultLauncher.launch(it)
        }
    }

    private var galleryResultLauncher =
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


}