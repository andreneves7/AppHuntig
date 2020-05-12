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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_adesao.*
import kotlinx.android.synthetic.main.custom_view.view.*


class AdesaoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    lateinit var gv: VariaveisGlobais


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_adesao)

        val texto = tInfo
        val botao = bEntrar


        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Grupos").document(gv.entrar)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val name = document.data?.get("nome")



                    texto.text = "nome: " + name

                    Log.d(
                        "adesao", "DocumentSnapshot data: ${document.data?.get("nome")} }"
                    )
                } else {
                    Log.d("adesao", "No such document")
                }
            }
        }

        botao.setOnClickListener {

            showAlert()
        }


    }


    private fun showAlert() {

        val cod = grupoCodigo
        val codigo = cod.text.toString()

        val c = mAuth.collection("Grupos").document(gv.entrar)
        c.get().addOnSuccessListener { document ->
            if (document != null) {

                val verdade = document.data?.get("Codigo")
                Log.d("adesao", "codigo: ${verdade} ")

                if (codigo == verdade) {
                    val use = Auth.currentUser
                    mAuth.collection("Grupos").document(gv.entrar)
                        .update("membros", FieldValue.arrayUnion(use?.uid))
                    mAuth.collection("Users").document(use!!.uid)
                        .update("grupo", FieldValue.arrayUnion(gv.entrar))

                    val intent = Intent(this, VerGrupoActivity :: class.java )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)


                }
                Log.d("adesao", "DocumentSnapshot data: ${gv.entrar} ")

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
            val intent = Intent(this, LoginActivity :: class.java )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this, CriarGrupoActivity::class.java))
        }

        if (item.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }
}
