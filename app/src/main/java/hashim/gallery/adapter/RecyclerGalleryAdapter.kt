package hashim.gallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import hashim.gallery.R
import hashim.gallery.databinding.ItemRecyclerGalleryItemsBinding
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.util.ScreenSizeUtils

class RecyclerGalleryAdapter(
    var models: ArrayList<GalleryModel>,
) : RecyclerView.Adapter<RecyclerGalleryAdapter.ViewHolder>() {

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            inflater, R.layout.item_recycler_gallery_items, parent, false
        ) as ItemRecyclerGalleryItemsBinding
        return ViewHolder(
            binding
        )
    }

    override fun getItemCount() = models.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screenWidth = ScreenSizeUtils().getScreenWidth(context as AppCompatActivity)
        val columnWidth = (105.00 * screenWidth) / 360.00
        models[holder.layoutPosition].columnWidth = columnWidth
        models[holder.layoutPosition].columnHeight = columnWidth

        holder.binding.model = models[holder.layoutPosition]
        holder.binding.imgviewRemove.setOnClickListener {
            models.removeAt(holder.layoutPosition)
            notifyItemRemoved(holder.layoutPosition)
        }
    }

    fun setList(list: ArrayList<GalleryModel>) {
        models = list
        notifyItemRangeRemoved(0, models.size)
        notifyItemRangeChanged(0, models.size)
    }


    class ViewHolder(var binding: ItemRecyclerGalleryItemsBinding) :
        RecyclerView.ViewHolder(binding.root)

}