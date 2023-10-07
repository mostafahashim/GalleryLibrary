package hashim.gallerylib.view.sub

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import hashim.gallerylib.R
import hashim.gallerylib.adapter.RecyclerAlbumsBottomSheetAdapter
import hashim.gallerylib.databinding.BottomSheetAlbumRecyclerBinding
import hashim.gallerylib.observer.OnBottomSheetItemClickListener
import hashim.gallerylib.view.GalleryBaseActivity


class BottomSheetAlbumsFragment : BottomSheetDialogFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GalleryBaseActivity)
            activity = context
    }

    lateinit var activity: GalleryBaseActivity
    lateinit var binding: BottomSheetAlbumRecyclerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.bottom_sheet_album_recycler,
                container,
                false
            )
        binding.lifecycleOwner = this
        dialog?.setOnShowListener {
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    lateinit var onBottomSheetItemClickListener: OnBottomSheetItemClickListener

    fun setOnBottomSheetItemClickObserver(listener: OnBottomSheetItemClickListener) {
        this.onBottomSheetItemClickListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var arrayList = requireArguments()["AlbumModels"] as ArrayList<String>
        var title = requireArguments()["title"] as String
        binding.tvLabelBottomSheet.text = title

        binding.rcBottomSheet.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        var myAdapter = RecyclerAlbumsBottomSheetAdapter(
                arrayList,
                object : OnBottomSheetItemClickListener {
                    override fun onBottomSheetItemClickListener(position: Int) {
                        onBottomSheetItemClickListener.onBottomSheetItemClickListener(position)
                        dismiss()
                    }
                })
        binding.rcBottomSheet.adapter = myAdapter
        binding.ivCloseBottomSheet.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}