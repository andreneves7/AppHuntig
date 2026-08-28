package com.example.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.example.app.databinding.ActivityFiltrosHostBinding

/**
 * PROVA DE CONCEITO da migração Activity -> Fragment com Navigation
 * Component (ver docs/PLANO_DESENVOLVIMENTO.md). Esta Activity passou a
 * ser um "host" fino: só aloja o FiltrosFragment (ver
 * activity_filtros_host.xml, nav_graph_filtros.xml, FiltrosFragment.kt),
 * que tem toda a lógica de conteúdo do ecrã.
 *
 * Nada muda para quem chama esta Activity (LoginActivity, HomeActivity,
 * ProfileActivity, etc.) — o nome da classe, o Intent, e o EXTRA_MESSAGE
 * continuam exatamente iguais a antes desta conversão. O menu de opções
 * (signOut/profile/Lis/grupo/home) fica na Activity porque navega para
 * OUTRAS Activities, não depende de nada específico do Fragment.
 */
class FiltrosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFiltrosHostBinding
    val Auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiltrosHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pedirPermissaoNotificacoes()
        HuntigMessagingService.guardarTokenAtualNoFirebase()
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
        }

        if (item.itemId == R.id.profile) {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (item.itemId == R.id.Lis) {
            startActivity(Intent(this, ListaGruposActivity::class.java))
        }

        if (item.itemId == R.id.grupo) {
            startActivity(Intent(this, VerGrupoActivity::class.java))
        }

        if (item.itemId == R.id.eventosProximos) {
            startActivity(Intent(this, EventosProximosActivity::class.java))
        }

        if (item.itemId == R.id.checkInQR) {
            iniciarScanQR()
        }

        if (item.itemId == R.id.definicoes) {
            startActivity(Intent(this, DefinicoesActivity::class.java))
        }

        if (item.itemId == R.id.home) {
            val marca = 0
            val intent = Intent(this, FiltrosActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, marca)
            }
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!processarResultadoScanQR(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
