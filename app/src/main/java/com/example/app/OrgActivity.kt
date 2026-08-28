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

        val semGrupos = binding.tNaoGrupos2
        val list = binding.ListView3

        val user = Auth.currentUser?.uid


        semGrupos.isVisible = true
        if (user != null) {
            val mail = mAuth.getReference("Grupos")

            val values = ArrayList<String>()
            val valor = ArrayList<String>()

            val j = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    val g = dataSnapshot.child("nome").getValue().toString()
                    val admin = dataSnapshot.child("admin").getValue().toString()

                    Log.d(
                        "VerGrupo2",
                        "${user}"
                    )

                    values.add(g)

                    val m = mAuth.getReference("Grupos").child(g)

                    Log.d(
                        "VerGrupo2",
                        " ${m}"
                    )

                    val t = mAuth.getReference("Grupos").child(g)
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

