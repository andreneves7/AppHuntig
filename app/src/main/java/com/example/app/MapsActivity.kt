package com.example.app

//import com.google.android.gms.location.places.ui.PlacePicker
//import com.google.android.gms.location.places.ui.PlacePicker

//import com.google.android.material.floatingactionbutton.FloatingActionButton

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    lateinit var gv: VariaveisGlobais

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        gv = application as VariaveisGlobais
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                //placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }





        createLocationRequest()

        search()



        val guardarEvento = bGuardar
        guardarEvento.setOnClickListener {
            evento()
        }


    }

    private fun search(){
        val apiKey = getString(R.string.api_key)


        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }


        val placesClient: PlacesClient = Places.createClient(this)




        val autocompleteFragment: AutocompleteSupportFragment? =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment?.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {



                gv.Lat = place.latLng?.latitude.toString().toDouble()
                gv.Long = place.latLng?.longitude.toString().toDouble()
                val currentLatLng = LatLng(gv.Lat,gv.Long)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                Log.d(
                    "Mapa",
                    "Place: " + place.getName().toString() + ", " + place.getId() + "," + place.latLng?.latitude.toString().toDouble()
                + ", " + place.latLng?.longitude.toString().toDouble()
                )
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.d("Mapa", "An error occurred: $status")
            }
        })



    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2


    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMap()
    }

    override fun onMarkerClick(p0: Marker?) = false


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }


        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_HYBRID


        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                Log.d("Mapa", "$lastLocation}")
                Log.d("Mapa", "latitude ${location.latitude}}")
                Log.d("Mapa", "longitude ${location.longitude}}")
                gv.Lat = location.latitude
                gv.Long = location.longitude

            }
        }

    }



    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }


    }




    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    private fun placeMarkerOnMap(location: LatLng) {
        // 1
        val markerOptions = MarkerOptions().position(location)
        // 2
        map.clear()
        map.addMarker(markerOptions)
    }

    private fun evento() {


        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val name = document.data?.get("name")


                    if (gv.Lat != 0.0 && gv.Long != 0.0) {


                        val evento = HashMap<String, Any>()
                        evento["nome"] = gv.nome
                        evento["Presenças"] = ArrayList<String>()
                        evento["horas"] = gv.Horas
                        evento["dia"] = gv.Day
                        evento["mes"] = gv.Month
                        evento["ano"] = gv.Year
                        evento["Tipo"] = gv.check
                        evento["Latitude"] = gv.Lat
                        evento["Longitude"] = gv.Long
                        mAuth.collection("Eventos").document(gv.nome)
                            .set(evento)

                        val up = HashMap<String, Any>()
                        up["Eventos"] = arrayListOf(gv.nome)
                        mAuth.collection("Grupos").document(gv.Evento)
                            .update("Eventos", FieldValue.arrayUnion(gv.nome))

                        //tirar criador nao conta
                        val upd = HashMap<String, Any>()
                        upd["Presenças"] = arrayListOf(name)
                        mAuth.collection("Eventos").document(gv.nome)
                            .update("Presenças", FieldValue.arrayUnion(name))

                        Toast.makeText(this, "evento criado", Toast.LENGTH_SHORT).show()


                        val intent = Intent(this, GrupoActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)

                        Log.d(
                            "evento", "DocumentSnapshot data: ${document.data?.get("name")}"
                        )
                    } else {
                        Toast.makeText(this, "Tem de ter localização", Toast.LENGTH_SHORT).show()
                        Log.d("Mapa", "$lastLocation}")
                        Log.d("Mapa", "latitude ${gv.Lat}}")
                        Log.d("Mapa", "longitude ${gv.Long}}")

                    }
                } else {
                    Log.d("evento", "No such document")
                }
            }
        }
    }

}



