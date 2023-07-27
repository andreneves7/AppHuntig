package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity
import android.content.Intent
import android.provider.AlarmClock
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class VerificarLoginActivity : AppCompatActivity() {

    private val a = FirebaseAuth.getInstance().currentUser
    private val b = FirebaseDatabase.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (a == null) {
            startActivity<LoginActivity>()
        } else {


            val c = b.getReference("Users").child(a.uid)
            c.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val org = dataSnapshot.child("Org").value


                    if (org == false) {

                        val marca = 0
                        val intent = Intent(this@VerificarLoginActivity, FiltrosActivity::class.java).apply {
                            putExtra(AlarmClock.EXTRA_MESSAGE, marca)
                        }
                        startActivity(intent)


                    } else startActivity<OrgActivity>()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    finish()
    }
}
