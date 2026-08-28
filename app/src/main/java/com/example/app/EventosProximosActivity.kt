package com.example.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.example.app.databinding.ActivityEventosProximosBinding
import java.util.Locale

/**
 * Descoberta de eventos perto de um local — próprio ou pesquisado — com
 * filtros de raio e de tipo (público/privado), em lista ou em mapa (com
 * agrupamento de marcadores quando há muitos próximos uns dos outros).
 *
 * Ver docs/PLANO_DESENVOLVIMENTO.md para o histórico desta funcionalidade
 * (cresceu bastante desde a versão inicial, mais simples) e as decisões de
 * performance tomadas para não ficar lento à medida que a plataforma
 * cresce: as duas pesquisas (públicos/privados) correm em paralelo, cada
 * uma só corre se o filtro correspondente estiver ligado, a pesquisa de
 * texto→coordenadas corre sempre fora da thread principal, e o número de
 * eventos públicos descarregados continua limitado a 200 (já indexado no
 * Firebase), independentemente de quantos existirem na plataforma.
 */
class EventosProximosActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityEventosProximosBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val mAuth = FirebaseDatabase.getInstance()

    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<EventoClusterItem>? = null
    private var aMostrarMapa = false

    private var eventosAtuais: List<EventoEncontrado> = emptyList()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2001
        private val RAIOS_KM = listOf(5, 10, 25, 50, 100, -1) // -1 = sem limite
    }

    private data class EventoEncontrado(
        val nome: String,
        val lat: Double,
        val lon: Double,
        val privado: Boolean,
        val numeroGrupo: String,
        val distanciaMetros: Float
    )

    private class EventoClusterItem(
        private val posicao: LatLng,
        val nomeExibicao: String,
        val nomeReal: String,
        val privado: Boolean,
        val numeroGrupo: String
    ) : ClusterItem {
        override fun getPosition(): LatLng = posicao
        override fun getTitle(): String = nomeExibicao
        override fun getSnippet(): String? = null
        override fun getZIndex(): Float? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventosProximosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        configurarSpinnerRaio()

        (supportFragmentManager.findFragmentById(R.id.mapaEventosProximos) as? SupportMapFragment)
            ?.getMapAsync(this)

        binding.bAplicarFiltros.evitarDuploClique {
            pesquisar()
        }

        binding.bAlternarVista.setOnClickListener {
            aMostrarMapa = !aMostrarMapa
            atualizarVisibilidadeVista()
        }

        binding.swipeRefreshEventosProximos.setOnRefreshListener {
            pesquisar()
        }

        pesquisar()
    }

    private fun configurarSpinnerRaio() {
        val nomes = listOf("5 km", "10 km", "25 km", "50 km", "100 km", "Sem limite")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRaio.adapter = adapter
        binding.spinnerRaio.setSelection(2) // 25 km por omissão
    }

    private fun atualizarVisibilidadeVista() {
        binding.ListViewEventosProximos.isVisible = !aMostrarMapa
        binding.mapaEventosProximos.isVisible = aMostrarMapa
        binding.bAlternarVista.text = if (aMostrarMapa) "Ver lista" else "Ver mapa"
        if (aMostrarMapa) {
            desenharMarcadoresNoMapa()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val manager = ClusterManager<EventoClusterItem>(this, map)
        clusterManager = manager
        map.setOnCameraIdleListener(manager)
        map.setOnMarkerClickListener(manager)
        manager.setOnClusterItemClickListener { item ->
            val gv = application as VariaveisGlobais
            gv.detalhes = item.nomeReal
            gv.detalhesPrivado = item.privado
            gv.detalhesNumeroGrupo = item.numeroGrupo
            startActivity(Intent(this, DetalhesEventoActivity::class.java))
            true
        }
        if (aMostrarMapa) {
            desenharMarcadoresNoMapa()
        }
    }

    private fun pesquisar() {
        val texto = binding.editLocalPesquisa.text.toString().trim()
        if (texto.isEmpty()) {
            usarLocalizacaoAtual()
        } else {
            geocodificarEmSegundoPlano(texto)
        }
    }

    private fun usarLocalizacaoAtual() {
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
                carregarEventos(LatLng(location.latitude, location.longitude))
            } else {
                binding.progressEventosProximos.isVisible = false
                binding.swipeRefreshEventosProximos.isRefreshing = false
                Toast.makeText(
                    this,
                    "Não foi possível obter a tua localização atual. Verifica se o GPS está ligado.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.addOnFailureListener {
            binding.progressEventosProximos.isVisible = false
            binding.swipeRefreshEventosProximos.isRefreshing = false
            Toast.makeText(this, "Erro ao obter localização.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Geocoder.getFromLocationName é uma chamada de REDE BLOQUEANTE — nunca
     * pode correr na thread principal (bloquearia a app inteira até
     * responder, podendo mesmo causar um crash de "não está a responder"
     * com ligação lenta). Corre sempre numa thread à parte, o resultado
     * volta para a thread principal via runOnUiThread.
     */
    private fun geocodificarEmSegundoPlano(texto: String) {
        binding.progressEventosProximos.isVisible = true

        Thread {
            var resultado: LatLng? = null
            try {
                val geocoder = Geocoder(this, Locale.getDefault())
                @Suppress("DEPRECATION")
                val enderecos = geocoder.getFromLocationName(texto, 1)
                if (!enderecos.isNullOrEmpty()) {
                    val endereco = enderecos[0]
                    resultado = LatLng(endereco.latitude, endereco.longitude)
                }
            } catch (e: Exception) {
                Log.d("EventosProximos", "erro no geocoder: ${e.message}")
            }

            runOnUiThread {
                val pontoEncontrado = resultado
                if (pontoEncontrado != null) {
                    carregarEventos(pontoEncontrado)
                } else {
                    binding.progressEventosProximos.isVisible = false
                    binding.swipeRefreshEventosProximos.isRefreshing = false
                    Toast.makeText(
                        this,
                        "Não foi possível encontrar esse local. Tenta escrever de outra forma.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                usarLocalizacaoAtual()
            } else {
                binding.progressEventosProximos.isVisible = false
                binding.swipeRefreshEventosProximos.isRefreshing = false
                Toast.makeText(
                    this,
                    "Sem permissão de localização não é possível pesquisar pela tua posição atual — tenta escrever um local.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun carregarEventos(pontoPesquisa: LatLng) {
        val querPublicos = binding.checkPublicos.isChecked
        val querPrivados = binding.checkPrivados.isChecked

        if (!querPublicos && !querPrivados) {
            binding.progressEventosProximos.isVisible = false
            binding.swipeRefreshEventosProximos.isRefreshing = false
            mostrarResultado(emptyList(), pontoPesquisa)
            return
        }

        val eventosBrutos = java.util.Collections.synchronizedList(
            ArrayList<Triple<DataSnapshot, Boolean, String>>() // snapshot, privado, nomeChave
        )

        var tarefasPendentes = 0
        if (querPublicos) tarefasPendentes++
        if (querPrivados) tarefasPendentes++

        fun tarefaConcluida() {
            tarefasPendentes--
            if (tarefasPendentes == 0) {
                processarResultados(eventosBrutos, pontoPesquisa)
            }
        }

        // Pesquisa de eventos PÚBLICOS — mesma query já otimizada e indexada
        // usada antes (dataFimTimestamp, limite de 200), só corre se o
        // filtro "Públicos" estiver ligado. Lê agora de EventosPublicos em
        // vez do antigo nó único "Eventos" (ver docs/PLANO_DESENVOLVIMENTO.md,
        // separação por privacidade) — já não é preciso filtrar por "Forma"
        // no cliente, o próprio caminho garante que só há eventos públicos aqui.
        if (querPublicos) {
            val agora = System.currentTimeMillis().toDouble()
            mAuth.getReference("EventosPublicos")
                .orderByChild("dataFimTimestamp")
                .startAt(agora)
                .limitToFirst(200)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (eventoSnap in snapshot.children) {
                            eventosBrutos.add(Triple(eventoSnap, false, eventoSnap.key ?: ""))
                        }
                        tarefaConcluida()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("EventosProximos", "erro na query publica: ${error.message}")
                        tarefaConcluida()
                    }
                })
        }

        // Pesquisa de eventos PRIVADOS dos grupos do próprio utilizador —
        // mesma lógica já usada em MeusEventosActivity (uma query por grupo,
        // tipicamente 1-3 grupos por pessoa). Lê agora
        // EventosPrivados/{numeroGrupo} diretamente — já não precisa de
        // orderByChild("numeroGrupo").equalTo(), porque o número do grupo já
        // é o próprio caminho, não um valor a comparar (também elimina a
        // necessidade de converter numeroStr para Double).
        if (querPrivados) {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                tarefaConcluida()
            } else {
                mAuth.getReference("Users").child(uid).child("Grupos")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(gruposSnapshot: DataSnapshot) {
                            val numerosGrupos = gruposSnapshot.children.mapNotNull { it.key }
                            if (numerosGrupos.isEmpty()) {
                                tarefaConcluida()
                                return
                            }

                            var subPendentes = numerosGrupos.size
                            for (numeroStr in numerosGrupos) {
                                mAuth.getReference("EventosPrivados").child(numeroStr)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (eventoSnap in snapshot.children) {
                                                eventosBrutos.add(
                                                    Triple(eventoSnap, true, eventoSnap.key ?: "")
                                                )
                                            }
                                            subPendentes--
                                            if (subPendentes == 0) tarefaConcluida()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            subPendentes--
                                            if (subPendentes == 0) tarefaConcluida()
                                        }
                                    })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("EventosProximos", "erro a ler grupos do utilizador: ${error.message}")
                            tarefaConcluida()
                        }
                    })
            }
        }
    }

    private fun processarResultados(
        eventosBrutos: List<Triple<DataSnapshot, Boolean, String>>,
        pontoPesquisa: LatLng
    ) {
        val agora = System.currentTimeMillis()
        val raioKm = RAIOS_KM.getOrElse(binding.spinnerRaio.selectedItemPosition) { 25 }

        val nomesVistos = HashSet<String>() // evita duplicados se um evento aparecer nas duas queries
        val resultado = ArrayList<EventoEncontrado>()

        for ((snapshot, privado, chave) in eventosBrutos) {
            if (!nomesVistos.add(chave)) continue

            val dataFim = snapshot.child("dataFimTimestamp").getValue(Long::class.java)
            if (dataFim != null && dataFim < agora) continue // já terminou

            val lat = snapshot.child("Latitude").getValue(Double::class.java)
            val lon = snapshot.child("Longitude").getValue(Double::class.java)
            val nome = snapshot.child("nome").getValue(String::class.java) ?: chave
            val numeroGrupo = snapshot.child("numeroGrupo").getValue().toString()
            if (lat == null || lon == null) continue

            val resultadoDistancia = FloatArray(1)
            Location.distanceBetween(
                pontoPesquisa.latitude, pontoPesquisa.longitude,
                lat, lon,
                resultadoDistancia
            )
            val distanciaMetros = resultadoDistancia[0]

            if (raioKm != -1 && distanciaMetros > raioKm * 1000) continue // fora do raio escolhido

            resultado.add(EventoEncontrado(nome, lat, lon, privado, numeroGrupo, distanciaMetros))
        }

        resultado.sortBy { it.distanciaMetros }

        runOnUiThread {
            binding.progressEventosProximos.isVisible = false
            binding.swipeRefreshEventosProximos.isRefreshing = false
            mostrarResultado(resultado, pontoPesquisa)
        }
    }

    private fun mostrarResultado(eventos: List<EventoEncontrado>, pontoPesquisa: LatLng) {
        eventosAtuais = eventos
        binding.tSemEventosProximos.isVisible = eventos.isEmpty()

        // Lista de texto — privados marcados com 🔒, mesmo padrão já usado em
        // HomeActivity, para consistência em toda a app.
        val linhas = eventos.map { evento ->
            val prefixo = if (evento.privado) "🔒 " else ""
            val distanciaTexto = if (evento.distanciaMetros >= 1000) {
                String.format("%.1f km", evento.distanciaMetros / 1000)
            } else {
                "${evento.distanciaMetros.toInt()} m"
            }
            "$prefixo${evento.nome} — $distanciaTexto"
        }
        val adapter = ArrayAdapter(this, R.layout.listview_item, linhas)
        binding.ListViewEventosProximos.adapter = adapter

        binding.ListViewEventosProximos.setOnItemClickListener { _, _, position, _ ->
            val gv = application as VariaveisGlobais
            gv.detalhes = eventos[position].nome
            gv.detalhesPrivado = eventos[position].privado
            gv.detalhesNumeroGrupo = eventos[position].numeroGrupo
            startActivity(Intent(this, DetalhesEventoActivity::class.java))
        }

        if (aMostrarMapa) {
            desenharMarcadoresNoMapa()
        }
    }

    private fun desenharMarcadoresNoMapa() {
        val manager = clusterManager ?: return
        val map = googleMap ?: return

        manager.clearItems()

        for (evento in eventosAtuais) {
            val prefixo = if (evento.privado) "🔒 " else ""
            manager.addItem(
                EventoClusterItem(
                    LatLng(evento.lat, evento.lon),
                    "$prefixo${evento.nome}",
                    evento.nome,
                    evento.privado,
                    evento.numeroGrupo
                )
            )
        }
        manager.cluster()

        if (eventosAtuais.isNotEmpty()) {
            val primeiro = eventosAtuais.first()
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(primeiro.lat, primeiro.lon), 10f)
            )
        }
    }
}
