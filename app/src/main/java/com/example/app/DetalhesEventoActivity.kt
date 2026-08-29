package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityDetalhesEventoBinding
import java.util.HashMap
import androidx.core.view.isVisible as isVisible

class DetalhesEventoActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetalhesEventoBinding
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais

    private lateinit var mMap: GoogleMap

    /**
     * Devolve a referência do Firebase para o evento atualmente aberto,
     * resolvendo para EventosPublicos ou EventosPrivados/{numeroGrupo}
     * consoante gv.detalhesPrivado/gv.detalhesNumeroGrupo — preenchidos por
     * quem quer que tenha navegado para este ecrã (ver VariaveisGlobais).
     * Centralizado aqui para não repetir esta condição em cada sítio que
     * precisa de aceder ao evento.
     */
    private fun referenciaEventoAtual(): DatabaseReference {
        return if (gv.detalhesPrivado) {
            mAuth.getReference("EventosPrivados").child(gv.detalhesNumeroGrupo).child(gv.detalhes)
        } else {
            mAuth.getReference("EventosPublicos").child(gv.detalhes)
        }
    }

    // Guardados quando o evento é lido em onCreate, para poderem ser
    // reutilizados em adicionarAoCalendario() sem precisar de nova leitura
    // ao Firebase.
    private var eventoNome: String? = null
    private var eventoDia: Int? = null
    private var eventoMes: Int? = null
    private var eventoAno: Int? = null
    private var eventoDiaFim: Int? = null
    private var eventoMesFim: Int? = null
    private var eventoAnoFim: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        binding = ActivityDetalhesEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Se este ecrã foi aberto a partir de um link de partilha
        // (apphuntig://evento?...), interpreta os parâmetros e preenche
        // gv.detalhes/detalhesPrivado/detalhesNumeroGrupo aqui — nos outros
        // casos (navegação normal a partir de outro ecrã da app), estes já
        // vêm preenchidos por quem chamou startActivity, e intent.data é nulo.
        val dadosLink = intent.data
        if (dadosLink != null) {
            val nome = dadosLink.getQueryParameter("nome")
            if (nome.isNullOrEmpty()) {
                Toast.makeText(this, "Link de evento inválido.", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            gv.detalhes = nome
            gv.detalhesPrivado = dadosLink.getQueryParameter("privado") == "true"
            gv.detalhesNumeroGrupo = dadosLink.getQueryParameter("grupo") ?: ""
        }

        val showDetalhe = binding.tShowDetalhes
        val marcar = binding.bPresença

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(this)



        desativar()

        marcar.evitarDuploClique {
            marcarPresença()
            val marca = 1
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }

        val user = Auth.currentUser

        if (user != null) {
            val mail = referenciaEventoAtual()
            mail.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val name = dataSnapshot.child("nome").getValue().toString()
                    val dateDia = dataSnapshot.child("dia").getValue().toString()
                    val dateMes = dataSnapshot.child("mes").getValue().toString()
                    val dateAno = dataSnapshot.child("ano").getValue().toString()
                    val time = dataSnapshot.child("horas").getValue().toString()
                    val tipo = dataSnapshot.child("Tipo").getValue().toString()
                    val limite = dataSnapshot.child("limiteParticipantes").getValue(Long::class.java) ?: 0L
                    val numAtual = dataSnapshot.child("Presenças").childrenCount

                    // Guardados para uso posterior em adicionarAoCalendario().
                    eventoNome = name
                    eventoDia = dataSnapshot.child("dia").getValue(Int::class.java)
                    eventoMes = dataSnapshot.child("mes").getValue(Int::class.java)
                    eventoAno = dataSnapshot.child("ano").getValue(Int::class.java)
                    eventoDiaFim = dataSnapshot.child("diaFim").getValue(Int::class.java)
                    eventoMesFim = dataSnapshot.child("mesFim").getValue(Int::class.java)
                    eventoAnoFim = dataSnapshot.child("anoFim").getValue(Int::class.java)

                    val textoVagas = if (limite > 0) {
                        "\nvagas: $numAtual/$limite"
                    } else {
                        ""
                    }

                    showDetalhe.text =
                        "nome: $name\ndata: $dateDia/$dateMes/$dateAno\nhoras: $time\ntipo: $tipo$textoVagas"


//                    Log.d(
//                        "evento",
//                        "DocumentSnapshot data: ${document.data?.get("nome")} \n${document.data?.get(
//                            "data"
//                        )}" +
//                                " \n ${document.data?.get("hora")} \n ${document.data?.get("local")}"
//                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@DetalhesEventoActivity.mostrarErroLigacao()
                }
            })
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mail = referenciaEventoAtual()
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
                Log.d("todo_fix", "erro Firebase: ${error.message}")
                this@DetalhesEventoActivity.mostrarErroLigacao()
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

        val marcar = binding.bPresença
        val uid = Auth.currentUser?.uid
        val fazParte = ArrayList<String>()

        val user = Auth.currentUser
        if (user != null) {
            val mail = referenciaEventoAtual().child("Presenças")
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
                    startActivity(
                        Intent(
                            this@DetalhesEventoActivity,
                            DetalhesEventoActivity::class.java
                        )
                    )
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    startActivity(Intent(this@DetalhesEventoActivity, FiltrosActivity::class.java))
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@DetalhesEventoActivity.mostrarErroLigacao()
                }


            }
            mail.addChildEventListener(m)
        }

    }


    fun marcarPresença() {
        val user = Auth.currentUser
        if (user != null) {
            val mail = referenciaEventoAtual()
            mail.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    // BUG CORRIGIDO: a versão anterior usava valu.size como próximo
                    // índice, mas valu só recebia SEMPRE um elemento (a lista
                    // Presenças inteira convertida para string, de uma só vez) — ou
                    // seja, o índice era sempre "1", e cada nova pessoa que marcasse
                    // presença SOBRESCREVIA a marcação da pessoa anterior, perdendo
                    // dados de presença silenciosamente. childrenCount() dá a
                    // contagem real de entradas já existentes.
                    val numAtual = dataSnapshot.child("Presenças").childrenCount

                    val limite = dataSnapshot.child("limiteParticipantes").getValue(Long::class.java) ?: 0L

                    Log.d(
                        "evento",
                        "presencas atuais: $numAtual, limite: $limite"
                    )

                    val update = HashMap<String, Any>()

                    if (limite > 0 && numAtual >= limite) {
                        // Evento cheio — entra na lista de espera em vez de Presenças.
                        update["$numAtual"] = user.uid
                        referenciaEventoAtual().child("ListaEspera")
                            .updateChildren(update)
                        Toast.makeText(
                            this@DetalhesEventoActivity,
                            "Este evento já atingiu o limite de participantes — ficaste na lista de espera.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        update["$numAtual"] = user.uid
                        referenciaEventoAtual().child("Presenças")
                            .updateChildren(update)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@DetalhesEventoActivity.mostrarErroLigacao()
                }
            })
        }
    }

    /**
     * Abre o calendário nativo do telemóvel com os dados do evento
     * pré-preenchidos, para o utilizador confirmar e guardar. Usa a API
     * pública ACTION_INSERT do CalendarContract — não precisa de nenhuma
     * permissão especial (a diferença de escrever diretamente no calendário,
     * que exigiria permissão WRITE_CALENDAR), porque quem confirma a
     * gravação final é sempre o utilizador, na própria app de calendário.
     * Documentação oficial: https://developer.android.com/guide/topics/providers/calendar-provider#intent-insert
     */
    private fun adicionarAoCalendario() {
        val dia = eventoDia
        val mes = eventoMes
        val ano = eventoAno

        if (dia == null || mes == null || ano == null) {
            Toast.makeText(this, "Ainda a carregar os dados do evento, tenta novamente.", Toast.LENGTH_SHORT).show()
            return
        }

        val inicio = java.util.Calendar.getInstance()
        inicio.set(ano, mes - 1, dia, 0, 0, 0)

        val fim = java.util.Calendar.getInstance()
        val diaFim = eventoDiaFim
        val mesFim = eventoMesFim
        val anoFim = eventoAnoFim
        if (diaFim != null && mesFim != null && anoFim != null) {
            fim.set(anoFim, mesFim - 1, diaFim, 23, 59, 59)
        } else {
            // Sem data de fim gravada — assume o mesmo dia, até ao final do dia.
            fim.set(ano, mes - 1, dia, 23, 59, 59)
        }

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(android.provider.CalendarContract.Events.CONTENT_URI)
            .putExtra(android.provider.CalendarContract.Events.TITLE, eventoNome ?: "Evento de caça")
            .putExtra(
                android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                inicio.timeInMillis
            )
            .putExtra(
                android.provider.CalendarContract.EXTRA_EVENT_END_TIME,
                fim.timeInMillis
            )

        try {
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // Telemóvel sem nenhuma app de calendário instalada — raro, mas acontece
            // em alguns dispositivos/emuladores personalizados.
            Toast.makeText(this, "Não foi encontrada nenhuma app de calendário no teu telemóvel.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_detalhes_evento, menu)
        return true
    }

    /**
     * Gera e mostra um QR code que codifica o nome do evento (a mesma chave
     * usada em Eventos/{nome} no Firebase — Eventos não foi migrado para
     * chave numérica, ao contrário de Grupos, ver
     * docs/PLANO_DESENVOLVIMENTO.md). Um caçador que leia este código com
     * "Check-in por QR Code" (menu principal) é levado diretamente para
     * este ecrã, já pronto para marcar presença.
     */
    /**
     * Descarrega a lista de presenças do evento, resolve o nome de cada uid,
     * e abre o seletor de partilha do Android (email, mensagens, etc.) com
     * uma lista de texto simples — mais compatível entre apps do que gerar
     * um ficheiro .csv real, que precisaria de configurar um FileProvider
     * (mais uma peça a testar sem conseguir compilar).
     */
    private fun exportarParticipantes() {
        val nomeEvento = gv.detalhes
        if (nomeEvento.isEmpty()) {
            Toast.makeText(this, "Evento ainda não identificado.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "A preparar lista de participantes…", Toast.LENGTH_SHORT).show()

        referenciaEventoAtual().child("Presenças")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(presencasSnapshot: DataSnapshot) {
                    val uids = presencasSnapshot.children.mapNotNull {
                        it.getValue(String::class.java)
                    }

                    if (uids.isEmpty()) {
                        Toast.makeText(
                            this@DetalhesEventoActivity,
                            "Ainda não há nenhum participante inscrito neste evento.",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    resolverNomesEPartilhar(nomeEvento, uids)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@DetalhesEventoActivity.mostrarErroLigacao()
                }
            })
    }

    private fun resolverNomesEPartilhar(nomeEvento: String, uids: List<String>) {
        val nomes = java.util.Collections.synchronizedList(ArrayList<String>())
        var pendentes = uids.size

        for (uid in uids) {
            mAuth.getReference("PerfisPublicos").child(uid).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val nome = snapshot.getValue(String::class.java) ?: "(nome desconhecido)"
                        nomes.add(nome)
                        pendentes--
                        if (pendentes == 0) {
                            partilharLista(nomeEvento, nomes)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        nomes.add("(nome desconhecido)")
                        pendentes--
                        if (pendentes == 0) {
                            partilharLista(nomeEvento, nomes)
                        }
                    }
                })
        }
    }

    private fun partilharLista(nomeEvento: String, nomes: List<String>) {
        val texto = buildString {
            append("Participantes — $nomeEvento\n\n")
            nomes.sorted().forEachIndexed { indice, nome ->
                append("${indice + 1}. $nome\n")
            }
            append("\nTotal: ${nomes.size}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Participantes — $nomeEvento")
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        startActivity(Intent.createChooser(intent, "Partilhar lista de participantes"))
    }

    /**
     * Partilha o evento — mensagem de texto com os detalhes (para quem não
     * tem a app), seguida de um link clicável (para quem já tem — abre
     * diretamente aqui, respeitando as mesmas regras de privacidade de
     * sempre, verificadas no Firebase).
     *
     * O link usa um esquema próprio da app (apphuntig://evento?...), que só
     * funciona em telemóveis que já têm a app instalada — ver
     * docs/PLANO_DESENVOLVIMENTO.md secção 10 para o caminho de evolução
     * para um link https:// verdadeiro (também funciona sem a app
     * instalada, leva à Play Store), que precisa de um domínio próprio e
     * fica documentado para mais tarde.
     */
    private fun partilharEvento() {
        val nomeEvento = gv.detalhes
        if (nomeEvento.isEmpty()) {
            Toast.makeText(this, "Evento ainda não identificado.", Toast.LENGTH_SHORT).show()
            return
        }

        val uriBuilder = android.net.Uri.Builder()
            .scheme("apphuntig")
            .authority("evento")
            .appendQueryParameter("nome", nomeEvento)
            .appendQueryParameter("privado", gv.detalhesPrivado.toString())
        if (gv.detalhesPrivado) {
            uriBuilder.appendQueryParameter("grupo", gv.detalhesNumeroGrupo)
        }
        val link = uriBuilder.build().toString()

        val dataTexto = if (eventoDia != null && eventoMes != null && eventoAno != null) {
            "\nData: $eventoDia/$eventoMes/$eventoAno"
        } else {
            ""
        }

        val texto = "Vem a este evento: $nomeEvento$dataTexto\n\n" +
            "Se já tens a HuntingEvent instalada, abre diretamente aqui:\n$link"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Convite: $nomeEvento")
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        startActivity(Intent.createChooser(intent, "Partilhar evento"))
    }

    private fun mostrarQRCode() {
        val nomeEvento = gv.detalhes
        if (nomeEvento.isEmpty()) {
            Toast.makeText(this, "Evento ainda não identificado.", Toast.LENGTH_SHORT).show()
            return
        }

        // Codifica tipo + grupo (se privado) + nome, separados por "::" — quem
        // ler este código (ver Utils.kt/processarResultadoScanQR) precisa desta
        // informação para saber em qual dos dois nós (EventosPublicos/
        // EventosPrivados) procurar o evento. Antes da separação por
        // privacidade, só o nome era codificado, porque só havia um nó.
        val conteudoQR = if (gv.detalhesPrivado) {
            "privado::${gv.detalhesNumeroGrupo}::$nomeEvento"
        } else {
            "publico::::$nomeEvento"
        }

        val tamanho = (250 * resources.displayMetrics.density).toInt()
        val bitmap: android.graphics.Bitmap
        try {
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            val matrizBits = writer.encode(
                conteudoQR,
                com.google.zxing.BarcodeFormat.QR_CODE,
                tamanho,
                tamanho
            )
            bitmap = android.graphics.Bitmap.createBitmap(
                tamanho, tamanho, android.graphics.Bitmap.Config.RGB_565
            )
            for (x in 0 until tamanho) {
                for (y in 0 until tamanho) {
                    bitmap.setPixel(
                        x, y,
                        if (matrizBits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
        } catch (e: Exception) {
            Log.d("QRCode", "erro ao gerar QR code: ${e.message}")
            Toast.makeText(this, "Não foi possível gerar o QR code.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageView = android.widget.ImageView(this)
        imageView.setImageBitmap(bitmap)
        imageView.setPadding(32, 32, 32, 32)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("QR Code de check-in")
            .setMessage(nomeEvento)
            .setView(imageView)
            .setPositiveButton("Fechar", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.mostrarQRCode) {
            mostrarQRCode()
        }

        if (item.itemId == R.id.exportarParticipantes) {
            exportarParticipantes()
        }

        if (item.itemId == R.id.partilharEvento) {
            partilharEvento()
        }

        if (item.itemId == R.id.adicionarCalendario) {
            adicionarAoCalendario()
        }

        if (item.itemId == R.id.signOut) {
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

