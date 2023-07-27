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

class ListaGruposActivity : AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais


    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_grupos)
        listView = findViewById(R.id.listViewLista)

        val gruposMemebro = mAuth.getReference("Grupos")
        val list = ArrayList<String>()

        val membro = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

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

                        //var b = mAuth.collection("Grupos").document(itemValue.toString())
                        b.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {


                                startActivity(
                                    Intent(view.context, AdesaoActivity::class.java).apply {
                                        putExtra(AlarmClock.EXTRA_MESSAGE, message)
                                    }
                                )
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                    }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                startActivity(Intent(this@ListaGruposActivity, PreferenciasActivity::class.java))
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