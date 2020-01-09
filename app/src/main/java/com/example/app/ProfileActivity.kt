package com.example.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_registo_user.*
import java.util.*

class ProfileActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    //val mAuth = FirebaseFirestore.getInstance().collection("Users")
    val mStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val editar = bEdit



        editar.setOnClickListener(View.OnClickListener{
            view -> editar()

        })


        bAdd.setOnClickListener{
            Log.d("RegistoUser", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0 )
        }



        /*val atual = mAuth.document(Auth.currentUser.toString()).toString()
        var newNome = edName as TextView
        newNome.text =  atual

        val docRef = mAuth.document(atual)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("exist", "DocumentSnapshot data: ${document.data}")



                } else {
                    Log.d("noexist", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("error", "get failed with ", exception)
            }*/


    }


    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d( "RegistoUser", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            vImg.setImageBitmap(bitmap)

            bAdd.alpha = 0f

            /*val bitmapDrawable = BitmapDrawable(bitmap)
            bAdd.setBackgroundDrawable(bitmapDrawable)*/
        }
    }

    private fun uploadImageToFirebaseStorage(password: String, email: String, name: String){
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = mStorage.getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegistoUser", "Successfully upload image: ${it.metadata?.path}")


                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("RegistoUser", "File localition: $it")

                    }
                }

    }







    private fun editar(){

        val user = FirebaseAuth.getInstance().currentUser
        var newPass = edPass.text.toString()
        var newEmail = edEmail.text.toString()
        //var newNome = edName.text.toString()


        if (!newEmail.isEmpty() && newPass.isEmpty()) {

            user!!.updateEmail(newEmail).addOnCompleteListener { task2 ->
                if (task2.isSuccessful) {

                    Toast.makeText(this, "Update email Success", Toast.LENGTH_LONG).show()
                    /*val items = HashMap<String, Any>()
                    items.put("Email", newEmail)
                    items.put("Password", newPass)

                    //sendEmailVerification()
                    mAuth.document(atual).set(items).addOnSuccessListener { void: Void? ->

                        Toast.makeText(this, "Update email Success", Toast.LENGTH_LONG).show()

                    }*/
                }else {
                    Toast.makeText(this, "Error email Update", Toast.LENGTH_LONG).show()
                }
            }
        }else {

            user!!.updatePassword(newPass).addOnCompleteListener { task3 ->
                if (task3.isSuccessful) {
                    Toast.makeText(this, "Update pass Success", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error email Update", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.signOut) {
            Auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateCurrentUser(){
        Auth.currentUser

    }
}
