package com.example.app

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class LoginActivity : AppCompatActivity() {

//    val mAuth = FirebaseFirestore.getInstance()

    val mAuth = FirebaseDatabase.getInstance()
    val Auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val loginBtn = bLogin
        val loginOrg = bLoginOrg
        val regTxt = bRegisto

        loginOrg.setVisibility(View.INVISIBLE)

        loginBtn.setOnClickListener(View.OnClickListener { view ->
            login()
        })

        regTxt.setOnClickListener(View.OnClickListener { view ->
            register()
        })

        loginOrg.setOnClickListener(View.OnClickListener { view ->
            loginOrg()
        })
    }

    fun loginOrg() {
        val emailTxt = idEmail
        val passwordTxt = idPassword

        var email = emailTxt.text.toString()
        var password = passwordTxt.text.toString()

        if (!email.isEmpty() && !password.isEmpty()) {
            Auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (Auth.currentUser!!.isEmailVerified) {


                        val ver = mAuth.getReference("Users").child(Auth.currentUser!!.uid)
                        ver.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                val org = dataSnapshot.child("Org").getValue()





                                if (org == true) {


                                    val intent = Intent(this@LoginActivity, OrgActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)


                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Successfully Logged in :)",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    Log.d("Login", "user ${Auth.currentUser?.uid}")
                                } else {

                                    Toast.makeText(
                                        this@LoginActivity,
                                        "So pode entrar com caçador",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.d("Login", "fail dados")
                            }
                        })


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

    private fun login() {
        val emailTxt = idEmail
        val passwordTxt = idPassword

        var email = emailTxt.text.toString()
        var password = passwordTxt.text.toString()


        val uid = Auth.currentUser?.uid

        Log.d("Login", "user ${uid}")

        if (!email.isEmpty() && !password.isEmpty()) {

            Auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (Auth.currentUser!!.isEmailVerified) {
                            val ver = mAuth.getReference("Users").child(Auth.currentUser!!.uid)
                            ver.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // This method is called once with the initial value and again
                                    // whenever data at this location is updated.
                                    val org = dataSnapshot.child("Org").getValue()
                                    val controlo = dataSnapshot.child("Controlo").getValue()


                                    Log.d("Login", "user ${Auth.currentUser?.uid}")

                                    if (controlo == true) {

                                        if (org == false) {


                                            ver.addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(first: DataSnapshot) {


                                                    val v = first.child("FirstTime").getValue()

                                                    Log.d("Login", "user primeira ${v}")


                                                    //verifica se a conta esta ser inicializada pela primeira vez
                                                    if (v == true) {


                                                        val intent =
                                                            Intent(
                                                                this@LoginActivity,
                                                                PreferenciasActivity::class.java
                                                            )
                                                        intent.flags =
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(
                                                                Intent.FLAG_ACTIVITY_NEW_TASK
                                                            )
                                                        startActivity(intent)

//                                                    val p = HashMap<String, Any>()
//                                                    p["FirstTime"] = false
//                                                    ver.updateChildren(p)
                                                    } else {

                                                        val marca = 0

                                                        val intent =
                                                            Intent(
                                                                this@LoginActivity,
                                                                FiltrosActivity::class.java
                                                            ).apply {
                                                                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
                                                            }
                                                        intent.flags =
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(
                                                                Intent.FLAG_ACTIVITY_NEW_TASK
                                                            )
                                                        startActivity(intent)
                                                        //startActivity(Intent(this, home::class.java))
                                                    }


                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.d("Login", "fail dados")
                                                }
                                            })

                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                "Esta fazer login errado mudar para Organização",
                                                Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    } else {

                                        Toast.makeText(
                                            this@LoginActivity,
                                            "Tem esperar pela aprovaçao",
                                            Toast.LENGTH_SHORT
                                        ).show();

                                    }

                                }


                                override fun onCancelled(error: DatabaseError) {
                                    // Failed to read value
                                    Log.d("Login", "fail dados")
                                }


                            })
                            Toast.makeText(
                                this,
                                "Successfully Logged in :)",
                                Toast.LENGTH_LONG
                            )
                                .show()
                            Log.d("Login", "user ${Auth.currentUser?.uid}")


                        } else {
                            Toast.makeText(this, "verifique email", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Error Logging in :(", Toast.LENGTH_SHORT)
                            .show()
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val regTxt = bRegisto
        val loginBtn = bLogin
        val loginOrg = bLoginOrg



        if (item!!.itemId == R.id.Cacador) {

            regTxt.setVisibility(View.VISIBLE)
            loginBtn.setVisibility(View.VISIBLE)
            loginOrg.setVisibility(View.INVISIBLE)

        }

        if (item.itemId == R.id.Organizacao) {

            regTxt.setVisibility(View.INVISIBLE)
            loginBtn.setVisibility(View.INVISIBLE)
            loginOrg.setVisibility(View.VISIBLE)
        }



        return super.onOptionsItemSelected(item)
    }


}
