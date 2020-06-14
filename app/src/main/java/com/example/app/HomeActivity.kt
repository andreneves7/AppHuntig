package com.example.app

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_grupo.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_ver_grupo.*
import kotlinx.android.synthetic.main.custom_view.view.*
import kotlinx.android.synthetic.main.custom_view.view.showPass
import kotlinx.android.synthetic.main.email_custom_view.view.*
import kotlinx.android.synthetic.main.pass_custom_view.view.*


class HomeActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_home)

        val semEventos = NaoEventos
        val lista = ListView4


        var d = mAuth.collection("Eventos")
        d.get().addOnSuccessListener { result ->
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


                lista.onItemClickListener = object : AdapterView.OnItemClickListener {


                    override fun onItemClick(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {


                        val itemValue = lista.getItemAtPosition(position) as String
                        Log.d("home", "grupoID to search: $itemValue")
                        gv.detalhes = itemValue
                        val uid = Auth.currentUser?.uid
                        var b = mAuth.collection("Eventos").document(itemValue)
                        b.get().addOnSuccessListener { result ->
                            if (result != null) {

                                startActivity(Intent (view.context, DetalhesEventoActivity :: class.java ))


                            }
                        }


                        Toast.makeText(
                            applicationContext,
                            "Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG
                        ).show()


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

