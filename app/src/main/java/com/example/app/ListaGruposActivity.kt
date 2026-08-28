package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityListaGruposBinding
import androidx.core.view.isVisible

class ListaGruposActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais
    private lateinit var binding: ActivityListaGruposBinding


    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaGruposBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listView = binding.listViewLista

        binding.progressListaGrupos.isVisible = true
        binding.progressListaGrupos.postDelayed({ binding.progressListaGrupos.isVisible = false }, 5000)

        // Este ecrã lista TODOS os grupos da plataforma para o utilizador navegar
        // e pedir adesão a um (não é "os meus grupos" — é um catálogo de
        // descoberta). Por isso, ao contrário de OrgActivity/AdmissaoActivity, um
        // limite simples aqui é seguro: não há risco de esconder algo específico
        // do utilizador, só de não mostrar os últimos alfabeticamente se a
        // plataforma vier a ter centenas de grupos.
        val gruposMemebro = mAuth.getReference("Grupos").limitToFirst(300)
        val list = ArrayList<String>()

        val membro = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                binding.progressListaGrupos.isVisible = false

                //val grupo = dataSnapshot.getValue()

                val g = dataSnapshot.child("nome").value.toString()
                list.add(
                    g
                )

//                Log.d(
//                    "ListaGruposActivity",
//                    " pref $grupo"
//                )
                Log.d(
                    "ListaGruposActivity",
                    " pref $g"
                )
                Log.d(
                    "ListaGruposActivity",
                    " pref $list"
                )
                val adapter3 = ArrayAdapter(this@ListaGruposActivity, R.layout.listview_item, list)

                listView.adapter = adapter3

                listView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, view, position, _ ->
                        val itemValue = listView.getItemAtPosition(position)
                        val message = itemValue as String
                        Log.d("ListaGruposActivity", "mensagem: $message" + "item: $itemValue ")

                        val b = mAuth.getReference("Grupos").child(itemValue.toString())

                        b.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {


                                startActivity(
                                    Intent(view.context, AdesaoActivity::class.java).apply {
                                        putExtra(AlarmClock.EXTRA_MESSAGE, message)
                                    }
                                )
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("todo_fix", "erro Firebase: ${error.message}")
                                this@ListaGruposActivity.mostrarErroLigacao()
                            }

                        })
                    }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                startActivity(Intent(this@ListaGruposActivity, PreferenciasActivity::class.java))
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressListaGrupos.isVisible = false
                Log.d("todo_fix", "erro Firebase: ${error.message}")
                this@ListaGruposActivity.mostrarErroLigacao()
            }


        }
        gruposMemebro.addChildEventListener(membro)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.signOut) {
            auth.signOut()
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
            val marca = 0
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }
}