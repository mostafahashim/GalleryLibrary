package hashim.gallerylib.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ItemRecyclerGalleryBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnItemSelectedListener
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.view.galleryActivity.GalleryActivity
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RecyclerGalleryAdapter(
    var columnWidth: Double,
    var selectedType: String, var maxSelectionCount: Int,
    var filteredGalleryModels: ArrayList<GalleryModel>,
    var onItemSelectedListener: OnItemSelectedListener
) : RecyclerView.Adapter<RecyclerGalleryAdapter.ViewHolder>() {

    var originalGalleryModels: ArrayList<GalleryModel> = ArrayList()

    init {
        originalGalleryModels = ArrayList()
        originalGalleryModels.addAll(filteredGalleryModels)
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_recycler_gallery,
            parent,
            false
        ) as ItemRecyclerGalleryBinding
        return ViewHolder(
            binding
        )
    }

    override fun getItemCount() = filteredGalleryModels.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (columnWidth != 0.0) {
            holder.binding.galleryItemContainer.layoutParams.height = columnWidth.toInt()
            holder.binding.galleryItemContainer.layoutParams.width = columnWidth.toInt()
            holder.binding.galleryItemContainer.requestLayout()
        }
        var galleryModel = filteredGalleryModels[holder.layoutPosition]
        try {
            if (galleryModel.type.compareTo(GalleryConstants.GalleryTypeImages) == 0) {
                Glide.with(context).load("file://" + galleryModel.sdcardPath)
                    .apply(
                        RequestOptions.bitmapTransform(
                            MultiTransformation(
                                CenterCrop(),
                                RoundedCornersTransformation(
                                    10, 0,
                                    RoundedCornersTransformation.CornerType.ALL
                                )
                            )
                        ).diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(holder.binding.imgQueueMultiSelected)
                holder.binding.imgQueueMultiSelectedThumbnail.visibility = View.INVISIBLE
            } else if (galleryModel.type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
                Glide.with(context).load(Uri.fromFile(File(galleryModel.sdcardPath)))
                    .apply(
                        RequestOptions.bitmapTransform(
                            MultiTransformation(
                                CenterCrop(),
                                RoundedCornersTransformation(
                                    20, 0,
                                    RoundedCornersTransformation.CornerType.ALL
                                )
                            )
                        ).diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .thumbnail(.1f)
                    .into(holder.binding.imgQueueMultiSelected)
                holder.binding.imgQueueMultiSelectedThumbnail.visibility = View.VISIBLE
            }
            if (galleryModel.isSelected) {
                holder.binding.galleryItemContainer.setPadding(3, 3, 3, 3)
                holder.binding.galleryItemContainer
                    .setBackgroundResource(R.drawable.round_courner_with_bg_color_primary_with_stroke)
                holder.binding.imgviewCounter.visibility = View.VISIBLE
            } else {
                holder.binding.galleryItemContainer
                    .setBackgroundResource(R.drawable.round_courner_with_bg_white_stroke_gray)
                holder.binding.galleryItemContainer.setPadding(1, 1, 1, 1)
                holder.binding.imgviewCounter.visibility = View.GONE
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        holder.binding.galleryItemContainer.setOnClickListener {
            changeSelection(holder.layoutPosition)
            onItemSelectedListener.onItemSelectedListener(0)
        }
    }

    fun getSelected(): ArrayList<GalleryModel> {
        val galleryModelsSelected = ArrayList<GalleryModel>()
        for (i in originalGalleryModels.indices) {
            if (originalGalleryModels[i].isSelected) {
                galleryModelsSelected.add(originalGalleryModels[i])
            }
        }
        return galleryModelsSelected
    }

    fun addAll(files: ArrayList<GalleryModel>) {
        try {
            originalGalleryModels = ArrayList()
            originalGalleryModels.addAll(files)
            filteredGalleryModels = ArrayList()
            filteredGalleryModels.addAll(files)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyDataSetChanged()
    }

    fun addItemToTop(file: GalleryModel) {
        try {
            if (originalGalleryModels != null && originalGalleryModels.isNotEmpty())
                originalGalleryModels.add(0, file)
            else {
                originalGalleryModels = ArrayList()
                originalGalleryModels.add(0, file)
            }
            if (filteredGalleryModels != null && filteredGalleryModels.isNotEmpty())
                filteredGalleryModels.add(0, file)
            else {
                filteredGalleryModels = ArrayList()
                filteredGalleryModels.add(0, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyDataSetChanged()
    }

    private fun changeSelection(position: Int) {
        //get index in original before edit in item
        var indexInOriginal = originalGalleryModels.indexOf(filteredGalleryModels[position])
        if (filteredGalleryModels[position].isSelected) {
            //deselect item
            filteredGalleryModels[position].isSelected = false
            filteredGalleryModels[position].index_when_selected = 0
            //change selection in original list
            originalGalleryModels[indexInOriginal] = filteredGalleryModels[position]
            if (getSelected().isEmpty()) {
                selectedType = ""
            }
        } else {
            if (maxSelectionCount == 1) {
                // one item is selected before
                deselectAll()
                filteredGalleryModels[position].isSelected = true
                filteredGalleryModels[position].index_when_selected = getSelected().size + 1
                selectedType = filteredGalleryModels[position].type
                //change selection in original list
                originalGalleryModels[indexInOriginal] = filteredGalleryModels[position]
            } else if (getSelected().size < maxSelectionCount) {
                if (filteredGalleryModels[position].type.compareTo(GalleryConstants.GalleryTypeImages) == 0) {
                    if (selectedType == GalleryConstants.GalleryTypeImages || selectedType == "") {
                        filteredGalleryModels[position].isSelected = true
                        //set item selection index
                        filteredGalleryModels[position].index_when_selected = getSelected().size + 1
                        selectedType = filteredGalleryModels[position].type
                        //change selection in original list
                        originalGalleryModels[indexInOriginal] = filteredGalleryModels[position]
                    } else {
                        // video is selected before
                        Toast.makeText(
                            context,
                            R.string.it_is_not_possible_to_select_photos_and_videos_at_the_same_time,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else if (filteredGalleryModels[position].type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
                    if (selectedType == GalleryConstants.GalleryTypeVideos || selectedType == "") {
                        if (getSelected().size == 0) {
                            filteredGalleryModels[position].isSelected = true
                            //set item selection index
                            filteredGalleryModels[position].index_when_selected =
                                getSelected().size + 1
                            selectedType = filteredGalleryModels[position].type
                        } else {
                            // one video is selected before
                            deselectAll()
                            filteredGalleryModels[position].isSelected = true
                            //set item selection index
                            filteredGalleryModels[position].index_when_selected =
                                getSelected().size + 1
                            selectedType = filteredGalleryModels[position].type
                        }
                        //change selection in original list
                        originalGalleryModels[indexInOriginal] = filteredGalleryModels[position]
                    } else {
                        // Photos is selected before
                        Toast.makeText(
                            context,
                            R.string.it_is_not_possible_to_select_photos_and_videos_at_the_same_time,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    "${context.getString(R.string.you_may_not_select_more_than)} $maxSelectionCount ${
                        context.getString(
                            R.string.item
                        )
                    }",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        notifyDataSetChanged()
    }

    fun deselectAll() {
        for (i in originalGalleryModels.indices) {
            originalGalleryModels[i].index_when_selected = 0
            originalGalleryModels[i].isSelected = false
        }
        for (i in filteredGalleryModels.indices) {
            filteredGalleryModels[i].index_when_selected = 0
            filteredGalleryModels[i].isSelected = false
        }
    }

    // Filter Class
    fun filter(charText: String) {
        var charText = charText
        charText = charText.lowercase(Locale.getDefault())
        filteredGalleryModels.clear()
        if (charText.isEmpty()) {
            filteredGalleryModels.addAll(originalGalleryModels)
        } else {
            for (galleryModel in originalGalleryModels) {
                if (galleryModel.albumName.lowercase(Locale.getDefault()).contains(charText)
                ) {
                    filteredGalleryModels.add(galleryModel)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setColumnWidthAndRatio(columnWidth: Double, ratio: Double) {
        this.columnWidth = columnWidth
    }

    class ViewHolder(var binding: ItemRecyclerGalleryBinding) :
        RecyclerView.ViewHolder(binding.root)

}