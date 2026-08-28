package com.example.app

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
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityHomeBinding
import org.intellij.lang.annotations.JdkConstants
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()
    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = application as VariaveisGlobais
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val lista = ListView4

        //val escolherFiltros = filtros

        eventos()

        // Puxar a lista para baixo recarrega o ecrã por completo (recreate())
        // em vez de tentar voltar a chamar eventos() sozinha — essa função
        // anexa vários listeners aninhados ao Firebase, e chamá-la outra vez
        // sem desanexar os anteriores acumularia listeners duplicados a cada
        // "puxar", com entradas repetidas na lista com o tempo. recreate()
        // destrói e reconstrói o ecrã do zero, evitando esse risco por
        // completo — mais simples e seguro do que alterar a lógica interna
        // de eventos() sem conseguir testar.
        binding.swipeRefreshHome.setOnRefreshListener {
            recreate()
        }
        /*escolherFiltros.setOnClickListener {
            lista.setAdapter(null);
            showFiltros()
        }*/


    }

    fun eventos() {
        val semEventos = binding.NaoEventos

        val lista = binding.ListViewHome
        val pesquisa = binding.SearchEvento
        val filtro = intent.getStringExtra(EXTRA_MESSAGE)
        val values = ArrayList<String>()
        // Antes: mAuth.getReference("Eventos") sem qualquer ordenação/limite — o
        // listener descarregava TODOS os eventos existentes, sempre, sem exceção.
        // Agora: ordenado pelo campo "dataFimTimestamp" (ver MapsActivity.kt) e
        // limitado aos 200 que terminam mais cedo — eventos muito antigos (sem
        // este campo, ou com data de fim já muito passada) ficam de fora primeiro
        // à medida que a base de dados cresce. A lógica de filtragem por
        // data/tipo/formato abaixo mantém-se exatamente igual — isto só limita
        // quantos nós o listener descarrega, não muda o que é mostrado dentro
        // desse limite.
        // MIGRAÇÃO (ver docs/PLANO_DESENVOLVIMENTO.md): lê agora EventosPublicos
        // em vez do antigo nó único "Eventos". A query dos eventos PRIVADOS já
        // não pode ser construída aqui — precisa do número de cada grupo do
        // utilizador, só conhecido mais abaixo — por isso passou a ser montada
        // individualmente, por grupo, no sítio onde já era usada.
        val ListaEventosPublic = mAuth.getReference("EventosPublicos")
            .orderByChild("dataFimTimestamp").limitToFirst(200)

        // Mostra o indicador de carregamento enquanto esperamos pela resposta do
        // Firebase. Como ChildEventListener não avisa quando "acabou de carregar
        // tudo o que existe" (ao contrário de addListenerForSingleValueEvent),
        // escondemo-lo assim que o primeiro evento chegar (onChildAdded) ou, se a
        // lista estiver mesmo vazia, ao fim de 5 segundos — para nunca ficar preso
        // a girar indefinidamente.
        binding.progressHome.isVisible = true
        binding.progressHome.postDelayed({ binding.progressHome.isVisible = false }, 5000)

        if (filtro != null) {
            val public = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    binding.progressHome.isVisible = false

                    val tipo = dataSnapshot.child("Tipo").getValue().toString()

                    if (tipo == filtro || filtro == "tudo") {

                        semEventos.isVisible = false
                        val anoAtual =
                            Calendar.getInstance().get(Calendar.YEAR)
                        val mesAtual =
                            Calendar.getInstance().get(Calendar.MONTH) + 1
                        val diaAtual =
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                        val ano = dataSnapshot.child("anoFim").getValue().toString().toInt()
                        val mes = dataSnapshot.child("mesFim").getValue().toString().toInt()
                        val dia = dataSnapshot.child("diaFim").getValue().toString().toInt()



                        if (anoAtual < ano) {

                            val f = dataSnapshot.child("Forma").getValue().toString()
                            if (f == "publico") {
                                Log.d(
                                    "home2",
                                    "${
                                        dataSnapshot.child("nome").getValue()
                                            .toString()
                                    },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
                                )
                                values.add(dataSnapshot.child("nome").getValue().toString())
                            }

                        } else if (anoAtual == ano) {

                            if (mesAtual < mes) {
                                val f = dataSnapshot.child("Forma").getValue().toString()
                                if (f == "publico") {
                                    Log.d(
                                        "home2",
                                        "${
                                            dataSnapshot.child("nome").getValue()
                                                .toString()
                                        },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
                                    )
                                    values.add(dataSnapshot.child("nome").getValue().toString())
                                }
                            } else if (mesAtual == mes) {
                                if (diaAtual <= dia) {
                                    val f = dataSnapshot.child("Forma").getValue().toString()
                                    if (f == "publico") {
                                        Log.d(
                                            "home2",
                                            "${
                                                dataSnapshot.child("nome").getValue()
                                                    .toString()
                                            },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
                                        )
                                        values.add(dataSnapshot.child("nome").getValue().toString())
                                    }
                                }
                            }

                        }


//                                    val f = evento.get("Forma")
//                                    if (f == "publico") {
//                                        Log.d(
//                                            "home2",
//                                            "${
//                                                evento.get("nome")
//                                                    .toString()
//                                            },$anoAtual ,$mesAtual,$diaAtual, $ano, $mes, $dia"
//                                        )
//                                        values.add(evento.get("nome").toString())
//                                    }


                    }

                    Log.d("home5", "$values")


                    val adapter =
                        ArrayAdapter(this@HomeActivity, R.layout.listview_item, values)

                    lista.adapter = adapter



                    lista.onItemClickListener =
                        object : AdapterView.OnItemClickListener {


                            override fun onItemClick(
                                parent: AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {


                                val itemValue =
                                    (lista.getItemAtPosition(position) as String)
                                        .removePrefix("🔒 ")
                                Log.d("home", "grupoID to search: $itemValue")
                                gv.detalhes = itemValue
                                // Este bloco é exclusivo do ramo público (ver "public" acima)
                                // — nunca mostra eventos privados, por isso é seguro fixar
                                // aqui sem mais verificação.
                                gv.detalhesPrivado = false
//                            val uid = Auth.currentUser?.uid
                                val eventoclick2 =
                                    mAuth.getReference("EventosPublicos").child(itemValue)
                                eventoclick2.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        startActivity(
                                            Intent(
                                                view.context,
                                                DetalhesEventoActivity::class.java
                                            )
                                        )


                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("todo_fix", "erro Firebase: ${error.message}")
                                        this@HomeActivity.mostrarErroLigacao()
                                    }
                                })


//                                            Toast.makeText(
//                                                applicationContext,
//                                                "Position :$position\nItem Value : $itemValue",
//                                                Toast.LENGTH_LONG
//                                            ).show()


                            }

                        }
//                var x = 0
//                for (evento in result) {
//
//                    x += 1
//                }
//                if (x > 0) {
//                    semEventos.isVisible = false
//                } else {
//
//                    semEventos.isVisible = true
//                    Toast.makeText(
//                        applicationContext,
//                        "Sem eventos disponiveis", Toast.LENGTH_LONG
//                    ).show()
//                }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressHome.isVisible = false
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@HomeActivity.mostrarErroLigacao()
                }

            }
            ListaEventosPublic.addChildEventListener(public)

            val gruposMemmbros = mAuth.getReference("Grupos")
            Log.d(
                "home75",
                "gr : ${
                    gruposMemmbros
                }"
            )

            val teste = ArrayList<String>()
            val gm = object : ChildEventListener {
                override fun onChildAdded(
                    dataSnapshot: DataSnapshot,
                    previousChildName: String?
                ) {

                    val uid = Auth.currentUser?.uid

                    val g = dataSnapshot.child("nome").getValue().toString()
                    // MIGRAÇÃO: Grupos passou a ser indexado por "Numero" em vez
                    // do nome (ver docs/PLANO_DESENVOLVIMENTO.md).
                    val numero = dataSnapshot.child("Numero").getValue().toString()
                    Log.d(
                        "home75",
                        "g : ${
                            g
                        }"
                    )

                    teste.add(g)

                    Log.d(
                        "home75",
                        "teste: ${
                            teste
                        }"
                    )

                    val m = mAuth.getReference("Grupos").child(numero)


                    val t = mAuth.getReference("Grupos").child(numero).child("membros")

                    Log.d(
                        "home75",
                        "m : ${
                            m
                        }"
                    )

                    val f = object : ChildEventListener {
                        override fun onChildAdded(
                            dataSnapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val j = dataSnapshot.getValue().toString()


                            Log.d(
                                "home75",
                                "j : ${
                                    j
                                }"
                            )

                            val fazParte = ArrayList<String>()

                            fazParte.add(j)

                            Log.d(
                                "home75",
                                "f : ${
                                    fazParte
                                }"
                            )

                            if (fazParte.contains(uid)) {


                                m.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {


                                        val n =
                                            snapshot.child("Numero").getValue().toString().toInt()

                                        // MIGRAÇÃO (ver docs/PLANO_DESENVOLVIMENTO.md): lê
                                        // EventosPrivados/{n} — já limitado a este grupo pelo
                                        // próprio caminho, construído aqui porque "n" só fica
                                        // conhecido neste ponto (um grupo de cada vez).
                                        val listaEventosPrivadosDesteGrupo =
                                            mAuth.getReference("EventosPrivados").child(n.toString())

                                        val private = object : ChildEventListener {
                                            override fun onChildAdded(
                                                dataSnapshot: DataSnapshot,
                                                previousChildName: String?
                                            ) {


                                                val l = dataSnapshot.child("numeroGrupo").getValue()
                                                    .toString().toInt()

                                                Log.d(
                                                    "home75",
                                                    "l :${
                                                        l
                                                    }"
                                                )

                                                val ano =
                                                    dataSnapshot.child("anoFim").getValue()
                                                        .toString()
                                                        .toInt()
                                                val mes =
                                                    dataSnapshot.child("mesFim").getValue()
                                                        .toString()
                                                        .toInt()
                                                val dia =
                                                    dataSnapshot.child("diaFim").getValue()
                                                        .toString()
                                                        .toInt()
                                                val tipo =
                                                    dataSnapshot.child("Tipo").getValue().toString()

                                                val f =
                                                    dataSnapshot.child("Forma").getValue()
                                                        .toString()


                                                val nome = dataSnapshot.child("nome").getValue()
                                                    .toString()


//                                            val fodasse = m.child(n)


                                                if (n == l) {

                                                    Log.d(
                                                        "home75",
                                                        "n :${
                                                            n
                                                        }"
                                                    )



                                                    if (tipo == filtro || filtro == "tudo") {


                                                        semEventos.isVisible = false
                                                        val anoAtual =
                                                            Calendar.getInstance()
                                                                .get(Calendar.YEAR)
                                                        val mesAtual =
                                                            Calendar.getInstance()
                                                                .get(Calendar.MONTH) + 1
                                                        val diaAtual =
                                                            Calendar.getInstance()
                                                                .get(Calendar.DAY_OF_MONTH)


                                                        // PROBLEMA NA VERIFICAÇAO DO DIA
                                                        if (anoAtual < ano) {

                                                            if (f == "privado") {
                                                                values.add(
                                                                    "🔒 $nome"
                                                                )

                                                            }

                                                        } else if (anoAtual == ano) {
                                                            if (mesAtual < mes) {
//                                                    val f =
//                                                        dataSnapshot.child("Forma").getValue()
//                                                            .toString()
                                                                if (f == "privado") {
                                                                    values.add(
                                                                        "🔒 $nome"
                                                                    )

                                                                }
                                                            } else if (mesAtual == mes) {
                                                                if (diaAtual <= dia) {

//                                                        val f =
//                                                            dataSnapshot.child("Forma").getValue()
//                                                                .toString()
                                                                    if (f == "privado") {
                                                                        Log.d(
                                                                            "home75",
                                                                            "nome :${
                                                                                nome
                                                                            }"
                                                                        )
                                                                        values.add(
                                                                            "🔒 $nome"
                                                                        )
                                                                        Log.d(
                                                                            "home75",
                                                                            "v :${
                                                                                values
                                                                            }"
                                                                        )

                                                                    }
                                                                }
                                                            }

                                                        }

                                                    }


                                                }//


                                                Log.d("home75", "$values")


                                                val adapter =
                                                    ArrayAdapter(
                                                        this@HomeActivity,
                                                        R.layout.listview_item,
                                                        values
                                                    )

                                                lista.adapter = adapter
                                                pesquisa.setOnQueryTextListener(object :
                                                    SearchView.OnQueryTextListener {
                                                    override fun onQueryTextSubmit(query: String): Boolean {

                                                        return false
                                                    }

                                                    override fun onQueryTextChange(newText: String): Boolean {

                                                        adapter.filter.filter(newText)
                                                        return false
                                                    }
                                                })

                                                lista.onItemClickListener =
                                                    object : AdapterView.OnItemClickListener {


                                                        override fun onItemClick(
                                                            parent: AdapterView<*>,
                                                            view: View,
                                                            position: Int,
                                                            id: Long
                                                        ) {


                                                            val itemValue =
                                                                (lista.getItemAtPosition(position) as String)
                                                                    .removePrefix("🔒 ")
                                                            Log.d(
                                                                "home44",
                                                                "grupoID to search: $itemValue"
                                                            )
                                                            gv.detalhes = itemValue
                                                            // Este bloco é exclusivo do ramo privado
                                                            // deste grupo (ver "private"/"n" acima) —
                                                            // seguro fixar aqui sem mais verificação.
                                                            gv.detalhesPrivado = true
                                                            gv.detalhesNumeroGrupo = n.toString()
//                                                        val uid = Auth.currentUser?.uid
                                                            val eventoClick =
                                                                mAuth.getReference("EventosPrivados")
                                                                    .child(n.toString())
                                                                    .child(itemValue)
                                                            eventoClick.addValueEventListener(object :
                                                                ValueEventListener {
                                                                override fun onDataChange(snapshot: DataSnapshot) {

                                                                    startActivity(
                                                                        Intent(
                                                                            view.context,
                                                                            DetalhesEventoActivity::class.java
                                                                        )
                                                                    )


                                                                }

                                                                override fun onCancelled(error: DatabaseError) {
                                                                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                                                                    this@HomeActivity.mostrarErroLigacao()
                                                                }
                                                            })


//                                            Toast.makeText(
//                                                applicationContext,
//                                                "Position :$position\nItem Value : $itemValue",
//                                                Toast.LENGTH_LONG
//                                            ).show()


                                                        }

                                                    }


                                            }

                                            override fun onChildChanged(
                                                snapshot: DataSnapshot,
                                                previousChildName: String?
                                            ) {
                                                startActivity(
                                                    Intent(
                                                        this@HomeActivity,
                                                        HomeActivity::class.java
                                                    )
                                                )
                                            }

                                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                                Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                                            }

                                            override fun onChildMoved(
                                                snapshot: DataSnapshot,
                                                previousChildName: String?
                                            ) {
                                                Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Log.d("todo_fix", "erro Firebase: ${error.message}")
                                                this@HomeActivity.mostrarErroLigacao()
                                            }

//                            var x = 0
//                            for (evento in result) {
//
//                                x += 1
//                            }
//                            if (x > 0) {
//                                semEventos.isVisible = false
//                            } else {
//
//                                semEventos.isVisible = true
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Sem eventos disponiveis", Toast.LENGTH_LONG
//                                ).show()
//                            }
//
//                        }
                                        }
//
//

                                        listaEventosPrivadosDesteGrupo.addChildEventListener(private)

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.d("todo_fix", "erro Firebase: ${error.message}")
                                        this@HomeActivity.mostrarErroLigacao()
                                    }


                                })


                            }//
                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {
                            Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.d("todo_fix", "erro Firebase: ${error.message}")
                            this@HomeActivity.mostrarErroLigacao()
                        }
                    }
                    t.addChildEventListener(f)

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("todo_fix", "Evento Firebase ignorado propositadamente (sem logica necessaria)")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@HomeActivity.mostrarErroLigacao()
                }


            }
            gruposMemmbros.addChildEventListener(gm)


            // filtros de pesquisa

            // Bloco de codigo morto removido (filtros antigos via Firestore, nunca ativo — ver docs/PLANO_DESENVOLVIMENTO.md)
        }
        else{
            val marca = 1
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
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
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //startActivity(Intent (this, MainActivity :: class.java ))
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.Lis) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this, VerGrupoActivity::class.java))
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
                putExtra(EXTRA_MESSAGE, marca)
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
