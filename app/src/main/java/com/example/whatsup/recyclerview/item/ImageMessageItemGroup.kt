package com.mirai.whatsup.receycleView.item

import android.content.Context
import com.bumptech.glide.Glide
import com.example.whatsup.R
import com.example.whatsup.Util.FirestoreUtil
import com.example.whatsup.Util.StorageUtil
import com.example.whatsup.model.ImageMessage
import com.example.whatsup.recyclerview.item.MessageItem
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_image_message.*
import kotlinx.android.synthetic.main.item_image_message_groupe.*

class ImageMessageItemGroup(val message: ImageMessage,
                            val context:Context): MessageItem(message) {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        super.bind(viewHolder, position)
        Glide.with(context)
            .load(StorageUtil.pathToReference(message.ImagePath))
            .into(viewHolder.imageView_message_image_groupe)

        FirestoreUtil.getUserByUid(message.senderId, onComplete = {
            viewHolder.textView_sender_name_groupe.text = it.name
        })
    }

    override fun getLayout()= R.layout.item_image_message_groupe

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is ImageMessageItemGroup)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? ImageMessageItemGroup)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}