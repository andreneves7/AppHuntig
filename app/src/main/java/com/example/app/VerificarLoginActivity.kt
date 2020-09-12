package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.startActivity
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible



class VerificarLoginActivity : AppCompatActivity() {

        val a = FirebaseAuth.getInstance().currentUser
        val b = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val c = b.collection("Users").document(a?.uid.toString())
            if (a == null) {
                startActivity<LoginActivity>()
            } else {


                c.get().addOnSuccessListener { document ->
                if (document != null) {
                    val org = document.data?.get("Org")

                    if (org == false){

                        startActivity<FiltrosActivity>()
                    }
                    else{
                        startActivity<OrgActivity>()
                    }
                }
            }
        }
        finish()
    }
}





