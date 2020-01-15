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
import kotlinx.android.synthetic.main.activity_evento.*
import java.util.*


class EventoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    val gAuth = FirebaseFirestore.getInstance().collection("Grupo")
    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = getApplication() as VariaveisGlobais
        setContentView(R.layout.activity_evento)

        val guardarEvento = bEvento


        guardarEvento.setOnClickListener {
            evento()
        }

    }

    private fun evento(){

        val nome = edNome.text.toString()
        val horas = edTime
        val data = edData
        val local = edLocal

        val documentId = mAuth.collection("Users").document().id
        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val name = document.data?.get("name")
//                    val grupo = mAuth.collection("Grupo").

                    val evento = HashMap<String, Any>()
                    evento["nome"] = nome
                    evento["Presenças"] = arrayListOf(name)
                    evento["data"] = data.text.toString()
                    evento["horas"] = horas.text.toString()
                    evento["local"] = local.text.toString()
                    mAuth.collection("Eventos").document(nome)
                        .set(evento)

                    val up = HashMap<String, Any>()
                    up["Eventos"] = arrayListOf(nome)
                    mAuth.collection("Grupos").document(gv.Evento)
                        .update("Eventos", FieldValue.arrayUnion(nome))

                    Toast.makeText(this, "evento criado", Toast.LENGTH_SHORT).show()


                    val intent = Intent(this,  GrupoActivity:: class.java )
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    Log.d("evento","DocumentSnapshot data: ${document.data?.get("name")}"
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
        if (item!!.itemId == R.id.signOut){
            Auth.signOut()
            val intent = Intent(this, LoginActivity :: class.java )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //startActivity(Intent (this, MainActivity :: class.java ))
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

