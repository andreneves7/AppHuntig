package com.example.app

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_evento.*
import kotlinx.android.synthetic.main.activity_evento.checkDiasCaça
import kotlinx.android.synthetic.main.activity_evento.checkEspera
import kotlinx.android.synthetic.main.activity_evento.checkMontaria
import kotlinx.android.synthetic.main.activity_evento.checkRola
import kotlinx.android.synthetic.main.activity_evento.checkTordo
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.filtros_custom_view.*
import kotlinx.android.synthetic.main.filtros_custom_view.view.*
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_home)

        val lista = ListView4

        val escolherFiltros = filtros

        eventos()
        escolherFiltros.setOnClickListener {
            lista.setAdapter(null);
            showFiltros()
        }


    }

    fun eventos(){
        val semEventos = NaoEventos
        val uid = Auth.currentUser?.uid
        val lista = ListView4
        val pesquisa = SearchEvento


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


                                    values.add(evento.get("nome").toString())

                                }
                                Log.d("home", "$values")


                                val adapter = ArrayAdapter(this, R.layout.listview_item, values)

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
                                        values.add(evento.get("nome").toString())
                                    }

                                }
                                Log.d("home", "$values")


                                val adapter = ArrayAdapter(this, R.layout.listview_item, values)

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
                    }
                }
            }
        }
    }

    private fun showFiltros() {
//        val inflater = layoutInflater
//        val inflate_view = inflater.inflate(R.layout.filtros_custom_view, null)


        val semEventos = NaoEventos
        val lista = ListView4
        val tipos = arrayOf("Montaria", "Espera", "Tordos", "Rolas", "Dias Caça")
        val checkedTiposArray = booleanArrayOf(false, false, false, false,  false)



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

                                    if (evento.get("Tipo").toString() == x){
                                        Log.d("merda", x)
                                        values.add(evento.get("nome").toString())
                                    }
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


        val dialog = alertDialog.create()
        dialog.show()

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

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this, CriarGrupoActivity::class.java))
        }



        if (item.itemId == R.id.home) {

            val intent = Intent(this, HomeActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }


}

