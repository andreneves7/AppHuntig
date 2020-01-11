package com.example.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registo_user.*
import java.lang.Exception
import java.util.*

class RegistoUserActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance().collection("Users")
    val Auth = FirebaseAuth.getInstance()
    val mStorage = FirebaseStorage.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo_user)

        val regBtn = bSingUp






        regBtn.setOnClickListener {

            val email = addEmail.text.toString()
            val password = addPass.text.toString()
            val name = addNome.text.toString()

            registoAuth(password, email, name)

        }

    }





   private fun registoAuth(password: String, email: String, name: String){

        if (!password.isEmpty() && !email.isEmpty() && !name.isEmpty())  {
            Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { it ->

                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d( "RegistoUser", "user auth com uid: ${it.result?.user?.uid}")
                Toast.makeText(this, "Successfully signed in", Toast.LENGTH_LONG).show()
                register(name, email)

            }.addOnFailureListener { exception: Exception ->
                Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
            }

        }

        else {
            Toast.makeText(this, "Please", Toast.LENGTH_LONG).show()
            Log.d( "RegistoUser", "nao registo")
        }

    }




   private  fun register (name: String, email: String){

       val uid = Auth.uid ?:""
       val ref = mAuth.document("$uid")

       val pessoa = HashMap<String, Any>()
       pessoa["name"] = name
       pessoa["email"] = email
       pessoa["grupos"] = ArrayList<String>()
       //pessoa["lastLocation"] = Timestamp.now()
       ref.set(pessoa)
       Log.d("RegistoUser","user firestore registo")


       clearInputs()
       val intent = Intent(this, LoginActivity :: class.java )
       intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
       startActivity(intent)

   }




    /*private fun sendEmailVerification(){
        val user = Auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener{

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Email Verfication")
            builder.setMessage("Please confirm email")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT)
                startActivity(Intent (this, MainActivity :: class.java ))
            }
            builder.show()
        }
    }*/

    private fun clearInputs(){
        addNome.text.clear()
        addEmail.text.clear()
        addPass.text.clear()
    }
}

