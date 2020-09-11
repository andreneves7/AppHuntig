package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_preferencias.*

class PreferenciasActivity : AppCompatActivity() {
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    lateinit var gv: VariaveisGlobais


    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferencias)

        val pular = bPular

        pular.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }



        listView = findViewById(R.id.listView)

        var d = mAuth.collection("Grupos")
        d.get().addOnSuccessListener { result ->
            if (result != null) {


                var list = ArrayList<String>()

                for (grupo in result) {

                    list.add(
                        "${grupo.get("nome").toString()}"
                    )

                }

                val adapter = ArrayAdapter(this, R.layout.listview_item, list)

                listView.adapter = adapter

                listView.onItemClickListener =
                    object : AdapterView.OnItemClickListener {


                        override fun onItemClick(
                            parent: AdapterView<*>,
                            view: View,
                            position: Int,
                            id: Long
                        ) {

                            val itemValue = listView.getItemAtPosition(position)
                            val message = itemValue as String
                            Log.d("Preferencias", "mensagem: $message" + "item: $itemValue ")
                            var b = mAuth.collection("Grupos").document(itemValue.toString())
                            b.get().addOnSuccessListener { result ->
                                if (result != null) {


                                    startActivity(
                                        Intent(view.context, AdesaoActivity::class.java).apply {
                                            putExtra(EXTRA_MESSAGE, message)
                                        }
                                    )
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