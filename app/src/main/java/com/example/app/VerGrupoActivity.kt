package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_expandable.*
import kotlinx.android.synthetic.main.activity_ver_grupo.*

class VerGrupoActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance()
    val Auth = FirebaseAuth.getInstance()

    val title: MutableList<String> = ArrayList()
    val subTitle: MutableList<MutableList<String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_grupo)


        val user = Auth.currentUser
        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {


//                    val ref = document.data?.get("grupo")as List<String>
//                    for (grupo in ref){
//                        mAuth.collection("Grupos").document(grupo).get().addOnSuccessListener {
//                            result ->
//                            val grupoDoc = result
//                            values.add(grupoDoc!!.get("nome").toString())
//                        }
//                    }


                    val g = document.data?.get("grupo").toString()

                    val diff_grupos: MutableList<String> = ArrayList()
                    diff_grupos.add(g)

                    subTitle.add(diff_grupos)

                    Log.d(
                        "Grupo",
                        " ${document.id} => ${document.data?.get("name")}, ${document.data?.get("grupo")}"
                    )




                    Log.d("evento","DocumentSnapshot data: ${document.data?.get("name")}"
                    )
                } else {
                    Log.d("evento", "No such document")
                }
            }
        }


        title.add("Grupos")

        expandableListView2.setAdapter(ExpandableListViewAdapter(this,expandableListView2,title,subTitle))



    }
}
