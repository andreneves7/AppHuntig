package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_criar_grupo.*
import kotlinx.android.synthetic.main.activity_evento.*
import java.lang.reflect.Field
import java.util.HashMap

class CriarGrupoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_criar_grupo)

        val criarGrupo = bCriaGrupo
        val verGrupo = bVerGrupos


        criarGrupo.setOnClickListener {
            criar()
        }

        verGrupo.setOnClickListener {
            startActivity(Intent (this, VerGrupoActivity :: class.java ))
        }


    }

    private fun criar(){

//        val ednome = edName

        val nome = edName.text.toString()
        val documentId = mAuth.collection("Users").document().id
        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {


                        val name = document.data?.get("name")

                        val grupo = HashMap<String, Any>()
                        grupo["nome"] = nome
                        grupo["membros"] = arrayListOf(name)
                        grupo["admin"] = arrayListOf(name)
                        mAuth.collection("Grupos").document(documentId).set(grupo)


                        val up = HashMap<String, Any>()
                        up["grupo"] = arrayListOf(nome)
                        mAuth.collection("Users").document(user.uid).update("grupo", FieldValue.arrayUnion(nome))

                    Toast.makeText(this, "grupo criado", Toast.LENGTH_SHORT).show()


                    startActivity(Intent (this, CriarGrupoActivity :: class.java ))

                    Log.d("criar","DocumentSnapshot data: ${document.data?.get("name")}")
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
        if (item!!.itemId == R.id.signOut){
            Auth.signOut()
            val intent = Intent(this, LoginActivity :: class.java )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //startActivity(Intent (this, MainActivity :: class.java ))
        }

        if (item!!.itemId == R.id.profile){

            startActivity(Intent (this, ProfileActivity :: class.java ))
        }

        if (item!!.itemId == R.id.grupo){

            startActivity(Intent (this, CriarGrupoActivity :: class.java ))
        }
        return super.onOptionsItemSelected(item)
    }
}

