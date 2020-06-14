package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_criar_grupo.*
import kotlinx.android.synthetic.main.activity_evento.*
import kotlinx.android.synthetic.main.activity_registo_user.*
import java.lang.reflect.Field
import java.util.ArrayList
import java.util.HashMap

class CriarGrupoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()

    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_criar_grupo)

        val criarGrupo = bCriaGrupo
        val verGrupo = bVerGrupos


        criarGrupo.setOnClickListener {
            criar()

        }

        verGrupo.setOnClickListener {
            startActivity(Intent(this, VerGrupoActivity::class.java))
        }

        show()


    }

    private fun show() {

        val lista = idScrool
        val uid = Auth.currentUser?.uid
        var d = mAuth.collection("Grupos")
        d.get().addOnSuccessListener { result ->
            if (result != null) {
                val values = ArrayList<String>()


                    for (grupo in result) {
                        values.add(grupo.get("nome").toString())
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
                            gv.entrar = itemValue

                            var b = mAuth.collection("Grupos").document(itemValue)
                            b.get().addOnSuccessListener { result ->
                                if (result != null) {
                                    var membersList = result.get("membros") as List<String>

                                    Log.d(
                                        "home", "aaaa: $membersList" +
                                                "ffff: $uid"
                                    )
                                    if (membersList.contains(uid)) {
                                        startActivity(
                                            Intent(
                                                view.context,
                                                VerGrupoActivity::class.java
                                            )
                                        )
                                        Log.d("home", "$uid, $membersList")
                                    } else {
                                        startActivity(
                                            Intent(
                                                view.context,
                                                AdesaoActivity::class.java
                                            )
                                        )
                                        Log.d("home", "$uid, $membersList")
                                    }
                                }
                            }

                        }
                    }


            }
        }

    }

    private fun criar() {


        val nome = edName.text.toString()
        val cod = edCodigo.text.toString()
        //val documentId = mAuth.collection("Users").document().id
        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {


                    val name = document.data?.get("name")
                    val uid = document.data?.get("uid")
                    if (!nome.isEmpty() && !cod.isEmpty()) {
                        val grup = HashMap<String, Any>()
                        grup["nome"] = nome
                        grup["membros"] = arrayListOf(uid)
                        grup["admin"] = arrayListOf(name)
                        grup["Eventos"] = ArrayList<String>()
                        grup["Codigo"] = cod
                        mAuth.collection("Grupos").document(nome).set(grup)


                        val up = HashMap<String, Any>()
                        up["grupo"] = arrayListOf(nome)
                        mAuth.collection("Users").document(user.uid)
                            .update("grupo", FieldValue.arrayUnion(nome))

                        Toast.makeText(this, "grupo criado", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, VerGrupoActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)



                        Log.d("criar", "DocumentSnapshot data: ${document.data?.get("name")}")
                    } else {
                        Toast.makeText(this, "Preencha os campos", Toast.LENGTH_SHORT).show()
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
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
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

            startActivity(Intent(this, HomeActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    private fun clearInputs() {
        addNome.text.clear()
        addEmail.text.clear()
        addPass.text.clear()
    }
}

