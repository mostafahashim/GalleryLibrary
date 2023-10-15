package hashim.gallerylib.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
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
        filteredGalleryModels[holder.layoutPosition].columnWidth = columnWidth
        filteredGalleryModels[holder.layoutPosition].columnHeight = columnWidth

        if (filteredGalleryModels[holder.layoutPosition].type.compareTo(GalleryConstants.GalleryTypeImages) == 0) {
            //image type
            filteredGalleryModels[holder.layoutPosition].url =
                "file://" + filteredGalleryModels[holder.layoutPosition].sdcardPath
            filteredGalleryModels[holder.layoutPosition].isVideo = false
        } else if (filteredGalleryModels[holder.layoutPosition].type.compareTo(GalleryConstants.GalleryTypeVideos) == 0) {
            //video type
            filteredGalleryModels[holder.layoutPosition].url =
                Uri.fromFile(File(filteredGalleryModels[holder.layoutPosition].sdcardPath))
                    .toString()
            filteredGalleryModels[holder.layoutPosition].isVideo = true
        }
        holder.binding.model = filteredGalleryModels[holder.layoutPosition]
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
        notifyItemRangeInserted(0, filteredGalleryModels.size)
    }

    fun addItemToTop(file: GalleryModel) {
        try {
            if (originalGalleryModels.isNotEmpty())
                originalGalleryModels.add(0, file)
            else {
                originalGalleryModels = ArrayList()
                originalGalleryModels.add(0, file)
            }
            if (filteredGalleryModels.isNotEmpty())
                filteredGalleryModels.add(0, file)
            else {
                filteredGalleryModels = ArrayList()
                filteredGalleryModels.add(0, file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyItemInserted(0)
    }

    private fun changeSelection(position: Int) {
        //get index in original before edit in item
        val indexInOriginal = originalGalleryModels.indexOf(filteredGalleryModels[position])
        if (filteredGalleryModels[position].isSelected) {
            //deselect item
            filteredGalleryModels[position].isSelected = false
            filteredGalleryModels[position].index_when_selected = 0
            //change selection in original list
            originalGalleryModels[indexInOriginal] = filteredGalleryModels[position]
            if (getSelected().isEmpty()) {
                selectedType = ""
            }
            //update view
            notifyItemChanged(position)
        } else {
            if (maxSelectionCount == 1) {
                // one item is selected before
                deselectAll()
                filteredGalleryModels[position].isSelected = true
                filteredGalleryModels[position].index_when_selected = getSelected().size + 1
                selectedType = filteredGalleryModels[position].type
                //change selection in original list
                originalGalleryModels[indexInOriginal] = filteredGalleryModels[position]
                //update view
                notifyItemChanged(position)
            } else if (getSelected().size < maxSelectionCount) {
                if (filteredGalleryModels[position].type.compareTo(GalleryConstants.GalleryTypeImages) == 0) {
                    if (selectedType == GalleryConstants.GalleryTypeImages || selectedType == "") {
                        filteredGalleryModels[position].isSelected = true
                        //set item selection index
                        filteredGalleryModels[position].index_when_selected = getSelected().size + 1
                        selectedType = filteredGalleryModels[position].type
                        //change selection in original list
                        originalGalleryModels[indexInOriginal] = filteredGalleryModels[position]
                        //update view
                        notifyItemChanged(position)
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
                            //update view
                            notifyItemChanged(position)
                        } else {
                            // one video is selected before
                            deselectAll()
                            filteredGalleryModels[position].isSelected = true
                            //set item selection index
                            filteredGalleryModels[position].index_when_selected =
                                getSelected().size + 1
                            selectedType = filteredGalleryModels[position].type
                            //update view
                            notifyItemChanged(position)
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
                    context.getString(
                        R.string.you_may_not_select_more_than_s_item, maxSelectionCount.toString()),
                    Toast.LENGTH_LONG
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
            filteredGalleryModels.addAll(originalGalleryModels)
        } else {
            for (galleryModel in originalGalleryModels) {
                if (galleryModel.albumName.lowercase(Locale.getDefault()).contains(albumName)
                ) {
                    filteredGalleryModels.add(galleryModel)
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