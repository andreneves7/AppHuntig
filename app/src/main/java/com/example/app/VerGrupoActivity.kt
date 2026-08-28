package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityVerGrupoBinding


class VerGrupoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerGrupoBinding
    val mAuth = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()

    lateinit var gv: VariaveisGlobais


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        binding = ActivityVerGrupoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val semGrupos = binding.tNaoGrupos
        val list = binding.ListView2

        binding.progressVerGrupo.isVisible = true
        binding.progressVerGrupo.postDelayed({ binding.progressVerGrupo.isVisible = false }, 5000)

        val user = auth.currentUser?.uid
        if (user != null) {
            val mail = mAuth.getReference("Grupos")

            val values = ArrayList<String>()
            val valor = ArrayList<String>()
            // MIGRAÇÃO: Grupos passou a ser indexado por "Numero" em vez do
            // nome (ver docs/PLANO_DESENVOLVIMENTO.md). "valor" continua a
            // guardar o nome (mostrado na lista ao utilizador); esta lista
            // paralela guarda o número de cada grupo, na mesma posição, para
            // usar como chave real do Firebase quando um item é clicado.
            val valorNumeros = ArrayList<String>()

            val m = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    binding.progressVerGrupo.isVisible = false

                    val g = dataSnapshot.child("nome").value.toString()
                    val numero = dataSnapshot.child("Numero").value.toString()

                    Log.d(
                        "VerGrupo2",
                        "$user"
                    )

                    values.add(g)

                    val m = mAuth.getReference("Grupos").child(numero)

                    Log.d(
                        "VerGrupo2",
                        " $m"
                    )

                    val t = mAuth.getReference("Grupos").child(numero).child("membros")


                    val f = object : ChildEventListener {
                        override fun onChildAdded(
                            dataSnapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val j = dataSnapshot.value.toString()
                            val fazParte = ArrayList<String>()

                            Log.d(
                                "VerGrupo2",
                                "j : $j"
                            )
                            fazParte.add(j)

                            Log.d(
                                "VerGrupo2",
                                "f : $fazParte"
                            )

                            if (fazParte.contains(user)) {


                                m.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        val n =
                                            snapshot.child("Numero").value
                                        Log.d(
                                            "VerGrupo",
                                            " grupos deste user"
                                        )

                                        Log.d(
                                            "VerGrupo2",
                                            " n: $n"
                                        )

                                        semGrupos.isVisible = false

                                        gv = application as VariaveisGlobais

                                        valor.add(g)
                                        valorNumeros.add(numero)
                                        val adapter =
                                            ArrayAdapter(this@VerGrupoActivity, R.layout.listview_item, valor)

                                        list.adapter = adapter



                                        list.onItemClickListener =
                                            AdapterView.OnItemClickListener { _, view, position, _ ->
                                                val itemValue = valorNumeros[position]
                                                mAuth.getReference("Grupos").child(itemValue).addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {
                                                        val num =
                                                            snapshot.child("Numero").value
                                                        val message = num.toString()
                                                        Log.d(
                                                            "VerGrupo2",
                                                            " num: $num"
                                                        )

                                                        startActivity(
                                                            Intent(
                                                                view.context,
                                                                GrupoActivity::class.java
                                                            ).apply {
                                                                putExtra(EXTRA_MESSAGE, message)
                                                                putExtra(EXTRA_MESSAGE, message)
                                                            }
                                                        )


                                                        Log.d(
                                                            "VerGrupo2",
                                                            "messagem: $message"
                                                        )
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        Log.d("todo_fix", "erro Firebase: ${error.message}")
                                                        this@VerGrupoActivity.mostrarErroLigacao()
                                                    }

                                                })
                                            }


                                    }


                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("todo_fix", "erro Firebase: ${error.message}")
                                        this@VerGrupoActivity.mostrarErroLigacao()
                                    }
                                })
                            } else {
                                Log.d(
                                    "VerGrupo",
                                    " sem grupos deste user"
                                )

                                semGrupos.isVisible = true
                            }
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("todo_fix", "erro Firebase: ${error.message}")
                            this@VerGrupoActivity.mostrarErroLigacao()
                        }
                    }

                    t.addChildEventListener(f)


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
                    binding.progressVerGrupo.isVisible = false
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@VerGrupoActivity.mostrarErroLigacao()
                }
            }
            mail.addChildEventListener(m)


        } else {
            Log.d("VerGrupo", "No such document")
        }
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
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //startActivity(Intent (this, MainActivity :: class.java ))
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
                putExtra(EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }



        return super.onOptionsItemSelected(item)
    }
}
