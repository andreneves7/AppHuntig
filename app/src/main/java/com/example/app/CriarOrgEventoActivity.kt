package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityCriarOrgEventoBinding

class CriarOrgEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarOrgEventoBinding
    lateinit var gv: VariaveisGlobais
    val Auth = FirebaseAuth.getInstance()

    val mAuth = FirebaseDatabase.getInstance()
    var numero = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        binding = ActivityCriarOrgEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val evento = binding.bEvento
        val soc = binding.bSocios

        binding.swipeRefreshCriarOrgEvento.setOnRefreshListener {
            recreate()
        }

        busca()

        evento.setOnClickListener {

            Log.d("Numero", "ola = $numero")
            startActivity(Intent(this, EventoActivity::class.java).apply {
                putExtra(
                    EXTRA_MESSAGE,
                    numero.toString()
                )
            })
        }

       soc.setOnClickListener {

            Log.d("Numero", "ola = $numero")
            startActivity(Intent(this, AdmissaoActivity::class.java).apply {
                putExtra(
                    EXTRA_MESSAGE,
                    numero.toString()
                )
            })
        }

    }


    /**
     * Mostra todos os eventos deste grupo — públicos e privados. Mesma
     * lógica e mesma razão de existir das duas leituras já usada em
     * GrupoActivity.busca() (ver esse ficheiro para a explicação completa):
     * um evento público continua ligado ao grupo que o criou, por isso é
     * preciso consultar EventosPublicos (filtrados por numeroGrupo) E
     * EventosPrivados/{numeroGrupo} separadamente.
     *
     * "numero" (variável da classe, usada pelos botões "Criar Evento" e
     * "Sócios") passa a ser definida logo aqui, diretamente do valor já
     * confiável vindo do Intent — antes esperava-se por uma "confirmação"
     * assíncrona via Firebase que não acrescentava nada (o valor não podia
     * ser diferente do que já vinha no Intent).
     */
    fun busca() {
        val semEventos = binding.tNaoEventos2
        semEventos.isInvisible = true
        val user = Auth.currentUser
        if (user == null) return

        val t = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE).toInt()
        numero = t

        val encontrados = java.util.Collections.synchronizedList(ArrayList<Pair<String, Boolean>>())
        var pendentes = 2

        fun concluir() {
            pendentes--
            if (pendentes == 0) {
                mostrarEventosDoGrupo(encontrados)
            }
        }

        mAuth.getReference("EventosPublicos")
            .orderByChild("numeroGrupo")
            .equalTo(t.toDouble())
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

        mAuth.getReference("EventosPrivados").child(t.toString())
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

    private fun mostrarEventosDoGrupo(eventos: List<Pair<String, Boolean>>) {
        val semEventos = binding.tNaoEventos2
        semEventos.isVisible = eventos.isEmpty()

        val nomes = eventos.map { it.first }
        val adapter = ArrayAdapter(this, R.layout.listview_item, nomes)
        val lista = binding.ListView4
        lista.adapter = adapter

        lista.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val (nome, privado) = eventos[position]
            gv.detalhes = nome
            gv.detalhesPrivado = privado
            gv.detalhesNumeroGrupo = if (privado) numero.toString() else ""
            startActivity(Intent(this, DetalhesEventoActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita_org, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.signOut2) {
            Auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }



        if (item.itemId == R.id.grupo2) {

            startActivity(Intent(this, OrgActivity::class.java))
        }

        if (item.itemId == R.id.lista) {

            startActivity(Intent(this, ListaSociosOrgActivity::class.java).apply {
                putExtra(
                    EXTRA_MESSAGE,
                    numero.toString()
                )
            })
        }


        return super.onOptionsItemSelected(item)
    }
}