package hashim.gallerylib.view.selected

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ActivitySelectedBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.util.dpToPx
import hashim.gallerylib.util.serializable
import hashim.gallerylib.view.GalleryBaseActivity
import hashim.gallerylib.view.crop.CropActivity

class SelectedActivity :
    GalleryBaseActivity(
        R.string.gallery, false, true, true, false,
        false, true, true
    ), SelectedViewModel.Observer {
    lateinit var binding: ActivitySelectedBinding
    override fun doOnCreate(arg0: Bundle?) {
        binding = putContentView(R.layout.activity_selected) as ActivitySelectedBinding
        binding.viewModel = ViewModelProvider(this)[SelectedViewModel::class.java]
        binding.viewModel?.observer = this
        binding.lifecycleOwner = this
        initializeViews()
        setListener()
    }

    override fun initializeViews() {
        binding.viewModel?.selectedPhotos =
            intent.extras?.serializable<ArrayList<GalleryModel>>(GalleryConstants.selected)
                ?: ArrayList()
        initViewPager()
    }

    val pagerAutoScrollHandler = Handler(Looper.getMainLooper())
    private fun initViewPager() {
        binding.viewPagerActivityLanguage.visibility = View.VISIBLE
        binding.viewPagerActivityLanguage.apply {
            clipToPadding = false   // allow full width shown with padding
            clipChildren = false    // allow left/right item is not clipped
            offscreenPageLimit = 1  // make sure left/right item is rendered
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.apply {
                //set items padding, when more padding, item close to other
                // setting padding on inner RecyclerView puts overscroll effect in the right place
                clipToPadding = false
                //adapter
                binding.viewModel?.selectedPagerAdapter =
                    RecyclerSelectedPagerAdapter(
                        binding.viewModel?.selectedPhotos!!
                    )
                adapter = binding.viewModel?.selectedPagerAdapter
            }
            val offsetPx =
                resources.getDimension(R.dimen.padding_0).toInt()
                    .dpToPx(resources.displayMetrics)
            setPadding(0, 0, offsetPx, 0)
            val pageMarginPx =
                resources.getDimension(R.dimen.padding_1).toInt()
                    .dpToPx(resources.displayMetrics)
            val marginTransformer = MarginPageTransformer(pageMarginPx)
            setPageTransformer(marginTransformer)
        }

        binding.viewPagerActivityLanguage.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            var previousPosition = -1
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) pagerAutoScrollHandler.removeMessages(
                    0
                )
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (previousPosition != -1 && binding.viewModel?.selectedPhotos!![previousPosition].type.compareTo(
                        GalleryConstants.GalleryTypeVideos
                    ) == 0
                ) {
                    //release player for
                    binding.viewModel?.selectedPhotos!![previousPosition].isReleasePlayer = true
                    binding.viewModel?.selectedPagerAdapter?.notifyItemChanged(previousPosition)
                }
                if (binding.viewModel?.selectedPhotos!![position].type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
                    binding.viewModel?.selectedPhotos!![position].isReleasePlayer = false
                    binding.viewModel?.selectedPagerAdapter?.notifyItemChanged(position)
                }
                //set current position to previous
                previousPosition = position
                //hide show crop button based on view type
                binding.imgviewCrop.visibility =
                    if (binding.viewModel?.selectedPhotos!![binding.viewPagerActivityLanguage.currentItem].type.compareTo(
                            GalleryConstants.GalleryTypeImages
                        ) == 0
                    ) {
                        View.VISIBLE
                    } else View.GONE
            }
        })
    }

    override fun setListener() {
    }

    override fun cropImage() {
        if (!binding.viewModel?.selectedPhotos.isNullOrEmpty()
            && binding.viewModel?.selectedPhotos!![binding.viewPagerActivityLanguage.currentItem].type.compareTo(
                GalleryConstants.GalleryTypeImages
            ) == 0
        ) {
            Intent(this, CropActivity::class.java).also {
                val galleryModels = ArrayList<GalleryModel>()
                galleryModels.add(binding.viewModel?.selectedPhotos!![binding.viewPagerActivityLanguage.currentItem])
                it.putExtra("GalleryModels", galleryModels)
                it.putExtra("position", binding.viewPagerActivityLanguage.currentItem)
                var locale = "en"
                if (intent.hasExtra(GalleryConstants.Language))
                    locale =
                        intent.getStringExtra(GalleryConstants.Language) ?: GalleryConstants.ENGLISH
                it.putExtra(GalleryConstants.Language, locale)
                cropResultLauncher.launch(it)
            }
        }
    }

    private var cropResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val position = result.data?.extras?.getInt("position", -1) ?: -1
                if (position != -1) {
                    val galleryModels =
                        result.data?.extras?.serializable<ArrayList<GalleryModel>>("GalleryModels")
                            ?: ArrayList()
                    if (galleryModels.isNotEmpty()) {
                        binding.viewModel?.selectedPhotos!![position] = galleryModels[0]
                        binding.viewModel?.selectedPagerAdapter?.notifyItemChanged(position)
                    }
                }
            }

        }

    override fun onBackClicked() {
        finish_activity()
    }

    override fun finishWithSuccess() {
        for (i in binding.viewModel?.selectedPhotos!!.indices) {
            binding.viewModel?.selectedPhotos!![i].player = null
        }
        val intent = Intent()
        intent.putExtra(GalleryConstants.selected, binding.viewModel?.selectedPhotos)
        setResult(RESULT_OK, intent)
        finish_activity()
    }

}