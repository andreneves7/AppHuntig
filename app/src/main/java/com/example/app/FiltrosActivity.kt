package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_filtros.*

class FiltrosActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance()
    val Auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtros)

        val maior =  bMaior
        val menor = bMenor
        val tudo = bTudo

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

        tudo.setOnClickListener(View.OnClickListener { view ->

            rolas.setVisibility(View.INVISIBLE)
            tordos.setVisibility(View.INVISIBLE)
            montaria.setVisibility(View.INVISIBLE)
            esperas.setVisibility(View.INVISIBLE)

            val filtro = "tudo"
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, filtro)
            }
            startActivity(intent)
        })

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
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //startActivity(Intent (this, MainActivity :: class.java ))
        }

        if (item.itemId == R.id.profile) {

            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.Lis) {

            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {

            startActivity(Intent(this, CriarGrupoActivity::class.java))
        }



        if (item.itemId == R.id.home) {

            startActivity(Intent(this, FiltrosActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }
}