package com.example.app


import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.app.databinding.ActivityEventoBinding
import java.util.*


class EventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventoBinding
    val Auth = FirebaseAuth.getInstance()

    lateinit var gv: VariaveisGlobais
var num = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gv = application as VariaveisGlobais


        val datePicker2 = binding.datePicker2
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


        val datePicker = binding.datePicker1
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

        val btnPop = binding.bTipos

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
        val paginaMapa = binding.bPais_User
        paginaMapa.setOnClickListener {
            evento()
        }

    }

    private fun evento() {

        val nome = binding.edNome.text.toString()
        val horas = binding.edTime.text.toString()
        val on = binding.switchForma

        var numero = intent.getStringExtra(EXTRA_MESSAGE).toInt()
        Log.d("Numero", "ola2 = $numero")

        num = numero

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

                    // Antes desta validação, nada impedia criar um evento cuja data de fim
                    // fosse anterior à data de início (as duas datas são escolhidas em
                    // DatePickers independentes, sem nenhuma verificação cruzada).
                    if (!isPeriodoValido()) {
                        Toast.makeText(this, this.getString(R.string.msg_a_data_de_fim_nao_pode_ser_anterior_a_da), Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val intent = Intent(this, MapsActivity::class.java).apply {
                        putExtra(
                            EXTRA_MESSAGE,
                            num.toString())
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, this.getString(R.string.msg_selecionar_tipo), Toast.LENGTH_SHORT).show()
                }


            } else {
                Toast.makeText(this, this.getString(R.string.msg_horas_mal_preenchidas), Toast.LENGTH_SHORT).show()
            }

        } else {

            Toast.makeText(this, this.getString(R.string.msg_preencha_campo_nome), Toast.LENGTH_SHORT).show()


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

        if (item.itemId == R.id.Lis) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.eventosProximos) {
            startActivity(Intent(this, EventosProximosActivity::class.java))
        }

        if (item.itemId == R.id.home) {

            startActivity(Intent(this, FiltrosActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Confirma que a data de fim (gv.YearFim/MonthFim/DayFim) não é anterior
     * à data de início (gv.Year/Month/Day). Ambas são preenchidas pelos dois
     * DatePickers no onCreate, de forma totalmente independente uma da outra,
     * daí ser preciso esta validação cruzada antes de avançar.
     *
     * A lógica em si vive em Validacoes.kt (testada em Validacoes*Test.kt);
     * esta função só liga a esse utilitário aos campos de VariaveisGlobais.
     */
    private fun isPeriodoValido(): Boolean {
        return Validacoes.isPeriodoValido(
            gv.Year, gv.Month, gv.Day,
            gv.YearFim, gv.MonthFim, gv.DayFim
        )
    }

    fun isTimeValid(horas: String): Boolean = Validacoes.isTimeValid(horas)


}

