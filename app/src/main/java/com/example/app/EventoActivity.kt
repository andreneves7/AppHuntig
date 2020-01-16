package com.example.app


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.internal.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_evento.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class EventoActivity : AppCompatActivity() {

    val Auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseFirestore.getInstance()
    val gAuth = FirebaseFirestore.getInstance().collection("Grupo")
    lateinit var gv: VariaveisGlobais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gv = getApplication() as VariaveisGlobais
        setContentView(R.layout.activity_evento)

        val guardarEvento = bEvento


        guardarEvento.setOnClickListener {
            evento()
        }

    }

    private fun evento() {

        val nome = edNome.text.toString()
        gv.nome = nome
        val horas = edTime.text.toString()
        val data = edData.text.toString()
        val local = edLocal.text.toString()


        val documentId = mAuth.collection("Users").document().id
        val user = Auth.currentUser

        if (user != null) {
            val mail = mAuth.collection("Users").document(user.uid)
            mail.get().addOnSuccessListener { document ->
                if (document != null) {

                    val name = document.data?.get("name")
//                    val grupo = mAuth.collection("Grupo").


                    var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val f = formatter.format(LocalDate.parse(data, formatter))
                    Log.d(
                        "evento",
                        "dados: $f, $formatter"
                    )
                    if (!nome.isEmpty() && !data.isEmpty() && isTimeValid(horas) && !local.isEmpty()) {


                        val evento = HashMap<String, Any>()
                        evento["nome"] = nome
                        evento["Presenças"] = ArrayList<String>()
                        evento["data"] = f
                        evento["horas"] = horas
                        evento["local"] = local
                        mAuth.collection("Eventos").document(nome)
                            .set(evento)

                        val up = HashMap<String, Any>()
                        up["Eventos"] = arrayListOf(nome)
                        mAuth.collection("Grupos").document(gv.Evento)
                            .update("Eventos", FieldValue.arrayUnion(nome))


                        val upd = HashMap<String, Any>()
                        upd["Presenças"] = arrayListOf(name)
                        mAuth.collection("Eventos").document(gv.nome)
                            .update("Presenças", FieldValue.arrayUnion(name))

                        Toast.makeText(this, "evento criado", Toast.LENGTH_SHORT).show()
                        Log.d(
                            "evento",
                            "dados: $nome ,$f,  ${isTimeValid(horas)} , $local"
                        )


                        val intent = Intent(this, GrupoActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        Log.d(
                            "evento",
                            "dados: $nome ,$f,  ${isTimeValid(horas)} , $local"
                        )
                        Toast.makeText(this, "Preencha campos", Toast.LENGTH_SHORT).show()


                    }


                    Log.d(
                        "evento", "DocumentSnapshot data: ${document.data?.get("name")}"
                    )
                } else {
                    Log.d("evento", "No such document")
                }
            }
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
            //startActivity(Intent (this, MainActivity :: class.java ))
        }

        if (item!!.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item!!.itemId == R.id.grupo) {

            startActivity(Intent(this, CriarGrupoActivity::class.java))
        }
        if (item!!.itemId == R.id.home) {

            startActivity(Intent(this, HomeActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    fun isTimeValid(horas: String): Boolean {
        var isValid = false
        val expression = "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$"
        val inputStr: CharSequence = horas
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(inputStr)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }


}

