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
import kotlinx.android.synthetic.main.activity_ver_grupo.*


class VerGrupoActivity : AppCompatActivity() {

    //    val mAuth = FirebaseFirestore.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()

    lateinit var gv: VariaveisGlobais


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_ver_grupo)


        val semGrupos = tNaoGrupos
        val list = ListView2

        val user = auth.currentUser?.uid
        if (user != null) {
            val mail = mAuth.getReference("Grupos")

            val values = ArrayList<String>()
            val valor = ArrayList<String>()

            val m = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    val g = dataSnapshot.child("nome").value.toString()

                    Log.d(
                        "VerGrupo2",
                        "$user"
                    )

                    values.add(g)

                    val m = mAuth.getReference("Grupos").child(g)

                    Log.d(
                        "VerGrupo2",
                        " $m"
                    )

                    val t = mAuth.getReference("Grupos").child(g).child("membros")


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
                                        val adapter =
                                            ArrayAdapter(this@VerGrupoActivity, R.layout.listview_item, valor)

                                        list.adapter = adapter



                                        list.onItemClickListener =
                                            AdapterView.OnItemClickListener { _, view, position, _ ->
                                                val itemValue =
                                                    list.getItemAtPosition(position) as String
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
                                                        TODO("Not yet implemented")
                                                    }

                                                })
                                            }


                                    }


                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
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
