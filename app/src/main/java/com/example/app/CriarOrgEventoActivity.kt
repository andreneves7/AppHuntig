package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_criar_org_evento.*
import kotlinx.android.synthetic.main.activity_grupo.*

class CriarOrgEventoActivity : AppCompatActivity() {

    lateinit var gv: VariaveisGlobais
    val Auth = FirebaseAuth.getInstance()

    //val mAuth = FirebaseFirestore.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    var numero = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_criar_org_evento)

        val evento = bEvento
        val soc = bSocios

        busca()

        evento.setOnClickListener {

            Log.d("Numero", "ola = $numero")
            startActivity(Intent(this, EventoActivity::class.java).apply {
                putExtra(
                    EXTRA_MESSAGE,
                    numero.toString()
                )
            })
        }

       soc.setOnClickListener {

            Log.d("Numero", "ola = $numero")
            startActivity(Intent(this, AdmissaoActivity::class.java).apply {
                putExtra(
                    EXTRA_MESSAGE,
                    numero.toString()
                )
            })
        }

    }


    fun busca() {
        val semEventos = tNaoEventos2
        semEventos.isInvisible = true
        val user = Auth.currentUser
        if (user != null) {

            val valu = ArrayList<String>()

            val t = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE).toInt()
            val mail = mAuth.getReference("Eventos")
            val m = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    val nome = dataSnapshot.child("nome").getValue().toString()

                    mAuth.getReference("Eventos").child(nome)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                val refe =
                                    snapshot.child("numeroGrupo").getValue().toString().toInt()






                                Log.d(
                                    "Grupo",
                                    " refe $refe"
                                )

                                if (refe == t) {
                                    Log.d(
                                        "Grupo",
                                        " grupos deste user"
                                    )

                                    numero = refe
                                    semEventos.isVisible = false
                                    valu.add(snapshot.child("nome").getValue().toString())


                                    val adapter = ArrayAdapter(
                                        this@CriarOrgEventoActivity,
                                        R.layout.listview_item,
                                        valu
                                    )
                                    val lista = ListView4
                                    lista.adapter = adapter

                                    lista.onItemClickListener =
                                        object : AdapterView.OnItemClickListener {


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

                                                var eve = mAuth.getReference("Eventos")
                                                    .child(itemValue.toString())

                                                eve.addValueEventListener(object :
                                                    ValueEventListener {
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


                                } else {
                                    Log.d(
                                        "Grupo",
                                        " sem grupos deste user ${valu.size}"
                                    )
                                    if (valu.size == 0) {

                                        semEventos.isVisible = true
                                    }

                                }


                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
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