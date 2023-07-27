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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_admissao.*
import kotlinx.android.synthetic.main.adesao_custom_view.view.*
import java.util.HashMap

class AdmissaoActivity : AppCompatActivity() {


    val mAuth = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()

    lateinit var lista: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admissao)

        lista = ListViewPendentes



        dados()
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
            val mail = mAuth.getReference("Grupos")

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

                    val t = mAuth.getReference("Users")
                    if (num == numeroGrupo.toInt()) {
                        if (admin == user) {

                            t.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    val teste = snapshot.children
                                    //valor.add(teste.toString())
                                    Log.d("adesa", "teste= $teste")
                                    //Log.d("adesa", "valor= $valor")

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
                                        lista.adapter = MyListAdapter(
                                            this@AdmissaoActivity,
                                            R.layout.listview_item_pendentes,
                                            values
                                        )

                                        lista.setOnItemClickListener { parent, _, position, _ ->

//                                        lista.onItemClickListener =
//                                            object : AdapterView.OnItemClickListener {
//                                                override fun onItemClick(
//                                                    parent: AdapterView<*>?,
//                                                    view: View?,
//                                                    position: Int,
//                                                    id: Long
//                                                ) {


                                            val elemnt = parent.getItemAtPosition(position) as Model
                                            val itemValue = lista.getItemIdAtPosition(position)
                                            Log.d(
                                                "adesa",
                                                "ffff :$elemnt"
                                            )




                                            mAuth.getReference("Users").child(elemnt.toString())
                                                .addValueEventListener(object :
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
                                                        TODO("Not yet implemented")
                                                    }
                                                })

                                            // }

                                            //}
                                        }


                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
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
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
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
        val inflateview = inflater.inflate(R.layout.adesao_custom_view, null)

        val texto = inflateview.textViewShow

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
            Toast.makeText(this, "Rejeitado", Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Aceitar") { _, _ ->


            mAuth.getReference("Grupos").child(nomeGrupo).addListenerForSingleValueEvent(
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
                        mAuth.getReference("Grupos").child(nomeGrupo).child("membros")
                            .updateChildren(update)

                        //adiciona no utilizador o a secçao dos grupos o seu numero de socio se ele for aceite
                        mAuth.getReference("Users").child(uid).child("Grupos")
                            .updateChildren(updateUser)

                        //remove o utilizador da lista de pendetes
                        mAuth.getReference("Grupos").child(nomeGrupo).child("Pendentes")
                            .child(uid)
                            .removeValue()


                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

            Toast.makeText(this, "Aceitou", Toast.LENGTH_LONG).show()
        }


        val dialog = alertDialog.create()
        dialog.show()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita_org, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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