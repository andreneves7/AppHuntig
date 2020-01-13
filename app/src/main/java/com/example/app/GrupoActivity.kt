package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_expandable.*
import java.util.HashMap


class GrupoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()


    val title: MutableList<String> = ArrayList()
    val subTitle: MutableList<MutableList<String>> = ArrayList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandable)


        val evento = bEvento

        evento.setOnClickListener{
            startActivity(Intent (this, EventoActivity :: class.java ))
        }



        val diff_pedidos : MutableList<String> = ArrayList()
        diff_pedidos.add("indian Oceans")

        val diff_membros : MutableList<String> = ArrayList()
        diff_membros.add("Asia")

        title.add("Pedidos")
        title.add("Membros")

        subTitle.add(diff_pedidos)
        subTitle.add(diff_membros)


        expandableListView.setAdapter(ExpandableListViewAdapter(this,expandableListView,title,subTitle))


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
