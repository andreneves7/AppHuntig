package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.isVisible
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

        desativar()

        marcar.setOnClickListener {
            marcarPresença()
        }

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

                    Log.d(
                        "evento",
                        "DocumentSnapshot data: ${document.data?.get("nome")} \n${document.data?.get(
                            "data"
                        )}" +
                                " \n ${document.data?.get("hora")} \n ${document.data?.get("local")}"
                    )
                } else {
                    Log.d("evento", "No such document")
                }
            }
        }

    }

    public fun desativar() {

        val marcar = bPresença

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Eventos").document(gv.detalhes)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val admin = document.data?.get("Presenças") as List<String>

                        val buscarNome = mAuth.collection("Users").document(user?.uid)
                        buscarNome.get().addOnSuccessListener { document ->
                            if (document != null) {

                                val nameUser = document.data?.get("name")
                                if (admin.contains(nameUser)) {
                                    marcar.isVisible = false
                                }
                            }
                        }

                    Log.d(
                        "evento", "DocumentSnapshot data: ${document.data?.get("admin")} "
                    )
                } else {
                    Log.d("evento", "No such document")
                }
            }
        }

    }


    public fun marcarPresença(){

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Eventos").document(gv.detalhes)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val buscarNome = mAuth.collection("Users").document(user?.uid)
                    buscarNome.get().addOnSuccessListener { document ->
                        if (document != null) {

                            val nameUser = document.data?.get("name").toString()
                            val pessoa = HashMap<String, Any>()
                            pessoa["Presenças"] = nameUser

                            mAuth.collection("Eventos").document(gv.detalhes).update("Presenças", FieldValue.arrayUnion(pessoa))

                        }
                    }

                    Log.d(
                        "evento", "DocumentSnapshot data: ${document.data?.get("admin")} "
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
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        if (item!!.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item!!.itemId == R.id.grupo) {

            startActivity(Intent(this, CriarGrupoActivity::class.java))
        }

        if (item!!.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

}
