package hashim.gallerylib.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import hashim.gallerylib.view.GalleryBaseActivity
import hashim.gallerylib.R
import hashim.gallerylib.databinding.RecyclerBottomSheetAlbumItemBinding
import hashim.gallerylib.model.AlbumModel
import hashim.gallerylib.observer.OnBottomSheetItemClickListener

class RecyclerAlbumsBottomSheetAdapter(
    var columnWidth: Double,
    val albumModels: ArrayList<AlbumModel>,
    var onBottomSheetItemClickListener: OnBottomSheetItemClickListener
) : RecyclerView.Adapter<RecyclerAlbumsBottomSheetAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recycler_bottom_sheet_album_item,
            parent,
            false
        ) as RecyclerBottomSheetAlbumItemBinding
        return ViewHolder(
            binding
        )
    }

    override fun getItemCount() = albumModels.size


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        albumModels[holder.layoutPosition].columnWidth = columnWidth
        albumModels[holder.layoutPosition].columnHeight = columnWidth

        val item = albumModels[holder.layoutPosition]

        holder.binding.model = item
        holder.itemView.setOnClickListener {
            onBottomSheetItemClickListener.onBottomSheetItemClickListener(holder.layoutPosition)
        }

    }

    class ViewHolder(var binding: RecyclerBottomSheetAlbumItemBinding) :
        RecyclerView.ViewHolder(binding.root)


}