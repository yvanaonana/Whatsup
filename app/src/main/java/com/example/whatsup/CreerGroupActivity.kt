package com.example.whatsup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.whatsup.Util.FirestoreUtil
import com.example.whatsup.Util.StorageUtil
import com.example.whatsup.fragment.ModalButtonFragment
import com.example.whatsup.fragment.ParamModalFragment
import com.example.whatsup.glide.GlideApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_creer_group.*
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.toast

class CreerGroupActivity : AppCompatActivity() {

    val CAMERA_REQUEST_CODE = 0
    val RC_SELECT_IMAGE = 5
    //Permission code
    private val PERMISSION_CODE_GALERY = 1001;
    var imageGroupeUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creer_group)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageView_profile_group.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE_GALERY);
                } else {
                    //permission already granted
                    startgallery()
                }
            } else {
                //system OS is < Marshmallow
                startgallery()
            }
        }
        btn_add_member.setOnClickListener {
            if (verifierChampOk()) {
                val myModal = ModalButtonFragment()
                myModal.show(supportFragmentManager, "MODAL")

                //FireStoreUtil.getListOfUser(this, onListen = { createChoisAlertDialog(it) })

                if (ParamModalFragment.listIdUserForGroup.size != 0) {
                    Toast.makeText(this, "ok", Toast.LENGTH_LONG).show()
                }
            }
        }

        btn_save_group.setOnClickListener {
            if (ParamModalFragment.listIdUserForGroup.size != 0 && imageGroupeUri != null)
                sendDataToFireBAse()
            else {
                val snackbar = Snackbar.make(
                    it,
                    "sélectionnez des membres du groupe et/ou  une image",
                    Snackbar.LENGTH_LONG
                )
                snackbar.show()
            }
        }
    }

    var currentPhotoPath: String = ""


    private fun sendDataToFireBAse() {
        val progressdialog = indeterminateProgressDialog("Creation du groupe")
        FirestoreUtil.createGroupeChat(
            ParamModalFragment.listIdUserForGroup,
            editText_name_group.text.toString().trim(),
            editText_desc_group.text.toString().trim(),
            onComplete = { idGroupe: String ->
                imageGroupeUri?.let { nonNulUri ->
                    StorageUtil.uploadImageOfGroupe(idGroupe, nonNulUri, onSuccess = { url: String ->
                        FirestoreUtil.updateImageGroup(url, idGroupe, onComplete = {
                            toast("groupe creer avec succes")
                            progressdialog.dismiss()

                            val myIntent = Intent(this, GroupeChatActivity::class.java)
                            myIntent.putExtra(AppContants.ID_GROUPE, idGroupe)
                            myIntent.putExtra(AppContants.USER_ID, FirebaseAuth.getInstance().currentUser?.uid)
                            myIntent.putExtra(AppContants.NOM_GROUPE, editText_name_group.text.toString().trim())
                            //myIntent.putExtra(AppConstants.NOMBRE_MEMBRE_GROUPE, ParamModalFragment.listIdUserForGroup.size)
                            startActivity(myIntent)
                            finish()
                        })
                    })
                }
            })
    }


    private fun verifierChampOk(): Boolean {

        if (editText_name_group.text.toString().trim().equals("", true)) {
            editText_name_group.error = "Require!!"
            return false
        }
        return true
    }

    fun startgallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(Intent.createChooser(intent, "Image de profil"), RC_SELECT_IMAGE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_CODE_GALERY -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    startgallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {

                if (resultCode == Activity.RESULT_OK) {

                    imageGroupeUri = data?.data

                    GlideApp.with(this)
                        .load(currentPhotoPath)
                        .transform(CircleCrop())
                        .into(imageView_profile_group)
                    //val imageBitmap = data?.extras?.get("data") as Bitmap
                    //imgAvatar.setImageBitmap(imageBitmap)
                }
            }

            RC_SELECT_IMAGE -> {

                imageGroupeUri = data?.data
                toast("URI: ${imageGroupeUri}")

                //val imageBitmap = data?.extras?.get("data") as Bitmap
                imageView_profile_group.setImageURI(imageGroupeUri)
                /* GlideApp.with(this)
                     .load(currentPhotoPath)
                     .transform(CircleCrop())
                     .into(imgAvatar)
                     */
            }
            else -> {
                Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStop() {
        super.onStop()
        ParamModalFragment.listIdUserForGroup.clear()
    }

}
