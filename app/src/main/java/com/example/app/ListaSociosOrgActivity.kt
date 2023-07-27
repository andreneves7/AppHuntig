package com.example.app

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.widget.ListView
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_lista_socios_org.*

class ListaSociosOrgActivity : AppCompatActivity() {

    val mAuth = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()

    lateinit var lista: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_socios_org)

        lista = ListViewSociosInscritos

        dados()

    }

    private fun dados() {

        val user = auth.currentUser?.uid
        val num = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE)?.toInt()
        val listaNumeroSocios = ArrayList<String>()

        val t = mAuth.getReference("Users")


        if (user != null) {
            val mail = mAuth.getReference("Grupos")

            val values = ArrayList<Model>()

            val j = object : ChildEventListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    val numeroGrupo = dataSnapshot.child("Numero").value.toString()
                    val nameGrupo = dataSnapshot.child("nome").value.toString()

                    if (num == numeroGrupo.toInt()) {

                        val membros = dataSnapshot.child("membros").value.toString()

                        Log.d("lista", "t= $membros")

//                        var separaVirgula = membros.split(',', '=') as ArrayList<String>
//
//                        Log.d("lista", "separa = ${separaVirgula}")

                        val separa = membros.split('{', ',', '=') as ArrayList<String>



                        Log.d("lista", "separa = $separa")

                        //remover {
                        separa.removeAt(0)

                        Log.d("lista", "separa = $separa")

                        Log.d("lista", "separa = ${separa.size}")

                        for (i in separa.indices step 2) {


                            listaNumeroSocios.add(separa[i])

                            Log.d("lista", "separa = ${separa[i]}")
                        }

                        Log.d("lista", "lista = $listaNumeroSocios")

                        t.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                for (i in listaNumeroSocios) {

                                    Log.d("lista", "socio = $i")

                                    val membro = dataSnapshot.child("membros")
                                        .child(i.trim().replace(" ", "")).value.toString()

                                    Log.d("lista", "socio = $membro")


                                    val nome = snapshot.child(membro).child("name").value
                                        .toString()

                                    Log.d("lista", "nome = $nome")

                                    val carta =
                                        snapshot.child(membro).child("Carta Caçadore")
                                            .value.toString()
                                    Log.d("lista", "carta= $carta")


                                    values.add(
                                        Model(
                                            nome, carta.toInt(), i.trim().replace(" ", "").toInt(),
                                            membro
                                        )
                                    )

                                    lista.adapter = MyListAdapter_ListaSocios(
                                        this@ListaSociosOrgActivity,
                                        R.layout.listview_item_pendentes,
                                        values
                                    )

                                    lista.setOnItemClickListener { parent, _, position, _ ->

                                        val elemnt = parent.getItemAtPosition(position) as Model

                                        Log.d(
                                            "lista",
                                            "fff :$elemnt"
                                        )


                                    }

                                }
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