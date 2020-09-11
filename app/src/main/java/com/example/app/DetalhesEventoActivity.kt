package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detalhes_evento.*
import java.util.HashMap
import androidx.core.view.isVisible as isVisible

class DetalhesEventoActivity : AppCompatActivity(), OnMapReadyCallback  {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
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
            startActivity(Intent(this, HomeActivity::class.java))
        }

        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Eventos").document(gv.detalhes)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val name = document.data?.get("nome")
                    val dateDia = document.data?.get("dia")
                    val dateMes = document.data?.get("mes")
                    val dateAno = document.data?.get("ano")
                    val time = document.data?.get("horas")
                    val tipo = document.data?.get("Tipo")


                    showDetalhe.text =
                        "nome: " + name + "\n" + "data: " + dateDia + "/" + dateMes + "/" + dateAno + "\n" + "horas: " + time + "\n" + "tipo: " + tipo





//                    Log.d(
//                        "evento",
//                        "DocumentSnapshot data: ${document.data?.get("nome")} \n${document.data?.get(
//                            "data"
//                        )}" +
//                                " \n ${document.data?.get("hora")} \n ${document.data?.get("local")}"
//                    )
                } else {
                    Log.d("evento", "No such document")
                }
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mail = mAuth.collection("Eventos").document(gv.detalhes)
        mail.get().addOnSuccessListener { document ->
            val placeLat = document.data?.get("Latitude")
            val placeLog = document.data?.get("Longitude")
            val P = LatLng(placeLat.toString().toDouble(), placeLog.toString().toDouble())

            placeMarkerOnMap(P)
            mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(P, 18f))
        }

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

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Eventos").document(gv.detalhes)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val pre = document.data?.get("Presenças") as List<String>

                    val buscarNome = mAuth.collection("Users").document(user.uid)
                    buscarNome.get().addOnSuccessListener { document ->
                        if (document != null) {

                            val nameUser = document.data?.get("name")
                            if (pre.contains(nameUser)) {

                                marcar.isVisible = false
                                Log.d(
                                    "detalhes", "detalhe: $pre" +
                                            "ffff: $nameUser" + "\n" + "false"
                                )
                            } else {

                                marcar.isVisible = true
                                Log.d(
                                    "detalhes", "detalhe: $pre" +
                                            "ffff: $nameUser" + "\n" + "true"
                                )
                            }
                        }
                    }

                    Log.d(
                        "evento", "DocumentSnapshot data: ${document.data?.get("admin")} "
                    )
                } else {
                    Log.d("evento", "No such document")
                }
            }
        }

    }


    fun marcarPresença() {

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Eventos").document(gv.detalhes)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val buscarNome = mAuth.collection("Users").document(user.uid)
                    buscarNome.get().addOnSuccessListener { document ->
                        if (document != null) {

                            val nameUser = document.data?.get("name")
                            val update = HashMap<String, Any>()
                            update["Presenças"] = arrayListOf(nameUser)

                            mAuth.collection("Eventos").document(gv.detalhes)
                                .update("Presenças", FieldValue.arrayUnion(nameUser))

                        }
                    }

                    Log.d(
                        "evento", "DocumentSnapshot data: ${document.data?.get("admin")} "
                    )
                } else {
                    Log.d("evento", "No such document")
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
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this,VerGrupoActivity::class.java))
        }

        if (item.itemId == R.id.Lista) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

}
