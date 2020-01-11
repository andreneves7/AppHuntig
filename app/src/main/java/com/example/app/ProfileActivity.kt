package com.example.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_registo_user.*
import kotlinx.android.synthetic.main.custom_view.view.*
import kotlinx.android.synthetic.main.custom_view.view.showPass
import kotlinx.android.synthetic.main.pass_custom_view.view.*
import java.util.*

class ProfileActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    val mStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val editar = bEdit



        editar.setOnClickListener(View.OnClickListener{
            view -> showAlertLogin()

        })

        bFotoAdd.setOnClickListener{
            uploadImageToFirebaseStorage()
        }


        bAdd.setOnClickListener{
            Log.d("Profile", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0 )
        }

    }

    private fun showAlertNew(){
        val inflater = layoutInflater
        val inflate_view = inflater.inflate(R.layout.home_custom_view,null)

        val userEmailEdt = inflate_view.userNewEmail
        val userPassEdt = inflate_view.userNewPass

        val checkBoxTooggle = inflate_view.showPass

        checkBoxTooggle.setOnCheckedChangeListener{buttonView, isChecked ->
            if (!isChecked){
                userPassEdt.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            else{
                userPassEdt.transformationMethod = null
            }
        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Login novamente")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel"){
                dialog, which ->
            Toast.makeText(this,"Cancel" , Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done"){
                dialog, which ->

            val userNewEmail = userEmailEdt.text.toString()
            val userPassword = userPassEdt.text.toString()

            Auth.signInWithEmailAndPassword(userNewEmail, userPassword)
                .addOnCompleteListener { task4 ->
                    if (task4.isSuccessful) {
                        Toast.makeText(this, "Successfully Re-Logged :)", Toast.LENGTH_LONG).show()
                        Log.d("Profile", "user re-logged  ${Auth.currentUser?.uid}")
                    }
                    Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
                    Log.d("Profile", "done botao")
                }


        }

        val dialog = alertDialog.create()
        dialog.show()
    }






    private fun showAlertLogin(){
        val inflater = layoutInflater
        val inflate_view = inflater.inflate(R.layout.pass_custom_view,null)

        val userEmailEdt = inflate_view.userEmail
        val userPassEdt = inflate_view.userPass

        val checkBoxTooggle = inflate_view.showPass

        checkBoxTooggle.setOnCheckedChangeListener{buttonView, isChecked ->
            if (!isChecked){
                userPassEdt.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            else{
                userPassEdt.transformationMethod = null
            }
        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("atualizar")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel"){
            dialog, which ->
            Toast.makeText(this,"Cancel" , Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done"){
            dialog, which ->

            val user = FirebaseAuth.getInstance().currentUser
            val userNewEmail = userEmailEdt.text.toString()
            val userPassword = userPassEdt.text.toString()


            if (!userNewEmail.isEmpty() && userPassword.isEmpty()) {

                user?.updateEmail(userNewEmail)?.addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        Toast.makeText(this, "Update email Success", Toast.LENGTH_LONG).show()
                        Log.d("Profile","email update auth")
                        showAlertNew()

                    }else {
                        Toast.makeText(this, "Error email Update", Toast.LENGTH_LONG).show()
                        Log.d("Profile","email erro auth")
                        showAlertNew()
                    }
                }
            }else if (userNewEmail.isEmpty() && !userPassword.isEmpty()){
                user?.updatePassword(userPassword)?.addOnCompleteListener { task3 ->
                    if (task3.isSuccessful) {
                        Toast.makeText(this, "Update password Success", Toast.LENGTH_LONG).show()
                        Log.d("Profile","password erro auth")
                        showAlertNew()

                    }else {
                        Toast.makeText(this, "Error password Update", Toast.LENGTH_LONG).show()
                        showAlertNew()
                    }
                }
            }else{
                Toast.makeText(this, "so pode mudar um de cada vez", Toast.LENGTH_LONG).show()
                Log.d("Profile","dois preenchidos")
            }

            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
             Log.d("Profile", "done botao")
        }

        val dialog = alertDialog.create()
        dialog.show()
    }





    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d( "Profile", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            vImg.setImageBitmap(bitmap)

            bAdd.alpha = 0f

        }
    }

    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = mStorage.getReference("/images/$filename")

            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("Profile", "Successfully upload image: ${it.metadata?.path}")



                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("Profile", "File localition: $it")

                    }
                }

    }







//    private fun editar(){
//
//        val user = FirebaseAuth.getInstance().currentUser
//        var newPass = edPass.text.toString()
//
//
//        if (!newPass.isEmpty()) {
//
//            user?.updatePassword(newPass)?.addOnCompleteListener { task2 ->
//                if (task2.isSuccessful) {
//
//
//                    Toast.makeText(this, "Update pass Success", Toast.LENGTH_LONG).show()
//                    Log.d("Profile","email update auth")
//
//
//
//                }else {
//                    Toast.makeText(this, "Error email Update", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//
//    }

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

}
