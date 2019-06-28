package com.example.whatsup

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.view.ContextThemeWrapper
import android.support.v7.widget.LinearLayoutManager
import android.widget.AdapterView
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.Section
import com.example.whatsup.AppContants
import com.example.whatsup.Util.FirestoreUtil
import com.example.whatsup.Util.StorageUtil
import com.example.whatsup.model.ImageMessage
import com.example.whatsup.model.MessageType
import com.example.whatsup.model.TextMessage
import com.example.whatsup.model.User
import com.google.firebase.auth.FirebaseAuth
import com.example.whatsup.recyclerview.item.ImageMessageItemGroup
import com.example.whatsup.recyclerview.item.TextMessageItemGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_groupe_chat.*
import java.io.ByteArrayOutputStream
import java.util.*

private const val RC_SELECT_IMAGE = 2

class GroupeChatActivity : AppCompatActivity() {


    private lateinit var currentGroupeUID: String
    private var sizeOfmember: Int = 0

    private lateinit var messageListenerRegistration: ListenerRegistration
    private var shouldInitRecycleView = true
    private lateinit var messageSection: Section

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupe_chat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppContants.NOM_GROUPE)

        currentGroupeUID = intent.getStringExtra(AppContants.ID_GROUPE)


        messageListenerRegistration =
            FirestoreUtil.addGroupeChatMessagesListener(currentGroupeUID, this, onListner = {
                updateRecycleView(it)
            })
        imageView_send_groupe.setOnClickListener {
            var textMessage = editText_message_groupe.text.toString()

            val messageText = TextMessage(
                textMessage,
                Calendar.getInstance().time,
                FirebaseAuth.getInstance().currentUser!!.uid, MessageType.TEXT
            )
            editText_message_groupe.setText("")
            FirestoreUtil.sendGroupeMessage(messageText, currentGroupeUID)
            //}
        }

        fab_send_image_groupe.setOnClickListener {

            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }

            startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), RC_SELECT_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val selectedimagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(contentResolver, selectedimagePath)
            val outputStream = ByteArrayOutputStream()

            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadMessageImage(selectedImageBytes, onSuccess = { imagepath: String ->
                val messageToSend = ImageMessage(
                    imagepath, Calendar.getInstance().time,
                    FirebaseAuth.getInstance().currentUser!!.uid
                )

                FirestoreUtil.sendGroupeMessage(messageToSend, currentGroupeUID)
            })
        }
    }

    private fun updateRecycleView(messages: List<Item>) {
        fun init() {
            recycler_view_messages_groupe.apply {
                layoutManager = LinearLayoutManager(this@GroupeChatActivity)
                adapter = GroupAdapter<ViewHolder>().apply {
                    messageSection = Section(messages)
                    this.add(messageSection)
                    setOnItemClickListener(onItemClick)
                }
            }

            shouldInitRecycleView = false
        }

        fun updateItem() = messageSection.update(messages)

        if (shouldInitRecycleView)
            init()
        else {
            updateItem()
        }

        recycler_view_messages_groupe.scrollToPosition((recycler_view_messages_groupe.adapter?.itemCount ?: 1) - 1)
    }

    private val onItemClick = OnItemClickListener { item, view ->

        val progressdialog = ProgressDialog(this)
        progressdialog.setMessage("Chargement")
        progressdialog.setCancelable(false)
        progressdialog.show()
        if (item is TextMessageItemGroup) {

            //on recherche le nom de l'utilisateur ayant envoyer le message
            FirestoreUtil.getUserByUid(item.message.senderId, onComplete = {
                progressdialog.dismiss()
                val builder = AlertDialog.Builder(ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Dialog))

                with(builder)
                {
                    setTitle("Démarrer un nouveau chat ")
                    setMessage("Démarer le chat avec avec " + it.name)
                    setPositiveButton("Oui", DialogInterface.OnClickListener { dialog, which ->
                        openChatActivity(it, item.message.senderId)
                    })
                    setNegativeButton("Non", DialogInterface.OnClickListener { dialog, which ->
                        // Do something when user press the positive button
                    })
                    show()
                }
            })

        } else {

            val item = item as ImageMessageItemGroup
            FirestoreUtil.getUserByUid(item.message.senderId, onComplete = {
                progressdialog.dismiss()
                val builder = AlertDialog.Builder(ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Dialog))

                with(builder)
                {
                    setTitle("Démarrer un nouveau chat ")
                    setMessage("Démarer le chat avec avec " + it.name)
                    setPositiveButton("Oui", DialogInterface.OnClickListener { dialog, which ->
                        openChatActivity(it, item.message.senderId)
                    })
                    setNegativeButton("Non", DialogInterface.OnClickListener { dialog, which ->
                        // Do something when user press the positive button
                    })
                    show()
                }
            })
        }

        true
    }

    private fun openChatActivity(it: User, senderId: String) {
        // startActivity<ChatActivity>(AppConstants.USER_NAME to it.name, AppConstants.USER_ID to senderId)
        val myIntent = Intent(this, ChatActivity::class.java)
        myIntent.putExtra(AppContants.USER_NAME, it.name)
        myIntent.putExtra(AppContants.USER_ID, senderId)

        startActivity(myIntent)
        finish()
    }

}

