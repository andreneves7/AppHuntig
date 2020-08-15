package com.example.app

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registo_user.*
import org.jetbrains.anko.email
import java.lang.Exception
import java.util.*

const val EXTRA_MESSAGE ="ola"
class RegistoUserActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance().collection("Users")
    val Auth = FirebaseAuth.getInstance()
    val mStorage = FirebaseStorage.getInstance().reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo_user)

//       val regBtn = bNext

//        val imageView =findViewById<ImageView>(R.id.imageView)
//        Glide.with(this/*context*/).load(mStorage).into()


        val email= addEmail.text.toString()
        val password = addPass.text.toString()
        val name = addNome.text.toString()

//regBtn.setOnClickListener {   val editText = findViewById<EditText>(R.id.addEmail)
//    val message = editText.text.toString()
//    val intent = Intent(this, RegistoActivity::class.java).apply {
//        putExtra(EXTRA_MESSAGE, message)
//    }
//    startActivity(intent) }

        var g = ""
        val outros = addPais_Outros
        outros.isInvisible = true
        val btnPop = bPais_User

        val passaporte = addNumero_Passaporte
        passaporte.isInvisible = true


        btnPop.setOnClickListener{

            val popMenu = PopupMenu(this@RegistoUserActivity, btnPop)
            popMenu.menuInflater.inflate(R.menu.menu_pop2, popMenu.menu)
            popMenu.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item!!.itemId){

                        R.id.checkPortugal -> {"Portugal"
                            outros.isInvisible = true
                            passaporte.isInvisible = true
                            Log.d("RegistoUser", "putas")

                        }
                        R.id.checkOutros ->  {outros.isVisible = true
                            passaporte.isVisible = true
                        g = outros.text.toString().toUpperCase()
                            Log.d("RegistoUser", "$g")
                        }
                        R.id.checkEspanha -> {"Espanha"
                            outros.isInvisible = true
                            passaporte.isInvisible = true
                            Log.d("RegistoUser", "putas2")
                        }
                    }
                    return true
                }

            })
            popMenu.show()
        }


        //registoAuth(password, email, name)

    }






    private fun registoAuth(password: String, email: String, name: String) {

        if (!password.isEmpty() && !email.isEmpty() && !name.isEmpty()) {
            Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { it ->

                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegistoUser", "user auth com uid: ${it.result?.user?.uid}")
                register(name, email)
                when {
                    it.isSuccessful -> {
                        Toast.makeText(this, "Registo COM sucesso", Toast.LENGTH_SHORT).show()
                        Auth.signOut()
                    }
                    else -> {
                        Toast.makeText(this, "Registo sem sucesso", Toast.LENGTH_SHORT).show()
                    }
                }


            }


                .addOnFailureListener { exception: Exception ->
                    Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                }
        }else
        {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            Log.d("RegistoUser", "nao registo")
        }

    }




private fun register(name: String, email: String) {

    val uid = Auth.uid.toString()
    val ref = mAuth.document("$uid")

    val pessoa = HashMap<String, Any>()
    pessoa["uid"] = uid
    pessoa["name"] = name
    pessoa["email"] = email
    pessoa["grupo"] = ArrayList<String>()
    ref.set(pessoa)
    Log.d("RegistoUser", "user firestore registo")


    sendEmailVerification()
    Log.d("RegistoUser", "email enviado")
    clearInputs()
    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)


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
            )
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}


private fun clearInputs() {
    addNome.text.clear()
    addEmail.text.clear()
    addPass.text.clear()
}
}

