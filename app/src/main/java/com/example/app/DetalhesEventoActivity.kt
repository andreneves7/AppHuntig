package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detalhes_evento.*
import java.util.HashMap
import androidx.core.view.isVisible as isVisible

class DetalhesEventoActivity : AppCompatActivity(), OnMapReadyCallback {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais

    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        setContentView(R.layout.activity_detalhes_evento)

        val showDetalhe = tShowDetalhes
        val marcar = bPresença

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)



        desativar()

        marcar.setOnClickListener {
            marcarPresença()
            val marca = 1
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }

        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.getReference("Eventos").child(gv.detalhes)
            mail.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val name = dataSnapshot.child("nome").getValue().toString()
                    val dateDia = dataSnapshot.child("dia").getValue().toString()
                    val dateMes = dataSnapshot.child("mes").getValue().toString()
                    val dateAno = dataSnapshot.child("ano").getValue().toString()
                    val time = dataSnapshot.child("horas").getValue().toString()
                    val tipo = dataSnapshot.child("Tipo").getValue().toString()


                    showDetalhe.text =
                        "nome: $name\ndata: $dateDia/$dateMes/$dateAno\nhoras: $time\ntipo: $tipo"


//                    Log.d(
//                        "evento",
//                        "DocumentSnapshot data: ${document.data?.get("nome")} \n${document.data?.get(
//                            "data"
//                        )}" +
//                                " \n ${document.data?.get("hora")} \n ${document.data?.get("local")}"
//                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mail = mAuth.getReference("Eventos").child(gv.detalhes)
        mail.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val placeLat = dataSnapshot.child("Latitude").getValue()
                val placeLog = dataSnapshot.child("Longitude").getValue()
                val P = LatLng(placeLat.toString().toDouble(), placeLog.toString().toDouble())

                placeMarkerOnMap(P)
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(P, 18f))
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        // 2
        mMap.clear()
        mMap.addMarker(markerOptions)
    }

    fun desativar() {

        val marcar = bPresença
        val uid = Auth.currentUser?.uid
        val fazParte = ArrayList<String>()

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.getReference("Eventos").child(gv.detalhes).child("Presenças")
            Log.d(
                "detalhes", "detalhe: ${gv.detalhes}"
            )
            val m = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    val pre = dataSnapshot.getValue().toString()
                    Log.d(
                        "detalhes", "detalhe: $pre"
                    )

                    fazParte.add(pre)





                            if (fazParte.contains(uid)) {

                                marcar.isVisible = false
                                Log.d(
                                    "detalhes", "detalhe: $pre" +
                                            "ffff: $uid" + "\n" + "false"
                                )
                            } else {

                                marcar.isVisible = true
                                Log.d(
                                    "detalhes", "detalhe: $pre" +
                                            "ffff: $uid" + "\n" + "true"
                                )
                            }
//                            Log.d(
//                                "evento", "DocumentSnapshot data: ${dataSnapshot.child("admin").getValue()} "
//                            )



                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    startActivity(Intent(this@DetalhesEventoActivity,DetalhesEventoActivity::class.java))
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    startActivity(Intent(this@DetalhesEventoActivity, FiltrosActivity::class.java))
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }


            }
            mail.addChildEventListener(m)
        }

    }


    fun marcarPresença() {

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.getReference("Eventos").child(gv.detalhes)
            mail.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {


                    val buscarNome = mAuth.getReference("Users").child(user.uid)
                    buscarNome.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {



                            val update = HashMap<String, Any>()
                            update["Presenças"] = arrayListOf(user.uid)

                            mAuth.getReference("Eventos").child(gv.detalhes)
                                .updateChildren(update)

                            Log.d(
                                "evento", "DocumentSnapshot data: ${dataSnapshot.child("admin").getValue()} "
                            )

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                }
            })


        }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
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
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this, VerGrupoActivity::class.java))
        }

        if (item.itemId == R.id.Lis) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.home) {
            val marca = 0
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }


        return super.onOptionsItemSelected(item)
    }

}

