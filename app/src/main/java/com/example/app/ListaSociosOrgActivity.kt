package com.example.app

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityListaSociosOrgBinding

class ListaSociosOrgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaSociosOrgBinding
    val mAuth = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()

    lateinit var lista: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaSociosOrgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lista = binding.ListViewSociosInscritos
        binding.progressListaSocios.isVisible = true
        binding.progressListaSocios.postDelayed({ binding.progressListaSocios.isVisible = false }, 5000)

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

                    binding.progressListaSocios.isVisible = false

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
                                Log.d("todo_fix", "erro Firebase: ${error.message}")
                                this@ListaSociosOrgActivity.mostrarErroLigacao()
                            }
                        })

                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressListaSocios.isVisible = false
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@ListaSociosOrgActivity.mostrarErroLigacao()
                }
            }
            mail.addChildEventListener(j)

        }


    }

}