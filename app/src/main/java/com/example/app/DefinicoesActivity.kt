package com.example.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.databinding.ActivityDefinicoesBinding

/**
 * Ecrã central de definições da app — antes disto, a única definição que
 * existia (login por biometria) estava enterrada num item de menu dentro de
 * ProfileActivity. Concentra aqui tudo o que faz sentido ser uma
 * "definição" global da app.
 */
class DefinicoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDefinicoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefinicoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBiometria()
        configurarNotificacoes()
        configurarModoEscuro()
        mostrarVersaoApp()
    }

    private fun configurarBiometria() {
        binding.switchBiometria.isChecked = biometriaEstaAtivada()

        binding.switchBiometria.setOnCheckedChangeListener { _, ativar ->
            if (ativar && !biometriaDisponivel()) {
                Toast.makeText(
                    this,
                    "O teu telemóvel não tem biometria configurada ou disponível.",
                    Toast.LENGTH_LONG
                ).show()
                // Repõe o interruptor na posição desligada — não conseguimos
                // ativar sem hardware/configuração de biometria disponível.
                binding.switchBiometria.isChecked = false
                return@setOnCheckedChangeListener
            }
            definirBiometriaAtivada(ativar)
        }
    }

    private fun configurarNotificacoes() {
        binding.switchNotificacoes.isChecked = notificacoesEstaoAtivadas()

        binding.switchNotificacoes.setOnCheckedChangeListener { _, ativar ->
            definirNotificacoesAtivadas(ativar)
        }
    }

    private fun configurarModoEscuro() {
        binding.switchModoEscuro.isChecked = modoEscuroEstaAtivado()

        binding.switchModoEscuro.setOnCheckedChangeListener { _, ativar ->
            definirModoEscuroAtivado(ativar)
            // AppCompatDelegate.setDefaultNightMode() só recria totalmente o
            // visual em Activities que ainda não existiam ou são recriadas —
            // este próprio ecrã precisa de recreate() para mostrar já a
            // mudança, em vez de só na próxima vez que a app abrir.
            recreate()
        }
    }

    private fun mostrarVersaoApp() {
        val nomeVersao = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "?"
        }
        binding.labelVersaoApp.text = "${getString(R.string.app_name)} — versão $nomeVersao"
    }
}
