package com.example.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.app.databinding.ActivityEventosProximosBinding

/**
 * Lista eventos PÚBLICOS futuros, ordenados pelo mais perto do mais longe da
 * localização atual do utilizador.
 *
 * Âmbito deliberadamente mais simples do que HomeActivity: só eventos
 * públicos (não tenta replicar toda a lógica de eventos privados por grupo,
 * que já é bastante complexa em HomeActivity) — isto é um ecrã de
 * descoberta, não substitui a lista principal.
 *
 * O Firebase Realtime Database não suporta pesquisa geográfica nativa (isso
 * precisaria de uma biblioteca extra como GeoFire, não incluída aqui para
 * não arriscar uma dependência nova não testada). Em vez disso: descarrega
 * os eventos futuros (já limitados e ordenados por data, tal como
 * HomeActivity) e calcula a distância a cada um no cliente, usando
 * Location.distanceBetween — API nativa do Android, sem dependências novas.
 */
class EventosProximosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventosProximosBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val mAuth = FirebaseDatabase.getInstance()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventosProximosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        pedirLocalizacaoECarregar()
    }

    private fun pedirLocalizacaoECarregar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        binding.progressEventosProximos.isVisible = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                carregarEventosProximos(location)
            } else {
                binding.progressEventosProximos.isVisible = false
                Toast.makeText(
                    this,
                    "Não foi possível obter a tua localização atual. Verifica se o GPS está ligado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.addOnFailureListener {
            binding.progressEventosProximos.isVisible = false
            Toast.makeText(this, "Erro ao obter localização.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pedirLocalizacaoECarregar()
            } else {
                Toast.makeText(
                    this,
                    "Sem permissão de localização não é possível mostrar eventos próximos.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private data class EventoComDistancia(
        val nome: String,
        val distanciaMetros: Float
    )

    private fun carregarEventosProximos(localizacaoAtual: Location) {
        val agora = System.currentTimeMillis()

        // Mesma ordenação/limite já usado em HomeActivity (ver
        // docs/PLANO_DESENVOLVIMENTO.md, secção de paginação) — eventos
        // futuros, ordenados por data de fim, até 200.
        val ref = mAuth.getReference("Eventos")
            .orderByChild("dataFimTimestamp")
            .startAt(agora.toDouble())
            .limitToFirst(200)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressEventosProximos.isVisible = false

                val comDistancia = ArrayList<EventoComDistancia>()

                for (eventoSnap in snapshot.children) {
                    val forma = eventoSnap.child("Forma").getValue(String::class.java)
                    if (forma != "publico") {
                        // Só eventos públicos neste ecrã de descoberta — ver
                        // explicação no comentário da classe.
                        continue
                    }

                    val lat = eventoSnap.child("Latitude").getValue(Double::class.java)
                    val lon = eventoSnap.child("Longitude").getValue(Double::class.java)
                    val nome = eventoSnap.child("nome").getValue(String::class.java)
                        ?: eventoSnap.key ?: continue

                    if (lat == null || lon == null) continue

                    val resultado = FloatArray(1)
                    Location.distanceBetween(
                        localizacaoAtual.latitude, localizacaoAtual.longitude,
                        lat, lon,
                        resultado
                    )
                    comDistancia.add(EventoComDistancia(nome, resultado[0]))
                }

                comDistancia.sortBy { it.distanciaMetros }

                binding.tSemEventosProximos.isVisible = comDistancia.isEmpty()

                val linhas = comDistancia.map { evento ->
                    val distanciaTexto = if (evento.distanciaMetros >= 1000) {
                        String.format("%.1f km", evento.distanciaMetros / 1000)
                    } else {
                        "${evento.distanciaMetros.toInt()} m"
                    }
                    "${evento.nome} — $distanciaTexto"
                }

                val adapter = ArrayAdapter(this@EventosProximosActivity, R.layout.listview_item, linhas)
                binding.ListViewEventosProximos.adapter = adapter

                binding.ListViewEventosProximos.setOnItemClickListener { _, _, position, _ ->
                    val gv = application as VariaveisGlobais
                    gv.detalhes = comDistancia[position].nome
                    startActivity(Intent(this@EventosProximosActivity, DetalhesEventoActivity::class.java))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressEventosProximos.isVisible = false
                Log.d("EventosProximos", "erro Firebase: ${error.message}")
                this@EventosProximosActivity.mostrarErroLigacao()
            }
        })
    }
}
