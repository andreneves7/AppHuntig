package com.example.app

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
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityOrgBinding

class OrgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrgBinding
    val mAuth = FirebaseDatabase.getInstance()
    val Auth = FirebaseAuth.getInstance()

    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        binding = ActivityOrgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedirPermissaoNotificacoes()
        HuntigMessagingService.guardarTokenAtualNoFirebase()

        val semGrupos = binding.tNaoGrupos2
        val list = binding.ListView3

        val user = Auth.currentUser?.uid


        semGrupos.isVisible = true
        binding.progressOrg.isVisible = true
        binding.progressOrg.postDelayed({ binding.progressOrg.isVisible = false }, 5000)
        if (user != null) {
            // Antes: mAuth.getReference("Grupos") sem filtro nenhum — descarregava
            // TODOS os grupos da plataforma, de todas as organizações, só para
            // verificar no cliente (if (admin == user)) quais eram desta. Cresce
            // mal à medida que existirem mais organizações. Agora: o próprio
            // Firebase filtra do lado do servidor, só devolve os grupos que esta
            // organização administra. Precisa do índice em "admin", já
            // adicionado a database.rules.json.
            val mail = mAuth.getReference("Grupos").orderByChild("admin").equalTo(user)

            val values = ArrayList<String>()
            val valor = ArrayList<String>()

            val j = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    binding.progressOrg.isVisible = false

                    val g = dataSnapshot.child("nome").getValue().toString()
                    val admin = dataSnapshot.child("admin").getValue().toString()
                    // MIGRAÇÃO: Grupos passou a ser indexado por "Numero" em vez
                    // do nome (ver docs/PLANO_DESENVOLVIMENTO.md).
                    val numero = dataSnapshot.child("Numero").getValue().toString()

                    Log.d(
                        "VerGrupo2",
                        "${user}"
                    )

                    values.add(g)

                    val m = mAuth.getReference("Grupos").child(numero)

                    Log.d(
                        "VerGrupo2",
                        " ${m}"
                    )

                    val t = mAuth.getReference("Grupos").child(numero)
                    if (admin == user) {

                        m.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                val n =
                                    snapshot.child("Numero").getValue()
                                Log.d(
                                    "VerGrupo",
                                    " grupos deste user"
                                )

                                Log.d(
                                    "VerGrupo2",
                                    " n: ${n}"
                                )

                                semGrupos.isVisible = false

                                gv = application as VariaveisGlobais

                                valor.add(g)
                                val adapter =
                                    ArrayAdapter(
                                        this@OrgActivity,
                                        R.layout.listview_item,
                                        valor
                                    )

                                list.adapter = adapter



                                list.onItemClickListener =
                                    object : AdapterView.OnItemClickListener {


                                        override fun onItemClick(
                                            parent: AdapterView<*>, view: View,
                                            position: Int, id: Long
                                        ) {


                                            val itemValue =
                                                list.getItemAtPosition(position) as String

                                            val message = n.toString()


                                            startActivity(
                                                Intent(
                                                    view.context,
                                                    CriarOrgEventoActivity::class.java
                                                ).apply {
                                                    putExtra(
                                                        AlarmClock.EXTRA_MESSAGE,
                                                        message
                                                    )
                                                }
                                            )


                                            Log.d(
                                                "VerGrupo2",
                                                "messagem: $message"
                                            )


                                        }

                                    }


                            }


                            override fun onCancelled(error: DatabaseError) {
                                Log.d("todo_fix", "erro Firebase: ${error.message}")
                                this@OrgActivity.mostrarErroLigacao()
                            }
                        })
                    } else {
                        semGrupos.isVisible = true
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                   startActivity(Intent(this@OrgActivity, OrgActivity::class.java))
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressOrg.isVisible = false
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@OrgActivity.mostrarErroLigacao()
                }
            }
            mail.addChildEventListener(j)


        } else {
            Log.d("VerGrupo", "No such document")
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

