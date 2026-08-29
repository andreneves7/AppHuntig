package com.example.app

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityAdmissaoBinding
import com.example.app.databinding.AdesaoCustomViewBinding
import java.util.HashMap

class AdmissaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdmissaoBinding

    val mAuth = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()

    lateinit var lista: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdmissaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lista = binding.ListViewPendentes
        binding.progressAdmissao.isVisible = true
        binding.progressAdmissao.postDelayed({ binding.progressAdmissao.isVisible = false }, 5000)

        binding.swipeRefreshAdmissao.setOnRefreshListener {
            recreate()
        }

        dados()
    }


    /**
     * Aprovação em lote — aceita de uma vez todos os pedidos pendentes deste
     * grupo, em vez de um a um. Pede confirmação primeiro (ação em massa,
     * mais fácil de tocar sem querer do que uma aceitação individual).
     */
    private fun confirmarAceitarTodos() {
        val numeroGrupo = intent.getStringExtra(EXTRA_MESSAGE) ?: return

        mAuth.getReference("Grupos").child(numeroGrupo).child("Pendentes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val total = snapshot.childrenCount
                    if (total == 0L) {
                        Toast.makeText(
                            this@AdmissaoActivity,
                            "Não há pedidos pendentes.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    AlertDialog.Builder(this@AdmissaoActivity)
                        .setTitle("Aceitar todos")
                        .setMessage("Tens a certeza que queres aceitar todos os $total pedidos pendentes deste grupo de uma vez?")
                        .setPositiveButton("Sim, aceitar todos") { _, _ ->
                            aceitarTodos(numeroGrupo, snapshot)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("todo_fix", "erro Firebase: ${error.message}")
                    this@AdmissaoActivity.mostrarErroLigacao()
                }
            })
    }

    private fun aceitarTodos(numeroGrupo: String, pendentesSnapshot: DataSnapshot) {
        var aceites = 0
        for (pendenteSnap in pendentesSnapshot.children) {
            val uid = pendenteSnap.key ?: continue
            val numSoc = pendenteSnap.child("numero socio").getValue(String::class.java)
                ?: pendenteSnap.child("numero socio").getValue(Int::class.java)?.toString()
                ?: continue

            val updateMembros = HashMap<String, Any>()
            updateMembros[numSoc] = uid
            mAuth.getReference("Grupos").child(numeroGrupo).child("membros")
                .updateChildren(updateMembros)

            val updateUser = HashMap<String, Any>()
            updateUser[numeroGrupo] = numSoc
            mAuth.getReference("Users").child(uid).child("Grupos")
                .updateChildren(updateUser)

            mAuth.getReference("Grupos").child(numeroGrupo).child("Pendentes")
                .child(uid).removeValue()

            aceites++
        }

        Toast.makeText(this, "$aceites pedidos aceites.", Toast.LENGTH_LONG).show()
        // recreate() em vez de chamar dados() outra vez — essa função anexa um
        // ChildEventListener a "Grupos", e chamá-la de novo sem desanexar o
        // anterior acumularia listeners duplicados (mesmo cuidado já usado no
        // pull-to-refresh de HomeActivity).
        recreate()
    }

    private fun dados() {
        val user = auth.currentUser?.uid
        val num = intent.getStringExtra(EXTRA_MESSAGE)?.toInt()
        var n: String
        var c: Int
        var s: Int
        var uid: String
        var socio = ""

        if (user != null) {
            // Antes: mAuth.getReference("Grupos") sem filtro — descarregava TODOS
            // os grupos da plataforma só para descartar no cliente os que não são
            // desta organização. Agora: filtrado no servidor por "admin" (sempre
            // um uid do Firebase Auth, sem ambiguidade de tipo). A comparação por
            // "Numero" mantém-se no cliente, porque esse campo nunca é escrito
            // pela app (só manualmente na consola), e não há garantia de que
            // esteja sempre gravado como número — uma query tipada errada
            // falharia silenciosamente (zero resultados), por isso não arrisquei.
            val mail = mAuth.getReference("Grupos").orderByChild("admin").equalTo(user)

            val values = ArrayList<Model>()
            val valor = ArrayList<String>()

            val j = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    //val g = dataSnapshot.child("nome").getValue().toString()
                    val admin = dataSnapshot.child("admin").value.toString()
                    val numeroGrupo = dataSnapshot.child("Numero").value.toString()
                    val nameGrupo = dataSnapshot.child("nome").value.toString()
                    Log.d("adesa", "numero= $numeroGrupo")

                    Log.d(
                        "VerGrupo2",
                        "$user"
                    )


//                    val m = mAuth.getReference("Grupos").child(g)

//                    Log.d(
//                        "VerGrupo2",
//                        " ${m}"
//                    )

                    val t = mAuth.getReference("PerfisPublicos")
                    if (num == numeroGrupo.toInt()) {
                        if (admin == user) {

                            // FIX: usa addListenerForSingleValueEvent em vez de addValueEventListener.
                            // O listener anterior era contínuo (disparava sempre que QUALQUER dado em
                            // "Users" mudasse) e reconstruía a lista sem a limpar primeiro, pelo que
                            // cada novo evento ADICIONAVA duplicados por cima dos já existentes em
                            // "values", em vez de a substituir. Isto fazia a lista de pendentes
                            // corromper-se rapidamente (entradas repetidas, cliques a apontarem para
                            // a pessoa errada) e dava a aparência de "só aceitar 1" corretamente.
                            t.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    binding.progressAdmissao.isVisible = false

                                    val teste = snapshot.children
                                    Log.d("adesa", "teste= $teste")

                                    // FIX: limpa a lista antes de a repovoar, para nunca acumular
                                    // duplicados entre chamadas.
                                    values.clear()

                                    for (i in teste) {
                                        Log.d("adesa", "t= ${i.key}")

                                        val existe =
                                            dataSnapshot.child("Pendentes")
                                                .value.toString()

                                        if (existe.contains(i.key.toString())) {

                                            val nome =
                                                snapshot.child("${i.key}").child("name")
                                                    .value.toString()
                                            Log.d("adesa", "nome= $nome")

                                            val carta =
                                                snapshot.child("${i.key}").child("Carta Caçadore")
                                                    .value.toString()
                                            Log.d("adesa", "carta= $carta")


                                            socio =
                                                dataSnapshot.child("Pendentes").child("${i.key}")
                                                    .child("numero socio").value
                                                    .toString()

                                            Log.d("adesa", "g= $socio")



                                            values.add(
                                                Model(
                                                    nome, carta.toInt(), socio.toInt(),
                                                    i.key.toString()
                                                )
                                            )


                                            uid = i.key.toString()
                                        }
                                    }

                                    // FIX: adapter e click listener saem de dentro do ciclo `for` e
                                    // passam a ser definidos UMA só vez, depois da lista completa
                                    // estar montada — antes eram recriados a cada iteração.
                                    lista.adapter = MyListAdapter(
                                        this@AdmissaoActivity,
                                        R.layout.listview_item_pendentes,
                                        values
                                    )

                                    lista.setOnItemClickListener { parent, _, position, _ ->

                                        val elemnt = parent.getItemAtPosition(position) as Model
                                        Log.d(
                                            "adesa",
                                            "ffff :$elemnt"
                                        )

                                        mAuth.getReference("PerfisPublicos").child(elemnt.toString())
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {

                                                    val name =
                                                        snapshot.child("name")
                                                            .value.toString()


                                                    val cartacc =
                                                        snapshot.child("Carta Caçadore")
                                                            .value.toString()


                                                    val numSocio = snapshot.child("Grupos")
                                                        .child(
                                                            num.toString()
                                                        ).child("Socio")
                                                        .value.toString()

                                                    val refUser = snapshot.child("uid")
                                                        .value.toString()

                                                    n = name
                                                    c = cartacc.toInt()
                                                    s = socio.toInt()


                                                    uid = refUser
                                                    Log.d(
                                                        "adesa",
                                                        "ffff :${nameGrupo}"
                                                    )

                                                    open(n, c, s, nameGrupo, uid, numeroGrupo)
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.d("adesa", "fail dados utilizador")
                                                }
                                            })
                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    binding.progressAdmissao.isVisible = false
                                    Log.d("adesa", "fail dados users: ${error.message}")
                                    this@AdmissaoActivity.mostrarErroLigacao()
                                }


                            })

                        }
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    startActivity(
                        Intent(
                            this@AdmissaoActivity,
                            CriarOrgEventoActivity::class.java
                        ).apply {
                            putExtra(
                                EXTRA_MESSAGE,
                                num.toString()
                            )
                        })
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Não precisamos de reagir a remoções de Grupos aqui; apenas registamos.
                    Log.d("adesa", "grupo removido: ${snapshot.key}")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Não aplicável a esta listagem (não depende de ordenação).
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("adesa", "fail dados grupos: ${error.message}")
                }
            }
            mail.addChildEventListener(j)
        }

    }

    fun open(
        name: String,
        numCC: Int,
        numSoc: Int,
        nomeGrupo: String,
        uid: String,
        numeroGrupo: String
    ) {
        val inflater = layoutInflater
        val dialogBinding = AdesaoCustomViewBinding.inflate(inflater)
        val inflateview = dialogBinding.root

        val texto = dialogBinding.textViewShow

        var num: Int
        val valu = ArrayList<String>()

//        texto.text =
//            "Nome: " + nome + "\n" + "Nº Carta Caçador: " + numeroCC + "\n" + "Nº Socio: " + numeroSocio
        texto.text = getString(R.string.adesao_custom_textViewShow, name, numCC, numSoc)


        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Socio Pendente")
        alertDialog.setView(inflateview)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Rejeitar") { _, _ ->
            // Ação destrutiva desde a correção do bug (antes não fazia nada de
            // facto) — pede confirmação extra antes de remover o pedido, para
            // não rejeitar alguém sem querer com um toque sem intenção.
            AlertDialog.Builder(this)
                .setTitle("Confirmar rejeição")
                .setMessage("Tens a certeza que queres rejeitar o pedido de $name? A pessoa vai ter de pedir adesão outra vez se mudares de ideias.")
                .setPositiveButton("Sim, rejeitar") { _, _ ->
                    mAuth.getReference("Grupos").child(numeroGrupo).child("Pendentes")
                        .child(uid).removeValue()
                    Toast.makeText(this, this.getString(R.string.msg_rejeitado), Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        alertDialog.setPositiveButton("Aceitar") { _, _ ->


            mAuth.getReference("Grupos").child(numeroGrupo).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val a = dataSnapshot.child("membros").value.toString()
                        valu.add(a)

                        val b = dataSnapshot.child("membros").childrenCount


//                       var c = a.split('=') as ArrayList<String>
//
//                        Log.d(
//                            "adesa",
//                            "DocumentSnapshot data: ${a.split('=')}"
//                        )
//
//                        Log.d(
//                            "adesa",
//                            "DocumentSnapshot data: ${c}"
//                        )
//
//
//                        Log.d(
//                            "adesa",
//                            "DocumentSnapshot data: ${c[0]}"
//                        )

//                        Log.d(
//                            "adesa",
//                            "DocumentSnapshot data: ${c[2]}"
//                        )

                        Log.d(
                            "adesa",
                            "DocumentSnapshot data: $b"
                        )

                        num = valu.size



                        num += 1
                        Log.d(
                            "adesa",
                            "DocumentSnapshot data: $num"
                        )
                        Log.d(
                            "adesa",
                            "DocumentSnapshot data: $numSoc"
                        )

                        val update = HashMap<String, Any>()
                        update["$numSoc"] = uid

                        val updateUser = HashMap<String, Any>()
                        updateUser[numeroGrupo] = numSoc

                        //adiciona ao grupo nos membros se ele for aceite
                        mAuth.getReference("Grupos").child(numeroGrupo).child("membros")
                            .updateChildren(update)

                        //adiciona no utilizador o a secçao dos grupos o seu numero de socio se ele for aceite
                        mAuth.getReference("Users").child(uid).child("Grupos")
                            .updateChildren(updateUser)

                        //remove o utilizador da lista de pendetes
                        mAuth.getReference("Grupos").child(numeroGrupo).child("Pendentes")
                            .child(uid)
                            .removeValue()


                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("adesa", "fail ao aceitar socio: ${error.message}")
                        Toast.makeText(this@AdmissaoActivity, this@AdmissaoActivity.getString(R.string.msg_erro_ao_aceitar_socio_tenta_novamente), Toast.LENGTH_LONG
                        ).show()
                    }
                })

            Toast.makeText(this, this.getString(R.string.msg_aceitou), Toast.LENGTH_LONG).show()
        }


        val dialog = alertDialog.create()
        dialog.show()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_admissao, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.aceitarTodos) {
            confirmarAceitarTodos()
        }

        if (item.itemId == R.id.signOut2) {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }



        if (item.itemId == R.id.grupo2) {

            startActivity(Intent(this, OrgActivity::class.java))
        }

//        if (item.itemId == R.id.pendente) {
//
//            startActivity(Intent(this, AdmissaoActivity::class.java))
//        }


        return super.onOptionsItemSelected(item)
    }

}