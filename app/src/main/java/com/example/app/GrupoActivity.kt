package com.example.app

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_grupo.*

class VariaveisGlobais : Application() {
    var Evento: String =""
    var detalhes: String=""
    var entrar: String=""

}

class GrupoActivity : AppCompatActivity() {

    lateinit var gv: VariaveisGlobais
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = getApplication() as VariaveisGlobais
        setContentView(R.layout.activity_grupo)

        val lista = ListView3

        val semEventos = tNaoEventos




        desativar()



        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Grupos").document(gv.Evento)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {


                    val valu = ArrayList<String>()


                    val refe = document.data?.get("Eventos") as List<String>
                    Log.d(
                        "Grupo",
                        " refe $refe"
                    )

                    if (refe.isEmpty()) {
                        Log.d(
                            "Grupo",
                            " sem grupos deste user"
                        )

                        semEventos.isVisible = true

                    } else {
                        Log.d(
                            "Grupo",
                            " grupos deste user"
                        )

                        semEventos.isVisible = false

                        for (evento in refe) {
                            Log.d(
                                "Grupo",
                                "grupo  $evento"
                            )


                            var d = valu.add(evento).toString()
                            Log.d(
                                "Grupo",
                                "values $d"
                            )

                            val adapter = ArrayAdapter(this,R.layout.listview_item, valu)

                            lista.setAdapter(adapter)

                            lista.onItemClickListener = object : AdapterView.OnItemClickListener {



                                override fun onItemClick(parent: AdapterView<*>, view: View,
                                                         position: Int, id: Long){

                                    val itemValue = lista.getItemAtPosition(position) as String
                                    gv.detalhes = itemValue

                                    startActivity(Intent (view.context, DetalhesEventoActivity :: class.java ))

                                    Toast.makeText(applicationContext,
                                        "Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG)
                                        .show()


                                }

                            }


                        }
                    }

                    Log.d(
                        "Grupo",
                        " ${document.id} => ${document.data?.get("name")}, ${document.data?.get("grupo")}"
                    )


                    Log.d(
                        "Grupo", "DocumentSnapshot data: ${document.data?.get("name")}"
                    )
                } else {
                    Log.d("Grupo", "No such document")
                }
            }
        }






        val evento = bEvento

        evento.setOnClickListener{
            startActivity(Intent (this, EventoActivity :: class.java ))
        }



    }

    public fun desativar() {

        val evento = bEvento

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Grupos").document(gv.Evento)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val admin = document.data?.get("admin").toString()

                    val buscarEvento = mAuth.collection("Users").document(user?.uid)
                    buscarEvento.get().addOnSuccessListener { document ->
                        if (document != null) {

                            val nameUser = document.data?.get("name")
                            if (admin != nameUser) {
                                evento.isVisible = false
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

        if (item!!.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }


        return super.onOptionsItemSelected(item)
    }
}
