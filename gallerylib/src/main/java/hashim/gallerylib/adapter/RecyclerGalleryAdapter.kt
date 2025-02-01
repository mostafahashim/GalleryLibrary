package hashim.gallerylib.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ItemRecyclerGalleryBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnItemSelectedListener
import hashim.gallerylib.util.GalleryConstants
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class RecyclerGalleryAdapter(
    var columnWidth: Double,
    var maxSelectionCount: Int,
    var filteredGalleryModels: ArrayList<GalleryModel>,
    var onItemSelectedListener: OnItemSelectedListener
) : RecyclerView.Adapter<RecyclerGalleryAdapter.ViewHolder>() {

    private var originalGalleryModels = ArrayList<GalleryModel>()

    init {
        originalGalleryModels = filteredGalleryModels.map { it.copy() }.toCollection(ArrayList())
    }

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            inflater, R.layout.item_recycler_gallery, parent, false
        ) as ItemRecyclerGalleryBinding
        return ViewHolder(
            binding
        )
    }

    override fun getItemCount() = filteredGalleryModels.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        filteredGalleryModels[holder.layoutPosition].columnWidth = columnWidth
        filteredGalleryModels[holder.layoutPosition].columnHeight = columnWidth

        //show counter if selection count > 1
        holder.binding.showCounter = maxSelectionCount > 1

//        if (filteredGalleryModels[holder.layoutPosition].type.compareTo(GalleryConstants.GalleryTypeImages) == 0) {
//            //image type
//            filteredGalleryModels[holder.layoutPosition].url =
//                "file://" + filteredGalleryModels[holder.layoutPosition].sdcardPath
//            filteredGalleryModels[holder.layoutPosition].isVideo = false
//        } else if (filteredGalleryModels[holder.layoutPosition].type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
//            //video type
//            filteredGalleryModels[holder.layoutPosition].url =
//                Uri.fromFile(File(filteredGalleryModels[holder.layoutPosition].sdcardPath))
//                    .toString()
//            filteredGalleryModels[holder.layoutPosition].isVideo = true
//        }
        holder.binding.model = filteredGalleryModels[holder.layoutPosition]
        holder.binding.imgviewPreview.setOnClickListener { imageview ->
            onItemSelectedListener.onImageView(
                holder.layoutPosition,
                holder.binding.ivImage,
                filteredGalleryModels
            )
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
                galleryModelsSelected.add(originalGalleryModels[i].copy())
            }
        }
        return galleryModelsSelected
    }

    fun addAll(files: ArrayList<GalleryModel>) {
        try {
            filteredGalleryModels = ArrayList()
            filteredGalleryModels.addAll(files)
            originalGalleryModels = ArrayList()
            originalGalleryModels =
                filteredGalleryModels.map { it.copy() }.toCollection(ArrayList())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyItemRangeInserted(0, filteredGalleryModels.size)
    }

    fun addItemToTop(file: GalleryModel) {
        try {
            if (originalGalleryModels.isNotEmpty()) originalGalleryModels.add(0, file.copy())
            else {
                originalGalleryModels = ArrayList()
                originalGalleryModels.add(0, file.copy())
            }
            if (filteredGalleryModels.isNotEmpty()) filteredGalleryModels.add(0, file.copy())
            else {
                filteredGalleryModels = ArrayList()
                filteredGalleryModels.add(0, file.copy())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyItemInserted(0)
    }

    fun reduceAllSelectedIndices(currentIndexWhenSelected: Int) {
        for (i in originalGalleryModels.indices) {
            if (originalGalleryModels[i].isSelected
                && originalGalleryModels[i].index_when_selected > currentIndexWhenSelected
            ) {
                originalGalleryModels[i].index_when_selected -= 1
//                notifyItemChanged(i)
            }
        }
        for (i in filteredGalleryModels.indices) {
            if (filteredGalleryModels[i].isSelected
                && filteredGalleryModels[i].index_when_selected > currentIndexWhenSelected
            ) {
                filteredGalleryModels[i].index_when_selected -= 1
                notifyItemChanged(i)
            }
        }
    }

    fun getIndexFromOriginalList(galleryModel: GalleryModel): Int {
        for (i in originalGalleryModels.indices) {
            if (galleryModel.itemUrI.compareTo(originalGalleryModels[i].itemUrI) == 0
                || galleryModel.url.compareTo(originalGalleryModels[i].url) == 0
            )
                return i
        }
        return -1
    }

    private fun changeSelection(position: Int) {
        //get index in original before edit in item
        val indexInOriginal = getIndexFromOriginalList(filteredGalleryModels[position])
        if (filteredGalleryModels[position].isSelected) {
            //reduce items index that selected after this item
            if (maxSelectionCount > 1)
                reduceAllSelectedIndices(
                    filteredGalleryModels[position].index_when_selected
                )
            //deselect item
            filteredGalleryModels[position].isSelected = false
            filteredGalleryModels[position].index_when_selected = 0
            //change selection in original list
            if (indexInOriginal != -1)
                originalGalleryModels[indexInOriginal] = filteredGalleryModels[position].copy()
            //update view
            notifyItemChanged(position)
        } else {
            if (maxSelectionCount == 1) {
                // one item is selected before
                deselectAll()
                filteredGalleryModels[position].isSelected = true
                filteredGalleryModels[position].index_when_selected = getSelected().size + 1
                //change selection in original list
                if (indexInOriginal != -1)
                    originalGalleryModels[indexInOriginal] = filteredGalleryModels[position].copy()
                //update view
                notifyItemChanged(position)
            } else if (getSelected().size < maxSelectionCount) {
                filteredGalleryModels[position].isSelected = true
                //set item selection index
                filteredGalleryModels[position].index_when_selected = getSelected().size + 1
                //change selection in original list
                if (indexInOriginal != -1)
                    originalGalleryModels[indexInOriginal] = filteredGalleryModels[position].copy()
                //update view
                notifyItemChanged(position)
            } else {
                Toast.makeText(
                    context, context.getString(
                        R.string.you_may_not_select_more_than_s_item, maxSelectionCount.toString()
                    ), Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun deselectAll() {
        for (i in filteredGalleryModels.indices) {
            if (filteredGalleryModels[i].isSelected) {
                filteredGalleryModels[i].index_when_selected = 0
                filteredGalleryModels[i].isSelected = false
                notifyItemChanged(i)
            }
        }
        for (i in originalGalleryModels.indices) {
            if (originalGalleryModels[i].isSelected) {
                originalGalleryModels[i].isSelected = false
                originalGalleryModels[i].index_when_selected = 0
            }
        }
    }

    // Filter Class
    fun filter(charText: String) {
        var albumName = charText
        albumName = albumName.lowercase(Locale.getDefault())
        val size = filteredGalleryModels.size
        filteredGalleryModels.clear()
        notifyItemRangeRemoved(0, size - 1)
        if (albumName.isEmpty()) {
            filteredGalleryModels =
                originalGalleryModels.map { it.copy() }.toCollection(ArrayList())
        } else {
            for (galleryModel in originalGalleryModels) {
                if (galleryModel.albumName.lowercase(Locale.getDefault()).contains(albumName)) {
                    filteredGalleryModels.add(galleryModel.copy())
                }
            }
        }
        notifyItemRangeInserted(0, filteredGalleryModels.size - 1)
    }

    fun setColumnWidthAndRatio(columnWidth: Double) {
        this.columnWidth = columnWidth
    }

    class ViewHolder(var binding: ItemRecyclerGalleryBinding) :
        RecyclerView.ViewHolder(binding.root)

}