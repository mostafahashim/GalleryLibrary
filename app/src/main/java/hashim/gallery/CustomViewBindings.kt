package hashim.gallery

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class CustomViewBindings {
    companion object {

        @BindingAdapter("imageUrlCircle")
        @JvmStatic
        fun loadImageUrlCircle(imageView: ImageView, imageUrlRoundTopRect: String?) {
            if (imageUrlRoundTopRect != null) {
                // If we don't do this, you'll see the old image appear briefly
                // before it's replaced with the current image
                if (imageView.getTag(R.id.image_url) == null || imageView.getTag(R.id.image_url) != imageUrlRoundTopRect) {
                    imageView.setImageBitmap(null)
                    imageView.setTag(R.id.image_url, imageUrlRoundTopRect)
                    val transformation = RequestOptions.circleCropTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                    val thumbnail: RequestBuilder<Drawable> = Glide.with(imageView)
                        .load(R.mipmap.ic_launcher_round)
                        .apply(transformation)
                    Glide.with(imageView).load(imageUrlRoundTopRect)
                        .apply(transformation).thumbnail(thumbnail)
                        .into(imageView)
                }
            } else {
                imageView.setTag(R.id.image_url, null)
                imageView.setImageBitmap(null)
            }
        }

    }
}