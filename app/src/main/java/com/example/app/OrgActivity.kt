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
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_org.*
import kotlinx.android.synthetic.main.activity_ver_grupo.*

class OrgActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance()
    val Auth = FirebaseAuth.getInstance()

    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_org)

        val semGrupos = tNaoGrupos2
        val list = ListView3

        val user = Auth.currentUser
        val nome = mAuth.collection("Users").document(user?.uid.toString())

        semGrupos.isVisible = true
        if (user != null) {
            nome.get().addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.data?.get("name").toString()


                    val mail = mAuth.collection("Grupos")
                    mail.get().addOnSuccessListener { document ->
                        if (document != null) {

                            val values = ArrayList<String>()

                            for (grupo in document) {

                                //val gn = grupo.get("nome") as String
                                val admin2 = grupo.get("admin").toString()

                                if (admin2 == name) {

                                    semGrupos.isVisible = false


                                    gv = application as VariaveisGlobais



                                    values.add(grupo.get("nome").toString())


//                                        Log.d(
//                                            "VerGrupo",
//                                            "values $d"
//                                        )

                                    val adapter =
                                        ArrayAdapter(this, R.layout.listview_item, values)

                                    list.adapter = adapter
                                    Log.d(
                                        "VerGrupo",
                                        "Position :$adapter"
                                    )


                                    list.onItemClickListener =
                                        object : AdapterView.OnItemClickListener {


                                            override fun onItemClick(
                                                parent: AdapterView<*>, view: View,
                                                position: Int, id: Long
                                            ) {


                                                val itemValue =
                                                    list.getItemAtPosition(position) as String

                                                gv.Evento = itemValue
                                                gv.ver = itemValue


                                                startActivity(
                                                    Intent(
                                                        view.context, CriarOrgEventoActivity::class.java
                                                    )
                                                )

//                                    Toast.makeText(applicationContext,
//                                        "Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG)
//                                        .show()
                                                Log.d(
                                                    "VerGrupo",
                                                    "Positionffff :$list"
                                                )


                                            }

                                        }


                                }

                            }
                        }
                    }
                }////

//                    Log.d(
//                        "VerGrupo",
//                        " ${document.id} => ${document.data?.get("name")}, ${
//                            document.data?.get(
//                                "grupo"
//                            )
//                        }"
//                    )


//                    Log.d(
//                        "VerGrupo", "DocumentSnapshot data: ${document.data?.get("name")}"
//                    )
            }
        } else {
            Log.d("VerGrupo", "No such document")
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

