package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.startActivity
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class VerificarLoginActivity : AppCompatActivity() {

    val a = FirebaseAuth.getInstance().currentUser
    val b = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (a == null) {
            startActivity<LoginActivity>()
        } else {


        val c = b.getReference("Users").child(a!!.uid)
            c.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val org = dataSnapshot.child("Org").getValue()


                    if (org == false) {

                        val marca = 0
                        val intent = Intent(this@VerificarLoginActivity, FiltrosActivity::class.java).apply {
                            putExtra(AlarmClock.EXTRA_MESSAGE, marca)
                        }
                        startActivity(intent)


                    } else {
                        startActivity<OrgActivity>()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    finish()
    }
}
