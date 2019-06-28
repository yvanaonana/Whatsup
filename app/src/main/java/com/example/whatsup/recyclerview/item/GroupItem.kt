package com.example.whatsup.recyclerview.item

import android.content.Context
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.whatsup.R
import com.example.whatsup.glide.GlideApp
import com.example.whatsup.model.ChatGroup
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_person.*

class GroupeItem(val chatGroupe: ChatGroup,
                 val chatGroupeId: String,
                 val context: Context) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_name.text =chatGroupe.groupeName
        viewHolder.textView_bio.text = chatGroupe.groupeDescription
        if(chatGroupe.groupIcon != ""){
            GlideApp.with(context)
                .load(chatGroupe.groupIcon)
                .placeholder(R.drawable.ic_people_black_24dp)
                .into(viewHolder.imageView_profile_picture)
        }

    }

    override fun getLayout() = R.layout.item_person

}