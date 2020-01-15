package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detalhes_evento.*
import java.util.HashMap

class DetalhesEventoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = getApplication() as VariaveisGlobais
        setContentView(R.layout.activity_detalhes_evento)

        val showDetalhe = tShowDetalhes
        val marcar = bPresença

        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Eventos").document(gv.detalhes)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val name = document.data?.get("nome")
                    val date = document.data?.get("data")
                    val time = document.data?.get("horas")
                    val place = document.data?.get("local")



                    showDetalhe.setText("nome: " + name + "\n" + "data: " + date + "\n" + "horas: " + time + "\n" + "local: " + place + "\n")

                    Log.d("evento","DocumentSnapshot data: ${document.data?.get("nome")} \n${document.data?.get("data")}" +
                            " \n ${document.data?.get("hora")} \n ${document.data?.get("local")}"
                    )
                } else {
                    Log.d("evento", "No such document")
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

        if (item!!.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

}
