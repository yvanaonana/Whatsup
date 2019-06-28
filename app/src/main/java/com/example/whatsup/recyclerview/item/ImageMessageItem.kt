package com.example.whatsup.recyclerview.item

import android.content.Context
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.whatsup.R
import com.example.whatsup.Util.StorageUtil
import com.example.whatsup.model.ImageMessage
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_image_message.*

class ImageMessageItem(val message: ImageMessage,
                       val context: Context)
    :MessageItem(message){

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        var requestOptions = RequestOptions()
        requestOptions.placeholder(R.drawable.ic_image_black_24dp)
        requestOptions.error(R.drawable.ic_account_circle_black_24dp)
        Toast.makeText(context, message.ImagePath, Toast.LENGTH_LONG).show()
        Glide.with(context)
            .setDefaultRequestOptions(requestOptions)
            .load(StorageUtil.pathToReference(message.ImagePath))
            .into(viewHolder.imageView_message_image)
    }

    override fun getLayout() = R.layout.item_image_message

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if(other !is ImageMessageItem)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? ImageMessageItem)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}