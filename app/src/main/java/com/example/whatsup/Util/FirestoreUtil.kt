package com.example.whatsup.Util

import android.content.Context
import android.util.Log
import com.example.whatsup.model.*
import com.example.whatsup.recyclerview.item.ImageMessageItem
import com.example.whatsup.recyclerview.item.PersonItem
import com.example.whatsup.recyclerview.item.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mirai.whatsup.entities.ChatGroup
import com.mirai.whatsup.receycleView.item.ImageMessageItemGroup
import com.mirai.whatsup.receycleView.item.TextMessageItemGroup
import com.xwray.groupie.kotlinandroidextensions.Item
import java.lang.NullPointerException
import java.util.*

object FirestoreUtil {

    private val firestoreInstance : FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef : DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
            ?: throw NullPointerException ("uid is null")}")

    private val chatChannelsCollectionRef = firestoreInstance.collection("chatChannels")
    private val groupeChatCollectionRef = firestoreInstance.collection("chatgroupes")
    private val userCollection = firestoreInstance.collection("users")

    fun initCurrentUserIfFirstTime (onComplete : () -> Unit){
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if(!documentSnapshot.exists()){
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                    "", null)
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }

            else
                onComplete()
        }
    }

    fun updateCurrentUser (name : String = "", bio : String = "", profilePicturePath : String? = null){
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (bio.isNotBlank()) userFieldMap["bio"] = bio
        if (profilePicturePath != null) userFieldMap["profilePicturePath"] = profilePicturePath
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser (onComplete: (User) -> Unit){
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }

    fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit) : ListenerRegistration{
        return firestoreInstance.collection("users")
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
                }
                onListen(items)
            }
    }


    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOnCreateChatChannel(otherUserId: String,
                               onComplete: (channelId: String) -> Unit){
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if(it.exists()){
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                val newChannel = chatChannelsCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                currentUserDocRef.collection("engagedChatChannels")
                    .document(otherUserId)
                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels")
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }

    fun addChatMessagesListener (channelId: String, context: Context,
                                 onListen: (List<Item>) -> Unit) : ListenerRegistration{
        return chatChannelsCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "ChatMessagesListener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if(it["type"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                    else
                        items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))
                    return@forEach
                }

                onListen(items)
            }
    }

    fun sendMessage(message: Message, ChannelId: String){
        chatChannelsCollectionRef.document(ChannelId)
            .collection("messages")
            .add(message)
    }

    fun getUserByUid(uid: String, onComplete: (user: User) -> Unit) {
        userCollection.document(uid).get().addOnSuccessListener { onComplete(it.toObject(User::class.java)!!) }
    }

    fun addGroupeChatMessagesListener(groupeId: String,
                                      context: Context,
                                      onListner: (List<Item>) -> Unit
    ): ListenerRegistration {
        return groupeChatCollectionRef.document(groupeId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "ChatMessageslistener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot!!.documents.forEach {
                    if (it["type"] == MessageType.TEXT) {
                        val textMessage = it.toObject(TextMessage::class.java)!!
                        items.add(TextMessageItemGroup(textMessage, context))
                    } else {
                        val imageMessage = it.toObject(ImageMessage::class.java)!!
                        items.add(ImageMessageItemGroup(imageMessage, context))
                    }
                    return@forEach
                }

                onListner(items)

            }
    }


    fun createGroupeChat(members: MutableList<String>,
                         groupeName: String,
                         groupeDescription: String,
                         onComplete: (groupeId: String) -> Unit) {

        val newgroupe = ChatGroup(FirebaseAuth.getInstance().currentUser!!.uid, groupeName,
            groupeDescription, Date(0),
            "", members)
        val newChatgroup = groupeChatCollectionRef.document()
        newChatgroup.set(newgroupe).addOnSuccessListener {
            currentUserDocRef.collection("groupes").add(mapOf("groupeId" to newChatgroup.id))

            for (itemuserId in members) {
                val refCurentuser = firestoreInstance.document(
                    "users/${itemuserId}"
                )
                refCurentuser.collection("groupes").add(mapOf("groupeId" to newChatgroup.id))
            }
            onComplete(newChatgroup.id)
        }
    }

    fun updateImageGroup(profilPicturePath: String? = null, refGroupe: String, onComplete: () -> Unit) {
        val refCurentGroupe = firestoreInstance.collection("chatgroupes").document(refGroupe)
        refCurentGroupe.update("groupIcon", profilPicturePath)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener {
                Log.e("FireStore", "Error adding document", it.cause)
            }
    }

    fun sendGroupeMessage(message: Message, groupeId: String) {
        groupeChatCollectionRef.document(groupeId)
            .collection("messages")
            .add(message)
    }

}