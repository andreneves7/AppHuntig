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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_grupo.*
import kotlinx.android.synthetic.main.activity_home.*
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
        gv = getApplication() as VariaveisGlobais
        setContentView(R.layout.activity_home)

        val lista = ListView4


        var d = mAuth.collection("Grupos")
        d.get().addOnSuccessListener { result ->
            if (result != null) {
                val values = ArrayList<String>()
                for (grupo in result) {


                    values.add(grupo.get("nome").toString())

                }
                Log.d("home", "$values")


                val adapter = ArrayAdapter(this, R.layout.listview_item, values)

                lista.setAdapter(adapter)


                lista.onItemClickListener = object : AdapterView.OnItemClickListener {


                    override fun onItemClick(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {


                        val itemValue = lista.getItemAtPosition(position) as String
                        Log.d("home", "grupoID to search: $itemValue")
                        gv.entrar = itemValue
                        val uid = Auth.currentUser?.uid
                        var b = mAuth.collection("Grupos").document(itemValue)
                        b.get().addOnSuccessListener { result ->
                            if (result != null) {

                                var membersList = result.get("membros") as List<String>
                                Log.d("home", "membros: " + membersList.toString())
//                                    teste.clear()
                                /* for (f in a){
                                     teste.add(f)

                                 }*/
                                if (membersList.contains(uid)) {
                                    startActivity(
                                        Intent(
                                            view.context,
                                            VerGrupoActivity::class.java
                                        )
                                    )
                                    Log.d("home", "$uid, $membersList")
                                } else {
                                    startActivity(Intent(view.context, AdesaoActivity::class.java))
                                    Log.d("home", "$uid, $membersList")

                                }


                            }
                        }


                        Toast.makeText(
                            applicationContext,
                            "Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG
                        ).show()


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

        if (item!!.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item!!.itemId == R.id.grupo) {

            startActivity(Intent(this, CriarGrupoActivity::class.java))
        }
        if (item!!.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
}

