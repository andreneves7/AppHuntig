package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_filtros.*

class FiltrosActivity : AppCompatActivity() {
    val mAuth = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtros)

        val maior =  bMaior
        val menor = bMenor

        val esperas = bEsperas
        val montaria = bMontarias
        val tordos = bTordos
        val rolas = bRolas


        rolas.setVisibility(View.INVISIBLE)
        tordos.setVisibility(View.INVISIBLE)
        montaria.setVisibility(View.INVISIBLE)
        esperas.setVisibility(View.INVISIBLE)

        maior.setOnClickListener(View.OnClickListener { view ->

            rolas.setVisibility(View.INVISIBLE)
            tordos.setVisibility(View.INVISIBLE)
            montaria.setVisibility(View.VISIBLE)
            esperas.setVisibility(View.VISIBLE)

            esperas.setOnClickListener(View.OnClickListener { view ->
                val filtro = "esperas"
                val intent = Intent(this, HomeActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, filtro)
                }
                startActivity(intent)
            })

            montaria.setOnClickListener(View.OnClickListener { view ->
                val filtro = "montaria"
                val intent = Intent(this, HomeActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, filtro)
                }
                startActivity(intent)
            })

        })

        menor.setOnClickListener(View.OnClickListener { view ->

            rolas.setVisibility(View.VISIBLE)
            tordos.setVisibility(View.VISIBLE)
            montaria.setVisibility(View.INVISIBLE)
            esperas.setVisibility(View.INVISIBLE)
            tordos.setOnClickListener(View.OnClickListener { view ->
                val filtro = "tordos"
                val intent = Intent(this, HomeActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, filtro)
                }
                startActivity(intent)
            })
            rolas.setOnClickListener(View.OnClickListener { view ->
                val filtro = "rolas"
                val intent = Intent(this, HomeActivity::class.java).apply {
                    putExtra(EXTRA_MESSAGE, filtro)
                }
                startActivity(intent)
            })
        })

    }
}