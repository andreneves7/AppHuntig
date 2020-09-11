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
import kotlinx.android.synthetic.main.activity_grupo.*
import kotlinx.android.synthetic.main.activity_ver_grupo.*




class VerGrupoActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance()
    val Auth = FirebaseAuth.getInstance()

    lateinit var gv: VariaveisGlobais




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_ver_grupo)




        val semGrupos = tNaoGrupos
        val list = ListView2

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {


                    val values = ArrayList<String>()




                    val ref = document.data?.get("grupo") as List<String>
                    Log.d(
                        "VerGrupo",
                        " ref $ref"
                    )

                    if (ref.isEmpty()) {
                        Log.d(
                            "VerGrupo",
                            " sem grupos deste user"
                        )

                        semGrupos.isVisible = true

                    } else {
                        Log.d(
                            "VerGrupo",
                            " grupos deste user"
                        )

                        semGrupos.isVisible = false

                        gv = application as VariaveisGlobais
                        for (grupo in ref) {
                            Log.d(
                                "VerGrupo",
                                "grupo  $grupo"
                            )


                            var d = values.add(grupo).toString()
                            Log.d(
                                "VerGrupo",
                                "values $d"
                            )

                            val adapter = ArrayAdapter(this,R.layout.listview_item, values)

                            list.adapter = adapter
                           Log.d("VerGrupo",
                                "Position :$adapter")

                            
                            list.onItemClickListener = object : AdapterView.OnItemClickListener {



                                override fun onItemClick(parent: AdapterView<*>, view: View,
                                                         position: Int, id: Long){


                                    val itemValue = list.getItemAtPosition(position) as String

                                    gv.Evento = itemValue
                                    gv.ver = itemValue


                                    startActivity(Intent (view.context, GrupoActivity :: class.java ))

//                                    Toast.makeText(applicationContext,
//                                        "Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG)
//                                        .show()
                                    Log.d("VerGrupo",
                                        "Positionffff :$list")


                                }

                            }

                            
                        }
                    }

                    Log.d(
                        "VerGrupo",
                        " ${document.id} => ${document.data?.get("name")}, ${document.data?.get("grupo")}"
                    )


                    Log.d(
                        "VerGrupo", "DocumentSnapshot data: ${document.data?.get("name")}"
                    )
                } else {
                    Log.d("VerGrupo", "No such document")
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
            //startActivity(Intent (this, MainActivity :: class.java ))
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this,VerGrupoActivity::class.java))
        }

        if (item.itemId == R.id.Lista) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }


        return super.onOptionsItemSelected(item)
    }
}
