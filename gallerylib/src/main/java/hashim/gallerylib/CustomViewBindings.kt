package hashim.gallerylib

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


class CustomViewBindings {
    companion object {
        @BindingAdapter("imageBitmap")
        @JvmStatic
        fun loadImageBitmap(imageView: ImageView, bitmap: Bitmap?) {
            if (bitmap != null) {
                // If we don't do this, you'll see the old image appear briefly
                // before it's replaced with the current image
                if (imageView.getTag(R.id.image_url) == null || imageView.getTag(R.id.image_url) != bitmap) {
                    imageView.setImageBitmap(bitmap)
                    imageView.setTag(R.id.image_url, bitmap)
                }
            } else {
                imageView.setTag(R.id.image_url, null)
                imageView.setImageBitmap(null)
            }
        }

        @BindingAdapter("imageUrlRect")
        @JvmStatic
        fun loadImageUrlRect(imageView: ImageView, imageUrl: String?) {
            if (imageUrl != null) {
                // If we don't do this, you'll see the old image appear briefly
                // before it's replaced with the current image
                if (imageView.getTag(R.id.image_url) == null || imageView.getTag(R.id.image_url) != imageUrl) {
                    imageView.setImageBitmap(null)
                    imageView.setTag(R.id.image_url, imageUrl)

                    Glide.with(imageView).load(imageUrl)
                        .apply(
                            RequestOptions().placeholder(R.drawable.placeholder_gallery_icon)
                                .error(R.drawable.placeholder_gallery_icon)
                                .diskCacheStrategy(
                                    DiskCacheStrategy.ALL
                                )
                        )
                        .into(imageView)
                }
            } else {
                imageView.setTag(R.id.image_url, null)
                imageView.setImageBitmap(null)
            }
        }

        @BindingAdapter("imageUrlRoundRect")
        @JvmStatic
        fun loadImageUrlRoundRect(imageView: ImageView, imageUrlRoundTopRect: String?) {
            if (imageUrlRoundTopRect != null) {
                // If we don't do this, you'll see the old image appear briefly
                // before it's replaced with the current image
                if (imageView.getTag(R.id.image_url) == null || imageView.getTag(R.id.image_url) != imageUrlRoundTopRect) {
                    imageView.setImageBitmap(null)
                    imageView.setTag(R.id.image_url, imageUrlRoundTopRect)

                    val cornerType = RoundedCornersTransformation.CornerType.ALL
                    val transformation = MultiTransformation(
                        CenterCrop(),
                        RoundedCornersTransformation(
                            20, 0,
                            cornerType
                        )
                    )
                    val requestOptions = RequestOptions.bitmapTransform(transformation)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                    val thumbnail: RequestBuilder<Drawable> = Glide.with(imageView)
                        .load(R.drawable.round_5_bg_gray_light)
                        .apply(requestOptions)

                    Glide.with(imageView).load(imageUrlRoundTopRect)
                        .apply(requestOptions).thumbnail(thumbnail)
                        .into(imageView)
                }
            } else {
                imageView.setTag(R.id.image_url, null)
                imageView.setImageBitmap(null)
            }
        }

        @BindingAdapter("bind:layoutHeight")
        @JvmStatic
        fun setHeight(view: View, height: Double) {
            view.layoutParams.height = height.toInt()
            view.requestLayout()
        }

        @BindingAdapter("bind:layoutWidth")
        @JvmStatic
        fun setWidth(view: View, width: Double) {
            view.layoutParams.width = width.toInt()
            view.requestLayout()
        }

        @BindingAdapter("bind:imageTintColor")
        @JvmStatic
        fun imageTintColor(imageView: ImageView, color: Int) {
            imageView.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                color,
                BlendModeCompat.SRC_ATOP
            )
        }

        @BindingAdapter("textColor")
        @JvmStatic
        fun bindTextColor(view: TextView, color: Int) {
            view.setTextColor(ContextCompat.getColor(view.context, color))
        }

    }
}