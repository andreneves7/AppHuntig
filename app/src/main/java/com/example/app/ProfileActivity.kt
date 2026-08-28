package com.example.app

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.app.databinding.ActivityProfileBinding
import com.example.app.databinding.CustomViewBinding
import com.example.app.databinding.EmailCustomViewBinding
import com.example.app.databinding.PassCustomViewBinding
import java.util.*

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance();


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val editar = binding.bEdit
        val editarPass = binding.bEditPass

        editarPass.setOnClickListener {
            showAlertPass()
        }


        editar.setOnClickListener(View.OnClickListener {
            showAlertEmail()

        })

        old()

    }

    private fun old() {
        val show = binding.textView
        val user = Auth.currentUser
        val uid = Auth.currentUser?.uid.toString()
        val userEmail = Auth.currentUser?.email
        val mail = mAuth.getReference("Users").child(uid)



        // buscar nome ao firebase realtime do user
        if (user != null) {
          mail.addValueEventListener(
          object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val n = snapshot.child("name").getValue().toString()
                    Log.d("Profile", "valor nome = $n")
                    show.text = "email: $userEmail\nname: $n"
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })


        }
    }

    private fun showAlertLogin() {
        val inflater = layoutInflater
        val dialogBinding = CustomViewBinding.inflate(inflater)
        val inflate_view = dialogBinding.root

        val userEmailEdt = dialogBinding.userEmail
        val userPassEdt = dialogBinding.userPass

        val checkBoxTooggle = dialogBinding.showPass

        checkBoxTooggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                userPassEdt.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                userPassEdt.transformationMethod = null
            }
        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Login novamente")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(this, this.getString(R.string.msg_cancel), Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done") { dialog, which ->

            val email = userEmailEdt.text.toString()
            val password = userPassEdt.text.toString()

            if (Auth.currentUser!!.isEmailVerified) {

            Auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task4 ->
                    if (task4.isSuccessful) {
                        Toast.makeText(this, this.getString(R.string.msg_successfully_re_logged), Toast.LENGTH_LONG).show()
                        Log.d("Profile", "user re-logged  ${Auth.currentUser?.uid}")
                    } else {
                        Toast.makeText(this, this.getString(R.string.msg_erro_re_logged), Toast.LENGTH_LONG).show()
                        showAlertLogin()
                    }
                    Log.d("Profile", "done botao")
                }
            }


        }

        val dialog = alertDialog.create()
        dialog.show()
    }


    private fun showAlertEmail() {
        val inflater = layoutInflater
        val dialogBinding = EmailCustomViewBinding.inflate(inflater)
        val inflate_view = dialogBinding.root

        val userEmailEdt = dialogBinding.userNewEmail


        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("New Email")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(this, this.getString(R.string.msg_cancel), Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done") { dialog, which ->

            val user = FirebaseAuth.getInstance().currentUser
            val userEmail = userEmailEdt.text.toString()
            val uid = user?.uid.toString()
            val mail = mAuth.getReference("Users").child(uid)

            if (user != null) {
                if (!userEmail.isEmpty()) {

                    user.updateEmail(userEmail).addOnCompleteListener { task2 ->
                        if (task2.isSuccessful) {

                           mail.child("email").setValue( userEmail)
                            Toast.makeText(this, this.getString(R.string.msg_update_email_success), Toast.LENGTH_LONG).show()
                            Log.d("Profile", "email update auth")
                            old()


                        } else {
                            Toast.makeText(this, this.getString(R.string.msg_error_email_update_re_loggin_try_aggain), Toast.LENGTH_LONG
                            ).show()
                            Log.d("Profile", "email erro auth")
                            showAlertLogin()
                        }
                        sendEmailVerification()
                    }
                }
            }

            Log.d("Profile", "done botao")
        }

        val dialog = alertDialog.create()
        dialog.show()
    }


    private fun showAlertPass() {
        val inflater = layoutInflater
        val dialogBinding = PassCustomViewBinding.inflate(inflater)
        val inflate_view = dialogBinding.root

        val userPassEdt = dialogBinding.userNewPass
        val userConfPassEdt = dialogBinding.userConfPass


        val checkBoxTooggle = dialogBinding.showPass

        checkBoxTooggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                userPassEdt.transformationMethod = PasswordTransformationMethod.getInstance()
                userConfPassEdt.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                userPassEdt.transformationMethod = null
                userConfPassEdt.transformationMethod = null
            }
        }

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("New Password")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(this, this.getString(R.string.msg_cancel), Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Done") { dialog, which ->

            val user = FirebaseAuth.getInstance().currentUser
            val userPassword = userPassEdt.text.toString()
            val userConf = userConfPassEdt.text.toString()

            if (!userPassword.isEmpty() && !userConf.isEmpty()) {
                if (userConf == userPassword) {
                    user?.updatePassword(userPassword)?.addOnCompleteListener { task3 ->
                        if (task3.isSuccessful) {
                            Toast.makeText(this, this.getString(R.string.msg_update_password_success), Toast.LENGTH_LONG)
                                .show()
                            Log.d("Profile", "password auth")


                        } else {
                            Toast.makeText(this, this.getString(R.string.msg_error_password_update), Toast.LENGTH_LONG).show()
                            showAlertLogin()
                        }
                    }
                } else {
                    Toast.makeText(this, this.getString(R.string.msg_password_nao_coincidem), Toast.LENGTH_LONG).show()
                    showAlertPass()
                }
            } else {
                Toast.makeText(this, this.getString(R.string.msg_campos_nao_preenchidos), Toast.LENGTH_LONG).show()
                showAlertPass()
            }

            Log.d("Profile", "done botao")
        }

        val dialog = alertDialog.create()
        dialog.show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.signOut) {
            Auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this,VerGrupoActivity::class.java))
        }

        if (item.itemId == R.id.Lis) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.eventosProximos) {
            startActivity(Intent(this, EventosProximosActivity::class.java))
        }

        if (item.itemId == R.id.checkInQR) {
            iniciarScanQR()
        }

        if (item.itemId == R.id.definicoes) {
            startActivity(Intent(this, DefinicoesActivity::class.java))
        }

        if (item.itemId == R.id.ajuda) {
            startActivity(Intent(this, AjudaActivity::class.java))
        }

        if (item.itemId == R.id.home) {
            val marca = 0
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!processarResultadoScanQR(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun sendEmailVerification() {
        val user = Auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Email Verfication")
            builder.setMessage("Please confirm email")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

}
