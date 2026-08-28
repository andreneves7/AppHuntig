package com.example.app

//import com.google.android.gms.location.places.ui.PlacePicker
//import com.google.android.gms.location.places.ui.PlacePicker

//import com.google.android.material.floatingactionbutton.FloatingActionButton

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.app.databinding.ActivityMapsBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnMarkerClickListener {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    lateinit var gv: VariaveisGlobais

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
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


        val guardarEvento = binding.bGuardar
        guardarEvento.evitarDuploClique {
            pedirLimiteEGuardarEvento()
        }


    }

    /**
     * Pede o limite de participantes (opcional) antes de gravar o evento.
     * Não está no layout activity_evento.xml porque esse ecrã já está
     * totalmente ocupado (todos os campos posicionados por bias específico,
     * sem espaço livre para acrescentar um campo novo em segurança sem
     * conseguir ver o resultado). Mesmo padrão de diálogo já usado em
     * LoginActivity.recuperarPassword().
     */
    private fun pedirLimiteEGuardarEvento() {
        val input = android.widget.EditText(this)
        input.hint = "Deixa vazio para sem limite"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        val padding = (16 * resources.displayMetrics.density).toInt()
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = padding
        params.rightMargin = padding
        input.layoutParams = params
        container.addView(input)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Limite de participantes")
            .setMessage("Quantas pessoas podem inscrever-se, no máximo? Quem se tentar inscrever depois de atingido o limite entra numa lista de espera.")
            .setView(container)
            .setCancelable(true)
            .setNegativeButton("Sem limite") { _, _ ->
                evento(0)
            }
            .setPositiveButton("Guardar") { _, _ ->
                val texto = input.text.toString().trim()
                val limite = texto.toIntOrNull() ?: 0
                evento(limite)
            }
            .show()
    }

    private fun search() {
        // Consolidado: antes havia duas chaves duplicadas (R.string.api_key em
        // strings.xml, e R.string.google_maps_key em google_maps_api.xml) com o
        // mesmo valor. Passa a usar só uma fonte, para nunca ficarem dessincronizadas
        // se alguma for regenerada.
        val apiKey = getString(R.string.google_maps_key)


        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }


        val placesClient: PlacesClient = Places.createClient(this)


        val autocompleteFragment: AutocompleteSupportFragment? =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        autocompleteFragment?.setPlaceFields(
            Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )

        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

//                var x = place.latLng!!.latitude
//                var y = place.latLng!!.longitude
//
//
//                val point = GeoPoint(x, y)

                gv.Lat = place.latLng?.latitude.toString().toDouble()
                gv.Long = place.latLng?.longitude.toString().toDouble()
                val currentLatLng = LatLng(gv.Lat, gv.Long)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                Log.d(
                    "Mapa",
                    "Place: " + place.getName()
                        .toString() + ", " + place.getId() + "," + gv.Lat + ", " + gv.Long
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
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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

    private fun evento(limiteParticipantes: Int) {
        val user = Auth.currentUser
        if (user != null) {


            var numero = intent.getStringExtra(EXTRA_MESSAGE).toInt()
            Log.d("Numero", "ola3 = $numero")




            if (gv.Lat != 0.0 && gv.Long != 0.0) {


                val evento = HashMap<String, Any>()
                evento["nome"] = gv.nome
                //evento["Presenças"] = ArrayList<String>()
                evento["horas"] = gv.Horas
                evento["dia"] = gv.Day
                evento["mes"] = gv.Month
                evento["ano"] = gv.Year
                evento["diaFim"] = gv.DayFim
                evento["mesFim"] = gv.MonthFim
                evento["anoFim"] = gv.YearFim
                // Campo calculado, necessário para o Firebase conseguir ordenar/paginar
                // eventos (orderByChild só ordena por UM campo — não existia nenhum campo
                // único e ordenável antes disto, só dia/mes/ano separados). Representa a
                // data de FIM do evento (não a de início), porque é essa que decide se um
                // evento ainda deve aparecer na lista (ver HomeActivity.eventos()).
                // Eventos criados antes desta alteração não têm este campo — o Firebase
                // trata-os como "menores que qualquer valor" numa orderByChild, o que
                // significa que vão aparecer primeiro numa ordenação ascendente e ser os
                // primeiros a cair fora de um limitToLast() à medida que a lista cresce.
                val calendarioFim = java.util.Calendar.getInstance()
                calendarioFim.set(gv.YearFim, gv.MonthFim - 1, gv.DayFim, 23, 59, 59)
                evento["dataFimTimestamp"] = calendarioFim.timeInMillis
                evento["Tipo"] = gv.check
                evento["Forma"] = gv.privado
                evento["Latitude"] = gv.Lat
                evento["Longitude"] = gv.Long
                evento["numeroGrupo"] = numero
                // 0 = sem limite. Ver DetalhesEventoActivity.marcarPresença() para a
                // lógica que usa este campo (lista de espera quando o evento enche).
                evento["limiteParticipantes"] = limiteParticipantes


                // Separado em dois nós (EventosPublicos / EventosPrivados) em vez de
                // um só "Eventos" — resolve um problema real de privacidade: as
                // regras do Firebase não conseguem filtrar LISTAS (só decidem se um
                // pedido passa ou não, por inteiro), por isso uma única árvore
                // "Eventos" com leitura ampla (necessária para a pesquisa de todos
                // os eventos públicos) deixava sempre os privados igualmente
                // legíveis por qualquer conta autenticada, mesmo sem pertencer ao
                // grupo. Ver docs/PLANO_DESENVOLVIMENTO.md.
                if (gv.privado == "privado") {
                    mAuth.getReference("EventosPrivados").child(numero.toString()).child(gv.nome)
                        .setValue(evento)
                } else {
                    mAuth.getReference("EventosPublicos").child(gv.nome).setValue(evento)
                }



                Toast.makeText(this, this.getString(R.string.msg_evento_criado), Toast.LENGTH_SHORT).show()


                val intent = Intent(this, CriarOrgEventoActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


            } else {
                Toast.makeText(this, this.getString(R.string.msg_tem_de_ter_localizacao), Toast.LENGTH_SHORT).show()
                Log.d("Mapa", "$lastLocation}")
                Log.d("Mapa", "latitude ${gv.Lat}}")
                Log.d("Mapa", "longitude ${gv.Long}}")

            }

        }
    }
}





