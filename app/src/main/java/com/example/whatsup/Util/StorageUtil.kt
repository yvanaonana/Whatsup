package com.example.whatsup.Util

import android.net.Uri
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.lang.NullPointerException
import java.security.cert.CertPath
import java.util.*

object StorageUtil {

    private val storageInstance : FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val storageRef = FirebaseStorage.getInstance().reference
    private val currentUserRef : StorageReference
    get() = storageInstance.reference
        .child(FirebaseAuth.getInstance().currentUser?.uid ?: throw NullPointerException("UID is null"))

    fun uploadProfilePhoto(imageBytes : ByteArray,
                           onSuccess: (imagePath: String) -> Unit){
        val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }

    fun uploadMessageImage(imageBytes : ByteArray,
                           onSuccess: (imagePath: String) -> Unit){
        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes)
            .addOnSuccessListener {
                onSuccess(ref.path)
            }
    }
    fun pathToReference(path: String) = storageInstance.getReference(path)

    fun uploadImageOfGroupe(idGroupe: String, filePath: Uri, onSuccess: (String) -> Unit) {
        var file = filePath
        val riversRef = storageRef.child(
            "chat_groupe_images/${idGroupe}"
        )
        var uploadTask = file?.let { riversRef.putFile(it) }

        uploadTask!!.addOnFailureListener {

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation riversRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    onSuccess(downloadUri.toString())
                } else {
                    // Handle failures
                    onSuccess("unploading error")

                }
            }
        }
    }


}