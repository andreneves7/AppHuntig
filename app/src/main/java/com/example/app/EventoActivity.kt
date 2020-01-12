package com.example.app


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_evento.*
import java.util.*


class EventoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)

        val nome = edNome
        val guardarEvento = bEvento
        val horas = edTime
        val data = edData
        val local = edLocal


        guardarEvento.setOnClickListener {
//
//        val date = datePick
//
//        val calender : Calendar = Calendar.getInstance()
//            date.init(calender.get(Calendar.YEAR),calender.get(Calendar.MONTH),calender.get(Calendar.DAY_OF_MONTH),
//                { view, year, monthOfYear, dayOfMonth ->
//                    Toast.makeText(
//                        applicationContext,
//                        "#" + date.dayOfMonth + "-" + date.month + "-" + date.year + "#",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    val dataDia = date.dayOfMonth
//                    val dataMes = date.month
//                    val dataAno = date.year
            val documentId = mAuth.collection("Users").document().id

            val user = Auth.currentUser
                    if (user != null) {
                        val mail = mAuth.collection("Users").document(user.uid)
                        mail.get().addOnSuccessListener { document ->
                            if (document != null) {

                                val name = document.data?.get("name")
                                val grupo = HashMap<String, Any>()
                                grupo["nome"] = nome.text.toString()
                                grupo["pessoas"] = arrayListOf(name)
                                grupo["data"] = data
                                grupo["horas"] = horas
                                grupo["local"] = local
                                mAuth.collection("Grupo").document(documentId)
                                    .set(grupo)
                                Toast.makeText(this, "evento criado", Toast.LENGTH_SHORT).show()

                                Log.d("evento","DocumentSnapshot data: ${document.data?.get("name")}"
                                )
                            } else {
                                Log.d("evento", "No such document")
                            }
                        }
                    }
//                })
        }

    }

}

