package com.jerry.chitchat

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerbuttton.setOnClickListener {
            performregister()
        }

        alreadyaccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        register_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var SelectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            SelectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, SelectedPhotoUri)
            profile_image.setImageBitmap(bitmap)
            register_btn.alpha = 0f
          //  val bitMapDrawable = BitmapDrawable(bitmap)
           // register_btn.setBackgroundDrawable(bitMapDrawable)
        }
    }

    private fun uploadImageToFirebaseStorage() {

        if (SelectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$filename")
        ref.putFile(SelectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())

                }
            }

    }

    private fun performregister() {

        val email = email.text.toString()
        val password = password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {

            Toast.makeText(this, "please enter email or password", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "failure loading the person ${it.message}", Toast.LENGTH_SHORT)
                    .show()

            }
    }
        private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

            val user = User(uid, username.text.toString(), profileImageUrl)
            ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "uploaded to Firebase database")
                    val intent = Intent(this, MessageActivity::class.java)
                    startActivity(intent)

                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to upload to Firebase database")
                }
        }

    }

    @Parcelize
    class User(val uid: String, val username: String, val profileImageUrl: String):Parcelable {
        constructor() : this("", "", "")
    }




