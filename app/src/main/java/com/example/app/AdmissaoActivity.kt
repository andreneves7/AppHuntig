package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_admissao.*

class AdmissaoActivity : AppCompatActivity() {


    val mAuth = FirebaseDatabase.getInstance()
    val Auth = FirebaseAuth.getInstance()

    lateinit var lista: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admissao)

        lista = ListViewPendentes



        dados()
    }


    fun dados() {
        val user = Auth.currentUser?.uid
        if (user != null) {
            val mail = mAuth.getReference("Grupos")

            var values = ArrayList<Model>()
            val valor = ArrayList<String>()

            val j = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    //val g = dataSnapshot.child("nome").getValue().toString()
                    val admin = dataSnapshot.child("admin").getValue().toString()
                    val numero = dataSnapshot.child("Numero").getValue().toString()
                    Log.d("adesa", "numero= $numero")

                    Log.d(
                        "VerGrupo2",
                        "${user}"
                    )


//                    val m = mAuth.getReference("Grupos").child(g)

//                    Log.d(
//                        "VerGrupo2",
//                        " ${m}"
//                    )

                    val t = mAuth.getReference("Users")
                    if (admin == user) {

                        t.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                val teste = snapshot.children
                                //valor.add(teste.toString())
                                Log.d("adesa", "teste= $teste")
                                //Log.d("adesa", "valor= $valor")

                                for (i in teste) {
                                    Log.d("adesa", "t= ${i.key}")

                                    val nome =
                                        snapshot.child("${i.key}").child("name")
                                            .getValue().toString()
                                    Log.d("adesa", "nome= $nome")

                                    val carta =
                                        snapshot.child("${i.key}").child("Carta Caçadore")
                                            .getValue().toString()
                                    Log.d("adesa", "carta= $carta")


                                    val socio =
                                        dataSnapshot.child("Pendentes").child("${i.key}")
                                            .child("numero socio").getValue()
                                            .toString()

                                    Log.d("adesa", "g= $socio")



                                    values.add(Model(nome, carta.toInt(), socio.toInt()))




                                }
                                lista.adapter = MyListAdapter(
                                    this@AdmissaoActivity,
                                    R.layout.listview_item_pendentes,
                                    values)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }


                        })

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
                    TODO("Not yet implemented")
                }
            }
            mail.addChildEventListener(j)
        }

    }
}