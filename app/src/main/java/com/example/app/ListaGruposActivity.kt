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
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListaGruposActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    lateinit var gv: VariaveisGlobais


    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_grupos)
        listView = findViewById(R.id.listViewLista)

        var d = mAuth.collection("Grupos")
        d.get().addOnSuccessListener { result ->
            if (result != null) {


                var list = ArrayList<String>()

                for (grupo in result) {

                    list.add(
                        "${grupo.get("nome").toString()}"
                    )

                }

                val adapter2 = ArrayAdapter(this, R.layout.listview_item, list)

                listView.adapter = adapter2

                listView.onItemClickListener =
                    object : AdapterView.OnItemClickListener {


                        override fun onItemClick(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {

                            val itemValue = listView.getItemAtPosition(position) as String
                            val message = itemValue
                            Log.d("Preferencias", "mensagem: $message" + "item: $itemValue ")


                            var b = mAuth.collection("Grupos").document(itemValue)
                            b.get().addOnSuccessListener { result ->
                                if (result != null) {
                                    var p = result.data?.get("membros") as List<String>


                                    val user = Auth.currentUser!!.uid

                                    var controlo = 0
                                    for (m in p) {

                                        if (m.contains(user)) {
                                            controlo = 1
                                        }
                                    }
                                    if (controlo == 0) {
                                        startActivity(
                                            Intent(
                                                view.context,
                                                AdesaoActivity::class.java
                                            ).apply {
                                                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                                            }
                                        )
                                    } else {
                                        Toast.makeText(
                                            this@ListaGruposActivity,
                                            "ja esta presente neste grupo",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }


                                }

                            }
                        }
                    }

            }


        }
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

            val intent = Intent(this, HomeActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}