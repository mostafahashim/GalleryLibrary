package hashim.gallerylib.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import hashim.gallerylib.R
import hashim.gallerylib.databinding.ItemRecyclerThumbnailBinding
import hashim.gallerylib.databinding.RecyclerBottomSheetAlbumItemBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnBottomSheetItemClickListener
import hashim.gallerylib.observer.OnItemThumbnailListener

class RecyclerSelectedThumbnailsAdapter(
    var columnWidth: Double,
    val galleryModels: ArrayList<GalleryModel>,
    var onItemThumbnailListener: OnItemThumbnailListener
) : RecyclerView.Adapter<RecyclerSelectedThumbnailsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_recycler_thumbnail,
            parent,
            false
        ) as ItemRecyclerThumbnailBinding
        return ViewHolder(
            binding
        )
    }

    override fun getItemCount() = galleryModels.size


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        galleryModels[holder.layoutPosition].columnWidth = columnWidth
        galleryModels[holder.layoutPosition].columnHeight = columnWidth

        val item = galleryModels[holder.layoutPosition]

        holder.binding.model = item
        holder.itemView.setOnClickListener {
            if (galleryModels[holder.layoutPosition].isSelected) {
                onItemThumbnailListener.onItemDeleted(holder.layoutPosition)
            } else {
                onItemThumbnailListener.onItemSelectedListener(holder.layoutPosition)
            }
        }
    }

    class ViewHolder(var binding: ItemRecyclerThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root)


}