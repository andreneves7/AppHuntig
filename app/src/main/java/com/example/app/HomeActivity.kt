package com.example.app

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.custom_view.view.*


class HomeActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.signOut){
            Auth.signOut()
            val intent = Intent(this, LoginActivity :: class.java )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //startActivity(Intent (this, MainActivity :: class.java ))
        }

        if (item!!.itemId == R.id.profile){





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

                alertDialog.setPositiveButton("ok"){
                        dialog, which ->

                    val userNewEmail = userEmailEdt.text.toString()
                    val userPassword = userPassEdt.text.toString()

                    Auth.signInWithEmailAndPassword(userNewEmail, userPassword)
                        .addOnCompleteListener { task4 ->
                            if (task4.isSuccessful) {
                                Toast.makeText(this, "Successfully Re-Logged :)", Toast.LENGTH_LONG).show()
                                Log.d("Home", "user re-logged  ${Auth.currentUser?.uid}")
                            }
                            Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
                            Log.d("Home", "done botao")
                            startActivity(Intent (this, ProfileActivity :: class.java ))
                        }

                }

                val dialog = alertDialog.create()
                dialog.show()



        }
        return super.onOptionsItemSelected(item)
    }



}