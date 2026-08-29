package com.example.app

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityGrupoBinding

class VariaveisGlobais : Application() {

    override fun onCreate() {
        super.onCreate()

        // Firebase App Check — TEM de ser inicializado o mais cedo possível,
        // antes de qualquer outro pedido ao Firebase, para que esses pedidos
        // já venham com o "bilhete de identidade" da app anexado. Em debug
        // usa o fornecedor Debug (gera um token aleatório que precisa de ser
        // registado manualmente na Firebase Console — ver
        // docs/ACOES_MANUAIS.md); em release usa o Play Integrity API
        // (verificação real de que o dispositivo/app são genuínos).
        // Documentação oficial: https://firebase.google.com/docs/app-check/android/play-integrity-provider
        val appCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            appCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            appCheck.installAppCheckProviderFactory(
                com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        // Ativa a persistência offline do Realtime Database — os dados já
        // vistos ficam guardados no dispositivo e continuam disponíveis sem
        // ligação à internet (leitura), e escritas feitas offline ficam em
        // fila e sincronizam automaticamente assim que a ligação voltar.
        // TEM de ser chamado aqui (Application.onCreate, antes de qualquer
        // Activity) e só uma vez em toda a vida do processo — chamar depois
        // de já ter havido qualquer uso do FirebaseDatabase, ou chamar mais
        // do que uma vez, lança uma exceção.
        // Documentação oficial: https://firebase.google.com/docs/database/android/offline-capabilities
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Nunca deve acontecer chamado daqui, mas mais vale a app arrancar
            // sem persistência do que rebentar no arranque por causa disto.
            android.util.Log.d("VariaveisGlobais", "falha ao ativar persistencia offline: ${e.message}")
        }

        // Aplica o tema guardado (ver Utils.kt/DefinicoesActivity) — sem
        // isto, a escolha só teria efeito enquanto a app estivesse aberta na
        // sessão em que foi mudada, e voltaria sempre a "seguir o sistema"
        // ao reabrir a app de raiz. (O idioma não precisa disto — o próprio
        // AppCompatDelegate.setApplicationLocales() já guarda e restaura a
        // escolha sozinho entre aberturas da app.)
        aplicarTema(temaGuardado())
    }

    var Evento: String = ""
    var detalhes: String = ""
    // Preenchidos sempre em conjunto com "detalhes", para DetalhesEventoActivity
    // saber em qual dos dois nós (EventosPublicos / EventosPrivados/{numero})
    // procurar o evento — necessário desde a separação dos eventos por
    // privacidade (ver docs/PLANO_DESENVOLVIMENTO.md).
    var detalhesPrivado: Boolean = false
    var detalhesNumeroGrupo: String = ""
    var entrar: String = ""
    var ver: String = ""
    var nome: String = ""
    var Month: Int = 0
    var Day: Int = 0
    var Year: Int = 0
    var MonthFim: Int = 0
    var DayFim: Int = 0
    var YearFim: Int = 0
    var Lat: Double = 0.0
    var Long: Double = 0.0
    var check: String = ""
    var Horas: String = ""
    var privado: String = ""
    var extra: String = ""
    var Associacao: String = ""
    var numSocio: Int = 0
    var numEspanha: Int = 0

    // Papel do utilizador autenticado nesta sessão.
    // Ver docs/PLANO_DESENVOLVIMENTO.md secção 3 para o desenho completo.
    var role: String = Roles.CACADOR

}

class GrupoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrupoBinding
    lateinit var gv: VariaveisGlobais
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        binding = ActivityGrupoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val semEventos = binding.tNaoEventos

        semEventos.isInvisible = true

        binding.swipeRefreshGrupo.setOnRefreshListener {
            recreate()
        }

        busca()


    }


    /**
     * Mostra todos os eventos deste grupo — públicos e privados. Um evento
     * público continua ligado ao grupo que o criou (campo "numeroGrupo"
     * gravado sempre, independentemente de "Forma"), por isso é preciso
     * consultar os dois nós separadamente desde a divisão por privacidade
     * (ver docs/PLANO_DESENVOLVIMENTO.md):
     * - EventosPublicos filtrados por numeroGrupo (só esse grupo escreve
     *   eventos públicos "seus", mas continuam visíveis a todos)
     * - EventosPrivados/{numeroGrupo} por inteiro (já limitado ao grupo
     *   pelo próprio caminho, sem precisar de filtro)
     *
     * Reescrita para fazer 2 leituras diretas (uma por nó) em vez do padrão
     * antigo — percorrer TODOS os eventos da plataforma e reler cada um
     * individualmente para verificar se pertencia a este grupo.
     */
    fun busca() {
        val semEventos = binding.tNaoEventos
        semEventos.isInvisible = true
        binding.progressGrupo.isVisible = true
        binding.progressGrupo.postDelayed({ binding.progressGrupo.isVisible = false }, 5000)
        val user = Auth.currentUser
        if (user == null) return

        val numeroGrupo = intent.getStringExtra(EXTRA_MESSAGE).toInt()
        // nome, privado
        val encontrados = java.util.Collections.synchronizedList(ArrayList<Pair<String, Boolean>>())
        var pendentes = 2

        fun concluir() {
            pendentes--
            if (pendentes == 0) {
                binding.progressGrupo.isVisible = false
                mostrarEventosDoGrupo(numeroGrupo, encontrados)
            }
        }

        mAuth.getReference("EventosPublicos")
            .orderByChild("numeroGrupo")
            .equalTo(numeroGrupo.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (eventoSnap in snapshot.children) {
                        val nome = eventoSnap.child("nome").getValue(String::class.java)
                            ?: eventoSnap.key ?: continue
                        encontrados.add(Pair(nome, false))
                    }
                    concluir()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Grupo", "erro ao ler eventos publicos: ${error.message}")
                    concluir()
                }
            })

        mAuth.getReference("EventosPrivados").child(numeroGrupo.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (eventoSnap in snapshot.children) {
                        val nome = eventoSnap.child("nome").getValue(String::class.java)
                            ?: eventoSnap.key ?: continue
                        encontrados.add(Pair(nome, true))
                    }
                    concluir()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Grupo", "erro ao ler eventos privados: ${error.message}")
                    concluir()
                }
            })
    }

    private fun mostrarEventosDoGrupo(numeroGrupo: Int, eventos: List<Pair<String, Boolean>>) {
        val semEventos = binding.tNaoEventos
        semEventos.isVisible = eventos.isEmpty()

        val nomes = eventos.map { it.first }
        val adapter = ArrayAdapter(this, R.layout.listview_item, nomes)
        val lista = binding.ListView3
        lista.adapter = adapter

        lista.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val (nome, privado) = eventos[position]
            gv.detalhes = nome
            gv.detalhesPrivado = privado
            gv.detalhesNumeroGrupo = if (privado) numeroGrupo.toString() else ""
            startActivity(Intent(this, DetalhesEventoActivity::class.java))
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

        if (item.itemId == R.id.eventosProximos) {
            startActivity(Intent(this, EventosProximosActivity::class.java))
        }

        if (item.itemId == R.id.checkInQR) {
            iniciarScanQR()
        }

        if (item.itemId == R.id.definicoes) {
            startActivity(Intent(this, DefinicoesActivity::class.java))
        }

        if (item.itemId == R.id.ajuda) {
            startActivity(Intent(this, AjudaActivity::class.java))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!processarResultadoScanQR(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
