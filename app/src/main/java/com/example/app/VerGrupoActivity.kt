package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_grupo.*
import kotlinx.android.synthetic.main.activity_ver_grupo.*
import org.jetbrains.anko.listView
import java.text.FieldPosition

class VerGrupoActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance()
    val Auth = FirebaseAuth.getInstance()



//    val title: MutableList<String> = ArrayList()
//    val subTitle: MutableList<MutableList<String>> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_grupo)


        val list = ListView2

        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {


                    val values = ArrayList<String>()


                    val ref = document.data?.get("grupo") as List<String>
                    Log.d(
                        "VerGrupo",
                        " ref $ref"
                    )

                    if (ref.isEmpty()) {
                        Log.d(
                            "VerGrupo",
                            " sem grupos deste user"
                        )
                    } else {
                        Log.d(
                            "VerGrupo",
                            " grupos deste user"
                        )


                        for (grupo in ref) {
                            Log.d(
                                "VerGrupo",
                                "grupo  $grupo"
                            )


                            var d = values.add(grupo).toString()
                            Log.d(
                                "VerGrupo",
                                "values $d"
                            )

                            val adapter = ArrayAdapter(this,R.layout.listview_item, values)

                            list.setAdapter(adapter)
                            
                            list.onItemClickListener = object : AdapterView.OnItemClickListener {



                                override fun onItemClick(parent: AdapterView<*>, view: View,
                                                         position: Int, id: Long){

                                    val itemValue = list.getItemAtPosition(position) as String

                                    val intent = Intent(view.context,  GrupoActivity:: class.java )
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)

                                    Toast.makeText(applicationContext,
                                        "Position :$position\nItem Value : $itemValue", Toast.LENGTH_LONG)
                                        .show()


                                }

                            }

                            
                        }
                    }

                    Log.d(
                        "VerGrupo",
                        " ${document.id} => ${document.data?.get("name")}, ${document.data?.get("grupo")}"
                    )


                    Log.d(
                        "VerGrupo", "DocumentSnapshot data: ${document.data?.get("name")}"
                    )
                } else {
                    Log.d("VerGrupo", "No such document")
                }
            }
        }

    }
}
