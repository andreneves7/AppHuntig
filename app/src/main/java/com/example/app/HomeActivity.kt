package com.example.app

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_evento.*
//import kotlinx.android.synthetic.main.activity_evento.checkDiasCaça
//import kotlinx.android.synthetic.main.activity_evento.checkEspera
//import kotlinx.android.synthetic.main.activity_evento.checkMontaria
//import kotlinx.android.synthetic.main.activity_evento.checkRola
//import kotlinx.android.synthetic.main.activity_evento.checkTordo
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.filtros_custom_view.*
import kotlinx.android.synthetic.main.filtros_custom_view.view.*
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_home)

        //val lista = ListView4

        //val escolherFiltros = filtros

        eventos()
        /*escolherFiltros.setOnClickListener {
            lista.setAdapter(null);
            showFiltros()
        }*/


    }

    fun eventos() {
        val semEventos = NaoEventos

        val lista = ListViewHome
        val pesquisa = SearchEvento
        val filtro = intent.getStringExtra(EXTRA_MESSAGE)
        val values = ArrayList<String>()
        var ListaEventosPrivat = mAuth.getReference("Eventos")
        var ListaEventosPublic = mAuth.getReference("Eventos")


        val public = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {


                var tipo = dataSnapshot.child("Tipo").getValue().toString()

                if (tipo == filtro || filtro == "tudo") {

                    semEventos.isVisible = false
                    val anoAtual =
                        Calendar.getInstance().get(Calendar.YEAR)
                    val mesAtual =
                        Calendar.getInstance().get(Calendar.MONTH) + 1
                    val diaAtual =
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                    val ano = dataSnapshot.child("anoFim").getValue().toString().toInt()
                    val mes = dataSnapshot.child("mesFim").getValue().toString().toInt()
                    val dia = dataSnapshot.child("diaFim").getValue().toString().toInt()



                    if (anoAtual < ano) {

                        val f = dataSnapshot.child("Forma").getValue().toString()
                        if (f == "publico") {
                            Log.d(
                                "home2",
                                "${
                                    dataSnapshot.child("nome").getValue()
                                        .toString()
                                },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
                            )
                            values.add(dataSnapshot.child("nome").getValue().toString())
                        }

                    } else if (anoAtual == ano) {

                        if (mesAtual < mes) {
                            val f = dataSnapshot.child("Forma").getValue().toString()
                            if (f == "publico") {
                                Log.d(
                                    "home2",
                                    "${
                                        dataSnapshot.child("nome").getValue()
                                            .toString()
                                    },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
                                )
                                values.add(dataSnapshot.child("nome").getValue().toString())
                            }
                        } else if (mesAtual == mes) {
                            if (diaAtual <= dia) {
                                val f = dataSnapshot.child("Forma").getValue().toString()
                                if (f == "publico") {
                                    Log.d(
                                        "home2",
                                        "${
                                            dataSnapshot.child("nome").getValue()
                                                .toString()
                                        },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
                                    )
                                    values.add(dataSnapshot.child("nome").getValue().toString())
                                }
                            }
                        }

                    }


//                                    val f = evento.get("Forma")
//                                    if (f == "publico") {
//                                        Log.d(
//                                            "home2",
//                                            "${
//                                                evento.get("nome")
//                                                    .toString()
//                                            },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
//                                        )
//                                        values.add(evento.get("nome").toString())
//                                    }


                }

                Log.d("home5", "$values")


                val adapter =
                    ArrayAdapter(this@HomeActivity, R.layout.listview_itemhome, values)

                lista.adapter = adapter



                lista.onItemClickListener =
                    object : AdapterView.OnItemClickListener {


                        override fun onItemClick(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {


                            val itemValue =
                                lista.getItemAtPosition(position) as String
                            Log.d("home", "grupoID to search: $itemValue")
                            gv.detalhes = itemValue
                            val uid = Auth.currentUser?.uid
                            var eventoclick2 =
                                mAuth.getReference("Eventos").child(itemValue)
                            eventoclick2.addValueEventListener(object : ValueEventListener {
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


//                                            Toast.makeText(
//                                                applicationContext,
//                                                "Position :$position\nItem Value : $itemValue",
//                                                Toast.LENGTH_LONG
//                                            ).show()


                        }

                    }
//                var x = 0
//                for (evento in result) {
//
//                    x += 1
//                }
//                if (x > 0) {
//                    semEventos.isVisible = false
//                } else {
//
//                    semEventos.isVisible = true
//                    Toast.makeText(
//                        applicationContext,
//                        "Sem eventos disponiveis", Toast.LENGTH_LONG
//                    ).show()
//                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
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
        ListaEventosPublic.addChildEventListener(public)

        var gruposMemmbros = mAuth.getReference("Grupos")
        Log.d(
            "home75",
            "gr : ${
                gruposMemmbros
            }"
        )

        val teste = ArrayList<String>()
        val gm = object : ChildEventListener {
            override fun onChildAdded(
                dataSnapshot: DataSnapshot,
                previousChildName: String?
            ) {

                val uid = Auth.currentUser?.uid

                val g = dataSnapshot.child("nome").getValue().toString()
                Log.d(
                    "home75",
                    "g : ${
                        g
                    }"
                )

                teste.add(g)

                Log.d(
                    "home75",
                    "teste: ${
                        teste
                    }"
                )

                var m = mAuth.getReference("Grupos").child(g)


                var t = mAuth.getReference("Grupos").child(g).child("membros")

                Log.d(
                    "home75",
                    "m : ${
                        m
                    }"
                )

                val f = object : ChildEventListener {
                    override fun onChildAdded(
                        dataSnapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        var j = dataSnapshot.getValue().toString()


                        Log.d(
                            "home75",
                            "j : ${
                                j
                            }"
                        )

                        var fazParte = ArrayList<String>()

                        fazParte.add(j)

                        Log.d(
                            "home75",
                            "f : ${
                                fazParte
                            }"
                        )

                        if (fazParte.contains(uid)) {


                            m.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {


                                    val n = snapshot.child("Numero").getValue().toString().toInt()


                                    val private = object : ChildEventListener {
                                        override fun onChildAdded(
                                            dataSnapshot: DataSnapshot,
                                            previousChildName: String?
                                        ) {


                                            val l = dataSnapshot.child("numeroGrupo").getValue()
                                                .toString().toInt()

                                            Log.d(
                                                "home75",
                                                "l :${
                                                    l
                                                }"
                                            )

                                            val ano =
                                                dataSnapshot.child("anoFim").getValue().toString()
                                                    .toInt()
                                            val mes =
                                                dataSnapshot.child("mesFim").getValue().toString()
                                                    .toInt()
                                            val dia =
                                                dataSnapshot.child("diaFim").getValue().toString()
                                                    .toInt()
                                            var tipo =
                                                dataSnapshot.child("Tipo").getValue().toString()

                                            val f =
                                                dataSnapshot.child("Forma").getValue()
                                                    .toString()


                                            val nome = dataSnapshot.child("nome").getValue()
                                                .toString()


//                                            val fodasse = m.child(n)


                                            if (n == l) {

                                                Log.d(
                                                    "home75",
                                                    "n :${
                                                        n
                                                    }"
                                                )



                                                if (tipo == filtro || filtro == "tudo") {



                                                    semEventos.isVisible = false
                                                    val anoAtual =
                                                        Calendar.getInstance().get(Calendar.YEAR)
                                                    val mesAtual =
                                                        Calendar.getInstance()
                                                            .get(Calendar.MONTH) + 1
                                                    val diaAtual =
                                                        Calendar.getInstance()
                                                            .get(Calendar.DAY_OF_MONTH)


                                                    // PROBLEMA NA VERIFICAÇAO DO DIA
                                                    if (anoAtual < ano) {

                                                        if (f == "privado") {
                                                            values.add(
                                                                nome
                                                            )

                                                        }

                                                    } else if (anoAtual == ano) {
                                                        if (mesAtual < mes) {
//                                                    val f =
//                                                        dataSnapshot.child("Forma").getValue()
//                                                            .toString()
                                                            if (f == "privado") {
                                                                values.add(
                                                                    nome
                                                                )

                                                            }
                                                        } else if (mesAtual == mes) {
                                                            if (diaAtual <= dia) {

//                                                        val f =
//                                                            dataSnapshot.child("Forma").getValue()
//                                                                .toString()
                                                                if (f == "privado") {
                                                                    Log.d(
                                                                        "home75",
                                                                        "nome :${
                                                                            nome
                                                                        }"
                                                                    )
                                                                    values.add(
                                                                        nome
                                                                    )
                                                                    Log.d(
                                                                        "home75",
                                                                        "v :${
                                                                            values
                                                                        }"
                                                                    )

                                                                }
                                                            }
                                                        }

                                                    }

                                                }


                                            }//


                                            Log.d("home75", "$values")


                                            val adapter =
                                                ArrayAdapter(
                                                    this@HomeActivity,
                                                    R.layout.listview_item,
                                                    values
                                                )

                                            lista.adapter = adapter
                                            pesquisa.setOnQueryTextListener(object :
                                                SearchView.OnQueryTextListener {
                                                override fun onQueryTextSubmit(query: String): Boolean {

                                                    return false
                                                }

                                                override fun onQueryTextChange(newText: String): Boolean {

                                                    adapter.filter.filter(newText)
                                                    return false
                                                }
                                            })

                                            lista.onItemClickListener =
                                                object : AdapterView.OnItemClickListener {


                                                    override fun onItemClick(
                                                        parent: AdapterView<*>,
                                                        view: View,
                                                        position: Int,
                                                        id: Long
                                                    ) {


                                                        val itemValue =
                                                            lista.getItemAtPosition(position) as String
                                                        Log.d(
                                                            "home44",
                                                            "grupoID to search: $itemValue"
                                                        )
                                                        gv.detalhes = itemValue
                                                        val uid = Auth.currentUser?.uid
                                                        var eventoClick =
                                                            mAuth.getReference("Eventos")
                                                                .child(itemValue)
                                                        eventoClick.addValueEventListener(object :
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


//                                            Toast.makeText(
//                                                applicationContext,
//                                                "Position :$position\nItem Value : $itemValue",
//                                                Toast.LENGTH_LONG
//                                            ).show()


                                                    }

                                                }


                                        }

                                        override fun onChildChanged(
                                            snapshot: DataSnapshot,
                                            previousChildName: String?
                                        ) {
                                            startActivity(
                                                Intent(
                                                    this@HomeActivity,
                                                    HomeActivity::class.java
                                                )
                                            )
                                        }

                                        override fun onChildRemoved(snapshot: DataSnapshot) {
                                            TODO("Not yet implemented")
                                        }

                                        override fun onChildMoved(
                                            snapshot: DataSnapshot,
                                            previousChildName: String?
                                        ) {
                                            TODO("Not yet implemented")
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

//                            var x = 0
//                            for (evento in result) {
//
//                                x += 1
//                            }
//                            if (x > 0) {
//                                semEventos.isVisible = false
//                            } else {
//
//                                semEventos.isVisible = true
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Sem eventos disponiveis", Toast.LENGTH_LONG
//                                ).show()
//                            }
//
//                        }
                                    }
//
//

                                    ListaEventosPrivat.addChildEventListener(private)

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }



                            })


                        }//
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(
                        snapshot: DataSnapshot,
                        previousChildName: String?
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
                t.addChildEventListener(f)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
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
        gruposMemmbros.addChildEventListener(gm)


        // filtros de pesquisa

        /* val checkedTiposArray = booleanArrayOf(false, false, false, false, false)
         private fun showFiltros() {
     //        val inflater = layoutInflater
     //        val inflate_view = inflater.inflate(R.layout.filtros_custom_view, null)


             val uid = Auth.currentUser?.uid
             val semEventos = NaoEventos
             val lista = ListView4
             val tipos = arrayOf("Montaria", "Espera", "Tordos", "Rolas", "Dias Caça")



     //        val diainflate = inflate_view.edDia
     //        val mesinflate = inflate_view.edMes
     //        val anoinflate = inflate_view.edAno
     //
     //        val dia = diainflate.text.toString()
     //        val mes = mesinflate.text.toString()
     //        val ano = anoinflate.text.toString()


             val alertDialog = AlertDialog.Builder(this)
             alertDialog.setTitle("Filtros por Tipo")
             //alertDialog.setView(inflate_view)
             alertDialog.setCancelable(false)

             alertDialog.setNegativeButton("Limpar") { dialog, which ->
                 Toast.makeText(this, "Limpar", Toast.LENGTH_LONG).show()
                 eventos()

             }
             alertDialog.setMultiChoiceItems(tipos, checkedTiposArray) { dialog, which, isChecked ->
                 checkedTiposArray[which] = isChecked

             }


             alertDialog.setPositiveButton("Done") { dialog, which ->
                 var gruposMemmbros = mAuth.collection("Grupos")
                 gruposMemmbros.get().addOnSuccessListener { result ->
                     if (result != null) {
                         for (grupo in result) {

                             var fazParte = grupo.get("membros") as List<String>
                             if (fazParte.contains(uid)) {

                                 var ListaEventosPrivat = mAuth.collection("Eventos")
                                 ListaEventosPrivat.get().addOnSuccessListener { result ->
                                     if (result != null) {
                                         val values = ArrayList<String>()

                                         for (evento in result) {
                                             semEventos.isVisible = false


                                             for (i in checkedTiposArray.indices) {
                                                 val checked = checkedTiposArray[i]
                                                 if (checked) {
                                                     val x = tipos[i]

                                                     if (evento.get("Tipo").toString() == x) {
                                                         Log.d("merda", x)
                                                         values.add(evento.get("nome").toString())
                                                     }
                                                 }
                                             }

                                         }
                                         Log.d("home", "$values")


                                         val adapter = ArrayAdapter(this, R.layout.listview_item, values)

                                         lista.adapter = adapter


                                         lista.onItemClickListener =
                                             object : AdapterView.OnItemClickListener {


                                                 override fun onItemClick(
                                                     parent: AdapterView<*>,
                                                     view: View,
                                                     position: Int,
                                                     id: Long
                                                 ) {


                                                     val itemValue =
                                                         lista.getItemAtPosition(position) as String
                                                     Log.d("home", "grupoID to search: $itemValue")
                                                     gv.detalhes = itemValue
                                                     val uid = Auth.currentUser?.uid
                                                     var eventoClick =
                                                         mAuth.collection("Eventos").document(itemValue)
                                                     eventoClick.get().addOnSuccessListener { result ->
                                                         if (result != null) {

                                                             startActivity(
                                                                 Intent(
                                                                     view.context,
                                                                     DetalhesEventoActivity::class.java
                                                                 )
                                                             )


                                                         }
                                                     }


     //                                            Toast.makeText(
     //                                                applicationContext,
     //                                                "Position :$position\nItem Value : $itemValue",
     //                                                Toast.LENGTH_LONG
     //                                            ).show()


                                                 }

                                             }
                                     }
                                     var x = 0
                                     for (evento in result) {

                                         x += 1
                                     }
                                     if (x > 0) {
                                         semEventos.isVisible = false
                                     } else {

                                         semEventos.isVisible = true
                                         Toast.makeText(
                                             applicationContext,
                                             "Sem eventos disponiveis", Toast.LENGTH_LONG
                                         ).show()
                                     }

                                 }

                             } else {
                                 var ListaEventosPublic = mAuth.collection("Eventos")
                                 ListaEventosPublic.get().addOnSuccessListener { result ->
                                     if (result != null) {
                                         val values = ArrayList<String>()

                                         for (evento in result) {
                                             semEventos.isVisible = false

                                             val f = evento.get("Forma")
                                             if (f == "publico") {
                                                 for (i in checkedTiposArray.indices) {
                                                     val checked = checkedTiposArray[i]
                                                     if (checked) {
                                                         val x = tipos[i]

                                                         if (evento.get("Tipo").toString() == x) {
                                                             Log.d("merda", x)
                                                             values.add(evento.get("nome").toString())
                                                         }
                                                     }
                                                 }

                                             }

                                         }
                                         Log.d("home44", "$values")
                                         val adapter = ArrayAdapter(this, R.layout.listview_item, values)

                                         lista.adapter = adapter
                                         lista.onItemClickListener =
                                             object : AdapterView.OnItemClickListener {


                                                 override fun onItemClick(
                                                     parent: AdapterView<*>,
                                                     view: View,
                                                     position: Int,
                                                     id: Long
                                                 ) {


                                                     val itemValue =
                                                         lista.getItemAtPosition(position) as String
                                                     Log.d("home", "grupoID to search: $itemValue")
                                                     gv.detalhes = itemValue
                                                     val uid = Auth.currentUser?.uid
                                                     var eventoclick2 =
                                                         mAuth.collection("Eventos").document(itemValue)
                                                     eventoclick2.get().addOnSuccessListener { result ->
                                                         if (result != null) {

                                                             startActivity(
                                                                 Intent(
                                                                     view.context,
                                                                     DetalhesEventoActivity::class.java
                                                                 )
                                                             )


                                                         }
                                                     }

                                                 }

                                             }

                                     }
                                 }
                             }
                         }
                     }
                 }


             }


             val dialog = alertDialog.create()
             dialog.show()

         }*/

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
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //startActivity(Intent (this, MainActivity :: class.java ))
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.Lis) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this, VerGrupoActivity::class.java))
        }




        if (item.itemId == R.id.home) {

            startActivity(Intent(this, FiltrosActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }
}