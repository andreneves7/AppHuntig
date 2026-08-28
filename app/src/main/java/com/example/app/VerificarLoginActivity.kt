package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
            startActivity(Intent(this, LoginActivity::class.java))
        } else {

            val c = b.getReference("Users").child(a.uid)
            c.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val org = dataSnapshot.child("Org").value as? Boolean
                    val roleValue = dataSnapshot.child("role").getValue(String::class.java)
                    val role = Roles.resolver(roleValue, org)

                    val gv = application as VariaveisGlobais
                    gv.role = role

                    when (role) {
                        Roles.SUPERADMIN -> startActivity(
                            Intent(this@VerificarLoginActivity, SuperAdminActivity::class.java)
                        )
                        Roles.ORGANIZACAO -> startActivity(
                            Intent(this@VerificarLoginActivity, OrgActivity::class.java)
                        )
                        else -> {
                            val marca = 0
                            val intent = Intent(
                                this@VerificarLoginActivity,
                                FiltrosActivity::class.java
                            ).apply {
                                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
                            }
                            startActivity(intent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@VerificarLoginActivity.mostrarErroLigacao()
                }
            })
        }
        finish()
    }
}
