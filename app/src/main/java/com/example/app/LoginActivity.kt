package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap


class LoginActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance()
    val Auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val loginBtn = bLogin
        val regTxt = bRegisto

        loginBtn.setOnClickListener(View.OnClickListener { view ->
            login()
        })

        regTxt.setOnClickListener(View.OnClickListener { view ->
            register()
        })
    }

    private fun login() {
        val emailTxt = idEmail
        val passwordTxt = idPassword

        var email = emailTxt.text.toString()
        var password = passwordTxt.text.toString()






        if (!email.isEmpty() && !password.isEmpty()) {
            Auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (Auth.currentUser!!.isEmailVerified) {
                        val ver = mAuth.collection("Users").document(Auth.currentUser!!.uid)
                        ver.get().addOnSuccessListener { document ->

                            if (document != null) {
                                val v = document.data?.get("firstTime")

                                Log.d("Login", "user primeira ${v}")

                                //verifica se a conta esta ser inicializada pela primeira vez
                                if (v == true) {
                                    val intent = Intent(this, PreferenciasActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    val p = HashMap<String, Any>()
                                    p["firstTime"] = false
                                    ver.update(p)
                                } else {
                                    val intent = Intent(this, FiltrosActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    //startActivity(Intent(this, home::class.java))
                                }

                                Toast.makeText(this, "Successfully Logged in :)", Toast.LENGTH_LONG)
                                    .show()
                                Log.d("Login", "user ${Auth.currentUser?.uid}")
                            }
                        }
                    } else {
                        Toast.makeText(this, "verifique email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error Logging in :(", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please fill up the credetianls", Toast.LENGTH_LONG).show()
        }
    }

    private fun register() {

        val intent = Intent(this, RegistoUserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


}
