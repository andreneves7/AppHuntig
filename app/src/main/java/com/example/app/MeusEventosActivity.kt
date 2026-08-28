package com.example.app

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.app.databinding.ActivityMeusEventosBinding

/**
 * Ecrã inicial personalizado — mostra primeiro os eventos dos grupos a que o
 * utilizador já pertence, antes da lista geral de todos os eventos
 * (FiltrosActivity/HomeActivity).
 *
 * Construído como ecrã novo e independente, sem tocar na lógica já muito
 * complexa e frágil de HomeActivity (ver docs/PLANO_DESENVOLVIMENTO.md) —
 * mesma abordagem já usada em EventosProximosActivity.
 *
 * Passa a ser o destino inicial depois do login para caçadores (ver
 * LoginActivity.kt e VerificarLoginActivity.kt), com um botão para quem
 * quiser ver a lista completa de sempre.
 */
class MeusEventosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMeusEventosBinding
    val mAuth = FirebaseDatabase.getInstance()
    val Auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeusEventosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bVerTodosEventos.setOnClickListener {
            val marca = 0
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }

        binding.swipeRefreshMeusEventos.setOnRefreshListener {
            carregarMeusEventos()
        }

        carregarMeusEventos()
    }

    private fun carregarMeusEventos() {
        val uid = Auth.currentUser?.uid ?: return

        binding.progressMeusEventos.isVisible = true
        binding.progressMeusEventos.postDelayed(
            { binding.progressMeusEventos.isVisible = false; binding.swipeRefreshMeusEventos.isRefreshing = false }, 5000
        )

        // Primeiro descobre a que grupos (números) o utilizador pertence.
        mAuth.getReference("Users").child(uid).child("Grupos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(gruposSnapshot: DataSnapshot) {
                    val numerosGrupos = gruposSnapshot.children.mapNotNull { it.key }

                    if (numerosGrupos.isEmpty()) {
                        binding.progressMeusEventos.isVisible = false
                        binding.swipeRefreshMeusEventos.isRefreshing = false
                        binding.tSemMeusEventos.isVisible = true
                        return
                    }

                    carregarEventosDosGrupos(numerosGrupos)
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressMeusEventos.isVisible = false
                    binding.swipeRefreshMeusEventos.isRefreshing = false
                    Log.d("MeusEventos", "erro Firebase: ${error.message}")
                    this@MeusEventosActivity.mostrarErroLigacao()
                }
            })
    }

    /**
     * Um utilizador normalmente pertence a poucos grupos (1-3), por isso uma
     * leitura separada por grupo é seguro e eficiente aqui. Lê
     * EventosPrivados/{numeroGrupo} diretamente — desde a separação por
     * privacidade (ver docs/PLANO_DESENVOLVIMENTO.md), já não é preciso
     * nenhuma query com orderByChild/equalTo nem conversão de tipo, o
     * número do grupo já é o próprio caminho.
     */
    private fun carregarEventosDosGrupos(numerosGrupos: List<String>) {
        val agora = System.currentTimeMillis().toDouble()
        // nome, dataFimTimestamp, numeroGrupo — o número do grupo é
        // necessário para saber onde procurar o evento outra vez, ao
        // navegar para os detalhes.
        val eventosEncontrados = ArrayList<Triple<String, Long, String>>()
        var queriesPendentes = numerosGrupos.size

        for (numeroGrupoStr in numerosGrupos) {
            mAuth.getReference("EventosPrivados").child(numeroGrupoStr)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (eventoSnap in snapshot.children) {
                            val dataFim = eventoSnap.child("dataFimTimestamp")
                                .getValue(Long::class.java) ?: continue
                            if (dataFim < agora) continue // já terminou

                            val nome = eventoSnap.child("nome").getValue(String::class.java)
                                ?: eventoSnap.key ?: continue
                            eventosEncontrados.add(Triple(nome, dataFim, numeroGrupoStr))
                        }

                        queriesPendentes--
                        if (queriesPendentes == 0) {
                            mostrarResultado(eventosEncontrados)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        queriesPendentes--
                        Log.d("MeusEventos", "erro numa query de grupo: ${error.message}")
                        if (queriesPendentes == 0) {
                            mostrarResultado(eventosEncontrados)
                        }
                    }
                })
        }

        if (queriesPendentes == 0) {
            // Nenhum grupo tinha eventos.
            binding.progressMeusEventos.isVisible = false
            binding.swipeRefreshMeusEventos.isRefreshing = false
            binding.tSemMeusEventos.isVisible = true
        }
    }

    private fun mostrarResultado(eventos: List<Triple<String, Long, String>>) {
        binding.progressMeusEventos.isVisible = false
        binding.swipeRefreshMeusEventos.isRefreshing = false

        val ordenados = eventos.sortedBy { it.second }
        binding.tSemMeusEventos.isVisible = ordenados.isEmpty()

        val nomes = ordenados.map { it.first }
        val adapter = ArrayAdapter(this, R.layout.listview_item, nomes)
        binding.ListViewMeusEventos.adapter = adapter

        binding.ListViewMeusEventos.setOnItemClickListener { _, _, position, _ ->
            val gv = application as VariaveisGlobais
            gv.detalhes = ordenados[position].first
            gv.detalhesPrivado = true
            gv.detalhesNumeroGrupo = ordenados[position].third
            startActivity(Intent(this, DetalhesEventoActivity::class.java))
        }
    }
}
