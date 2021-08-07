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
    val Auth = FirebaseAuth.getInstance()

    lateinit var lista: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admissao)

        lista = ListViewPendentes



        dados()
    }


    fun dados() {
        val user = Auth.currentUser?.uid
        val num = intent.getStringExtra(EXTRA_MESSAGE).toInt()
        var n = ""
        var c = 0
        var s = 0
        var uid = ""
        var socio = ""

        if (user != null) {
            val mail = mAuth.getReference("Grupos")

            var values = ArrayList<Model>()
            val valor = ArrayList<String>()

            val j = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                    //val g = dataSnapshot.child("nome").getValue().toString()
                    val admin = dataSnapshot.child("admin").getValue().toString()
                    val numeroGrupo = dataSnapshot.child("Numero").getValue().toString()
                    val nameGrupo = dataSnapshot.child("nome").getValue().toString()
                    Log.d("adesa", "numero= $numeroGrupo")

                    Log.d(
                        "VerGrupo2",
                        "${user}"
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
                                                .getValue().toString()

                                        if (existe.contains(i.key.toString())) {

                                            val nome =
                                                snapshot.child("${i.key}").child("name")
                                                    .getValue().toString()
                                            Log.d("adesa", "nome= $nome")

                                            val carta =
                                                snapshot.child("${i.key}").child("Carta Caçadore")
                                                    .getValue().toString()
                                            Log.d("adesa", "carta= $carta")


                                            socio =
                                                dataSnapshot.child("Pendentes").child("${i.key}")
                                                    .child("numero socio").getValue()
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

                                        lista.setOnItemClickListener { parent, view, position, id ->

//                                        lista.onItemClickListener =
//                                            object : AdapterView.OnItemClickListener {
//                                                override fun onItemClick(
//                                                    parent: AdapterView<*>?,
//                                                    view: View?,
//                                                    position: Int,
//                                                    id: Long
//                                                ) {


                                            var elemnt = parent.getItemAtPosition(position) as Model
                                            val itemValue = lista.getItemIdAtPosition(position)
                                            Log.d(
                                                "adesa",
                                                "ffff :${elemnt.toString()}"
                                            )




                                            mAuth.getReference("Users").child(elemnt.toString())
                                                .addValueEventListener(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(snapshot: DataSnapshot) {

                                                        val name =
                                                            snapshot.child("name")
                                                                .getValue().toString()


                                                        val cartacc =
                                                            snapshot.child("Carta Caçadore")
                                                                .getValue().toString()


                                                        val numSocio = snapshot.child("Grupos")
                                                            .child(
                                                                num.toString()
                                                            ).child("Socio")
                                                            .getValue().toString()

                                                        val refUser = snapshot.child("uid")
                                                            .getValue().toString()

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
                    val numero = num
                    startActivity(
                        Intent(
                            this@AdmissaoActivity,
                            CriarOrgEventoActivity::class.java
                        ).apply {
                            putExtra(
                                EXTRA_MESSAGE,
                                numero.toString()
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
        val inflate_view = inflater.inflate(R.layout.adesao_custom_view, null)

        val texto = inflate_view.textViewShow

        val user = uid

        val nome = name
        val numeroCC = numCC
        val numeroSocio = numSoc
        var num = 0
        val valu = ArrayList<String>()

        texto.text =
            "Nome: " + nome + "\n" + "Nº Carta Caçador: " + numeroCC + "\n" + "Nº Socio: " + numeroSocio

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Socio Pendente")
        alertDialog.setView(inflate_view)
        alertDialog.setCancelable(false)

        alertDialog.setNegativeButton("Rejeitar") { dialog, which ->
            Toast.makeText(this, "Rejeitado", Toast.LENGTH_LONG).show()
        }

        alertDialog.setPositiveButton("Aceitar") { dialog, which ->


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
                            "DocumentSnapshot data: ${b}"
                        )

                        num = valu.size



                        num = num + 1
                        Log.d(
                            "adesa",
                            "DocumentSnapshot data: ${num}"
                        )
                        Log.d(
                            "adesa",
                            "DocumentSnapshot data: ${numeroSocio}"
                        )

                            val update = HashMap<String, Any>()
                            update["$numeroSocio"] = user

                        val updateUser = HashMap<String, Any>()
                        updateUser["$numeroGrupo"] = numeroSocio

                        //adiciona ao grupo nos membros se ele for aceite
                            mAuth.getReference("Grupos").child(nomeGrupo).child("membros")
                                .updateChildren(update)

                        //adiciona no utilizador o a secçao dos grupos o seu numero de socio se ele for aceite
                        mAuth.getReference("Users").child(user).child("Grupos")
                            .updateChildren(updateUser)

                        //remove o utilizador da lista de pendetes
                            mAuth.getReference("Grupos").child(nomeGrupo).child("Pendentes")
                                .child(user)
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

//        if (item.itemId == R.id.pendente) {
//
//            startActivity(Intent(this, AdmissaoActivity::class.java))
//        }


        return super.onOptionsItemSelected(item)
    }

}