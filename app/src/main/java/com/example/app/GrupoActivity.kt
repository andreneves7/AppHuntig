package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_grupo.*

class GrupoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grupo)

        val evento = bEvento

        evento.setOnClickListener{
            startActivity(Intent (this, EventoActivity :: class.java ))
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

            startActivity(Intent (this, GrupoActivity :: class.java ))
        }
        return super.onOptionsItemSelected(item)
    }
}
