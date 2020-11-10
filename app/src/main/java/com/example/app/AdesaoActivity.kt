package com.example.app

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_adesao.*


class AdesaoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_adesao)

        val texto = tInfo
        val botao = bEntrar


        val user = Auth.currentUser
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        if (user != null) {
            val mail = mAuth.getReference("Grupos").child(message)
            mail.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val name = dataSnapshot.child("nome").getValue().toString()
                    val numero = dataSnapshot.child("Numero").getValue().toString()



                    texto.text = "nome: $name\nnumero de associativa:$numero"

                    Log.d(
                        "adesao", "DocumentSnapshot data: ${name} }"
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("adesao", "No such document")
                }
            })
        }

        botao.setOnClickListener {

            showAlert()
        }


    }


    private fun showAlert() {

        val cod = grupoCodigo
        val codigo = cod.text.toString()
        val message = intent.getStringExtra(EXTRA_MESSAGE)

        val c = mAuth.getReference("Grupos").child(message)
        c.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val use = Auth.currentUser
                val socio: MutableMap<String, Any> = HashMap()
                socio["numero socio"] = codigo

                    c.child("Pendentes")
                        .child(use!!.uid).setValue(socio)


                    val intent = Intent(this@AdesaoActivity, FiltrosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)



                Log.d("adesao", "DocumentSnapshot data: ${message} ")

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })
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
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this,VerGrupoActivity::class.java))
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
