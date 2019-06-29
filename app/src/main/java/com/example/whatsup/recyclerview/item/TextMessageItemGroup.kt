package com.example.whatsup.recyclerview.item

import android.content.Context
import android.graphics.Color
import com.example.whatsup.R
import com.example.whatsup.Util.FirestoreUtil
import com.example.whatsup.model.TextMessage
import com.google.firebase.auth.FirebaseAuth
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message_groupe.*
import java.text.SimpleDateFormat

class TextMessageItemGroup(val message: TextMessage,
                           val context: Context
) : Item() {
    var isSelectet = false
    private val colorTranslateText = Color.GRAY
    private val colorSrcText = Color.BLACK

    override fun bind(viewHolder: ViewHolder, position: Int) {

        val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        viewHolder.textView_message_time_groupe.text = dateFormat.format(message.time)
        viewHolder.textView_message_text_group.text = message.text
        // on recupère zt on affiche le nom de de celui qui a envoyer le message
        FirestoreUtil.getUserByUid(message.senderId, onComplete = {
            var senderName = "me"
            // on controle si le message ne provient pas del'utiliisateur couran
            if (FirebaseAuth.getInstance().currentUser?.uid != message.senderId)
                senderName = it.name

            viewHolder.textView_sender_name_txt.text = senderName
        })
    }


    override fun getLayout() = R.layout.item_text_message_groupe

    override fun isSameAs(other: com.xwray.groupie.Item<*>?): Boolean {
        if (other !is TextMessageItemGroup)
            return false
        if (this.message != other.message)
            return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as? TextMessageItemGroup)
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}