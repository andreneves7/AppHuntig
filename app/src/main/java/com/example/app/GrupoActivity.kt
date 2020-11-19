package com.example.app

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_grupo.*
import java.sql.Time
import java.time.Year

class VariaveisGlobais : Application() {
    var Evento: String = ""
    var detalhes: String = ""
    var entrar: String = ""
    var ver: String = ""
    var nome: String = ""
    var Month: Int = 0
    var Day: Int = 0
    var Year: Int = 0
    var MonthFim: Int = 0
    var DayFim: Int = 0
    var YearFim: Int = 0
    var Lat: Double = 0.0
    var Long: Double = 0.0
    var check: String = ""
    var Horas: String = ""
    var privado: String = ""
    var extra: String = ""
    var Associacao: String = ""
    var numSocio: Int = 0
    var numEspanha: Int = 0


}

class GrupoActivity : AppCompatActivity() {

    lateinit var gv: VariaveisGlobais
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_grupo)

        val lista = ListView3

        val semEventos = tNaoEventos


        val user = Auth.currentUser




        if (user != null) {

            val valu = ArrayList<String>()

            val t = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE).toInt()
            val mail = mAuth.getReference("Eventos")
            val m = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {



                    val refe = dataSnapshot.child("numeroGrupo").getValue().toString().toInt()




                    Log.d(
                        "Grupo",
                        " refe $refe"
                    )

                    if (refe != t) {
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
                        valu.add(dataSnapshot.child("nome").getValue().toString())


                        val adapter = ArrayAdapter(this@GrupoActivity, R.layout.listview_item, valu)

                        lista.adapter = adapter

                        lista.onItemClickListener = object : AdapterView.OnItemClickListener {


                            override fun onItemClick(
                                parent: AdapterView<*>, view: View,
                                position: Int, id: Long
                            ) {

                                val itemValue = lista.getItemAtPosition(position)
                                gv.detalhes = itemValue as String
                                Log.d(
                                    "Grupo",
                                    "ffff :$itemValue"
                                )

                                var eve = mAuth.getReference("Eventos").child(itemValue.toString())

                                eve.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        startActivity(
                                            Intent(
                                                view.context,
                                                DetalhesEventoActivity::class.java
                                            )
                                        )
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })


//                                Toast.makeText(
//                                    this@GrupoActivity,
//                                    "Position :$position\nItem Value : $itemValue",
//                                    Toast.LENGTH_LONG
//                                )
//                                    .show()


                            }

                        }


                    }


                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Grupo", "No such document")
                }
            }
            mail.addChildEventListener(m)
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

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this, VerGrupoActivity::class.java))
        }

        if (item.itemId == R.id.Lis) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
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
}
