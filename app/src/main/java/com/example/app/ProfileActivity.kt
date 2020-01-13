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
import kotlinx.android.synthetic.main.email_custom_view.view.*
import kotlinx.android.synthetic.main.pass_custom_view.view.showPass
import kotlinx.android.synthetic.main.custom_view.view.*
import kotlinx.android.synthetic.main.pass_custom_view.*
import kotlinx.android.synthetic.main.pass_custom_view.view.*
import org.jetbrains.anko.email
import java.util.*

class ProfileActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    val mStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val editar = bEdit
        val editarPass = bEditPass


        editarPass.setOnClickListener{
            showAlertPass()
        }


        editar.setOnClickListener(View.OnClickListener{
           showAlertEmail()

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

       old()


    }

    private fun old(){
        val show = textView
        val user = Auth.currentUser
        val userEmail = Auth.currentUser?.email

        // buscar nome ao firestore do user
        if (user != null){
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener{
                document->
                if (document != null){
                    val nome = document.data?.get("name")
                    show.setText("email: " + userEmail + "\n"+ "name: " + nome)

                    Log.d("Profile", "DocumentSnapshot data: ${document.data?.get("name")}")
                } else {
                    Log.d("Profile", "No such document")
                }
            }
            //show.setText("email: "+ userEmail)
            Log.d("Profile", "show email")
        }
    }

    private fun showAlertLogin(){
        val inflater = layoutInflater
        val inflate_view = inflater.inflate(R.layout.custom_view,null)

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
        alertDialog.setTitle("Login novamente")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel"){
                dialog, which ->
            Toast.makeText(this,"Cancel" , Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done"){
                dialog, which ->

            val email = userEmailEdt.text.toString()
            val password = userPassEdt.text.toString()

            Auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task4 ->
                    if (task4.isSuccessful) {
                        Toast.makeText(this, "Successfully Re-Logged :)", Toast.LENGTH_LONG).show()
                        Log.d("Profile", "user re-logged  ${Auth.currentUser?.uid}")
                    }else{
                        Toast.makeText(this, "Erro Re-Logged :)", Toast.LENGTH_LONG).show()
                        showAlertLogin()
                    }
                    Log.d("Profile", "done botao")
                }


        }

        val dialog = alertDialog.create()
        dialog.show()
    }






    private fun showAlertEmail(){
        val inflater = layoutInflater
        val inflate_view = inflater.inflate(R.layout.email_custom_view,null)

        val userEmailEdt = inflate_view.userNewEmail


        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("New Email")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel"){
            dialog, which ->
            Toast.makeText(this,"Cancel" , Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done"){
            dialog, which ->

            val user = FirebaseAuth.getInstance().currentUser
            val userEmail = userEmailEdt.text.toString()

            if (user != null) {
                if (!userEmail.isEmpty()) {

                    user?.updateEmail(userEmail)?.addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {
                            val pessoa = HashMap<String, Any>()
                            pessoa["email"] = userEmail
                            mAuth.collection("Users").document(user.uid).update(pessoa)
                            Toast.makeText(this, "Update email Success", Toast.LENGTH_LONG).show()
                            Log.d("Profile", "email update auth")
                            old()
                            showAlertLogin()

                        } else {
                            Toast.makeText(this, "Error email Update re-loggin try aggain", Toast.LENGTH_LONG).show()
                            Log.d("Profile", "email erro auth")
                            showAlertLogin()
                        }
                    }
                }
            }

             Log.d("Profile", "done botao")
        }

        val dialog = alertDialog.create()
        dialog.show()
    }



    private fun showAlertPass(){
        val inflater = layoutInflater
        val inflate_view = inflater.inflate(R.layout.pass_custom_view,null)

        val userPassEdt = inflate_view.userNewPass
        val userConfPassEdt = inflate_view.userConfPass


        val checkBoxTooggle = inflate_view.showPass

        checkBoxTooggle.setOnCheckedChangeListener{buttonView, isChecked ->
            if (!isChecked){
                userPassEdt.transformationMethod = PasswordTransformationMethod.getInstance()
                userConfPassEdt.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            else{
                userPassEdt.transformationMethod = null
                userConfPassEdt.transformationMethod = null
            }
        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("New Password")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel"){
                dialog, which ->
            Toast.makeText(this,"Cancel" , Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done"){
                dialog, which ->

            val user = FirebaseAuth.getInstance().currentUser
            val userPassword = userPassEdt.text.toString()
            val userConf = userConfPassEdt.text.toString()

            if (!userPassword.isEmpty() && !userConf.isEmpty()) {
                if (userConf == userPassword) {
                    user?.updatePassword(userPassword)?.addOnCompleteListener { task3 ->
                        if (task3.isSuccessful) {
                            Toast.makeText(this, "Update password Success", Toast.LENGTH_LONG)
                                .show()
                            Log.d("Profile", "password auth")
                            showAlertLogin()

                        } else {
                            Toast.makeText(this, "Error password Update", Toast.LENGTH_LONG).show()
                           showAlertLogin()
                        }
                    }
                }else {
                    Toast.makeText(this, "Password nao coincidem", Toast.LENGTH_LONG).show()
                    showAlertPass()
                }
            }else {
                Toast.makeText(this, "Campos nao preenchidos", Toast.LENGTH_LONG).show()
                showAlertPass()
            }

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
        val user = Auth.currentUser



            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("Profile", "Successfully upload image: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("Profile", "File localition: $it")

                        val p = it.toString()

                        if (user != null){
                            val pessoa = HashMap<String, Any>()
                            pessoa["Photo"] = p
                            mAuth.collection("Users").document(user.uid).update(pessoa)
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

        if (item!!.itemId == R.id.profile){

            startActivity(Intent (this, ProfileActivity :: class.java ))
        }

        if (item!!.itemId == R.id.grupo){

            startActivity(Intent (this, CriarGrupoActivity :: class.java ))
        }

        return super.onOptionsItemSelected(item)
    }

}
