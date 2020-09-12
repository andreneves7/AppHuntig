package com.example.app

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
import kotlinx.android.synthetic.main.activity_criar_org_evento.*
import kotlinx.android.synthetic.main.activity_grupo.*

class CriarOrgEventoActivity : AppCompatActivity() {

    lateinit var gv: VariaveisGlobais
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_criar_org_evento)


        val lista = ListView4

        val semEventos = tNaoEventos2

        val evento = bEvento2

        val user = Auth.currentUser

        val mail = mAuth.collection("Grupos").document(gv.ver)
        mail.get().addOnSuccessListener { document ->
            if (document != null) {

                val admin = document.data?.get("admin").toString()

                val buscarEvento = mAuth.collection("Users").document(user?.uid.toString())
                buscarEvento.get().addOnSuccessListener { document ->
                    if (document != null) {

                        val nameUser = document.data?.get("name")
                        Log.d("grupo", "aaaa: $admin" +
                                "ffff: $nameUser")

                        //apos separar retirar
                        if (admin == nameUser ) {
                            evento.isVisible = true

                            Log.d("grupo", "aaaa: $admin" +
                                    "ffff: $nameUser"+ "\n" + "true")
                        }else{
                            evento.isVisible = false
                            Log.d("grupo", "aaaa: $admin" +
                                    "ffff: $nameUser"+ "\n" + "false")
                        }///
                    }
                }

                Log.d(
                    "evento", "DocumentSnapshot data: ${document.data?.get("admin")} "
                )
            } else {
                Log.d("evento", "No such document")
            }
        }







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

                            lista.adapter = adapter

                            lista.onItemClickListener = object : AdapterView.OnItemClickListener {



                                override fun onItemClick(parent: AdapterView<*>, view: View,
                                                         position: Int, id: Long){

                                    val itemValue = lista.getItemAtPosition(position) as String
                                    gv.detalhes = itemValue
                                    Log.d("Grupo",
                                        "ffff :$itemValue")

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








        evento.setOnClickListener{
            startActivity(Intent (this, EventoActivity :: class.java ))
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita_org, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.signOut2) {
            Auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }



        if (item.itemId == R.id.grupo2) {

            startActivity(Intent(this, OrgActivity::class.java))
        }



        return super.onOptionsItemSelected(item)
    }
}