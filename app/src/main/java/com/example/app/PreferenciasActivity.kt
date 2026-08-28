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
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityPreferenciasBinding

class PreferenciasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreferenciasBinding
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais


    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pular = binding.bPular
        val change = Auth.currentUser?.uid.toString()

        pular.setOnClickListener {

            val marca = 1


            val intent = Intent(this, FiltrosActivity::class.java)  .apply {
                putExtra(EXTRA_MESSAGE, marca)
            }

            val first = mAuth.getReference("Users").child(change)
                first.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val pessoa = HashMap<String, Any>()
                        pessoa["FirstTime"] = false
                        mAuth.getReference("Users").child(change).updateChildren(pessoa)

                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

                        startActivity(intent)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("todo_fix", "erro Firebase: ${error.message}")
                        this@PreferenciasActivity.mostrarErroLigacao()
                    }
                })

        }



        listView = binding.listViewPre

        var d = mAuth.getReference("Grupos")
        var list = ArrayList<String>()
        // MIGRAÇÃO: Grupos passou a ser indexado por "Numero" em vez do nome
        // (ver docs/PLANO_DESENVOLVIMENTO.md). "list" continua a mostrar o
        // nome; esta lista paralela guarda o número de cada grupo, na mesma
        // posição, para usar como chave real do Firebase.
        var listNumeros = ArrayList<String>()

        val c = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {


                val grupo = dataSnapshot.getValue()

                val g = dataSnapshot.child("nome").getValue().toString()
                val numero = dataSnapshot.child("Numero").getValue().toString()
                list.add(
                    "${g}"
                )
                listNumeros.add(numero)

                Log.d(
                    "Preferencias",
                    " pref $grupo"
                )
                Log.d(
                    "Preferencias",
                    " pref $g"
                )
                Log.d(
                    "Preferencias",
                    " pref $list"
                )
                val adapter3 = ArrayAdapter(this@PreferenciasActivity, R.layout.listview_item, list)

                listView.adapter = adapter3

                listView.onItemClickListener =
                    object : AdapterView.OnItemClickListener {


                        override fun onItemClick(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {

                            val message = listNumeros[position]
                            Log.d("Preferencias", "numero: $message" + "posicao: $position ")

                            var b = mAuth.getReference("Grupos").child(message)

                            b.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {


                                    startActivity(
                                        Intent(view.context, AdesaoActivity::class.java).apply {
                                            putExtra(EXTRA_MESSAGE, message)
                                        }
                                    )
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                                    this@PreferenciasActivity.mostrarErroLigacao()
                                }

                            })
                        }
                    }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                startActivity(Intent(this@PreferenciasActivity, PreferenciasActivity::class.java))
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("todo_fix", "erro Firebase: ${error.message}")
                this@PreferenciasActivity.mostrarErroLigacao()
            }


        }
        d.addChildEventListener(c)

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



        if (item.itemId == R.id.eventosProximos) {
            startActivity(Intent(this, EventosProximosActivity::class.java))
        }

        if (item.itemId == R.id.checkInQR) {
            iniciarScanQR()
        }

        if (item.itemId == R.id.home) {

            val intent = Intent(this, FiltrosActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!processarResultadoScanQR(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}