package com.example.app


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_evento.*
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class EventoActivity : AppCompatActivity() {


    val Auth = FirebaseAuth.getInstance()

    //val gAuth = FirebaseFirestore.getInstance().collection("Grupo")
    lateinit var gv: VariaveisGlobais


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)
        gv = application as VariaveisGlobais


        val datePicker2 = findViewById<DatePicker>(R.id.datePicker2)
        val today2 = Calendar.getInstance()
        datePicker2.init(
            today2.get(Calendar.YEAR), today2.get(Calendar.MONTH),
            today2.get(Calendar.DAY_OF_MONTH)

        ) { view, year, month, day ->
            val month = month + 1
            val ano = year
            // val msg = "You Selected: $day/$month/$year"
            Log.d(
                "evento",
                "dados2: $month , $ano , $day"
            )
            gv.MonthFim = month
            gv.DayFim = day
            gv.YearFim = year
        }


        val datePicker = findViewById<DatePicker>(R.id.datePicker1)
        val today = Calendar.getInstance()
        datePicker.init(
            today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)

        ) { view, year, month, day ->
            val month = month + 1
            val ano = year
            // val msg = "You Selected: $day/$month/$year"
            Log.d(
                "evento",
                "dados: $month , $ano , $day"
            )
            gv.Month = month
            gv.Day = day
            gv.Year = year
        }

        val btnPop = bTipos

        btnPop.setOnClickListener{

            val popMenu = PopupMenu(this@EventoActivity, btnPop)
            popMenu.menuInflater.inflate(R.menu.menu_pop, popMenu.menu)
            popMenu.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item!!.itemId){
                        R.id.checkMontaria -> gv.check = "montaria"
                        R.id.checkDiasCaca -> gv.check = "dias"
                        R.id.checkEspera -> gv.check = "espera"
                        R.id.checkRolas -> gv.check = "rolas"
                        R.id.checkTordos -> gv.check = "tordos"
                    }
                    return true
                }
            })
            popMenu.show()
        }
        val paginaMapa = bPais_User
        paginaMapa.setOnClickListener {
            evento()
        }

    }

    private fun evento() {

        val nome = edNome.text.toString()
        val horas = edTime.text.toString()
        val on = switchForma



        val user = Auth.currentUser

        if (!nome.isEmpty()
        ) {
            gv.nome = nome

            if (isTimeValid(horas) == true) {
                gv.Horas = horas

                if (on.isChecked) {
                    gv.privado = "privado"
                } else {
                    gv.privado = "publico"
                }
                if (gv.check != "") {
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Selecionar Tipo", Toast.LENGTH_SHORT).show()
                }


            } else {
                Toast.makeText(this, "Horas mal preenchidas", Toast.LENGTH_SHORT).show()
            }

        } else {

            Toast.makeText(this, "Preencha campo nome", Toast.LENGTH_SHORT).show()


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

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this,VerGrupoActivity::class.java))
        }
        if (item.itemId == R.id.home) {

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

