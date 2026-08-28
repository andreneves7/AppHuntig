package com.example.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.databinding.ActivityDefinicoesBinding

/**
 * Ecrã central de definições da app.
 */
class DefinicoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDefinicoesBinding

    // Códigos de idioma (tags BCP-47) na mesma ordem das opções mostradas no
    // spinner — usados para saber a que idioma corresponde a posição
    // escolhida, e para pré-selecionar a posição certa ao abrir o ecrã.
    private val codigosIdioma = listOf(IDIOMA_SISTEMA, "pt", "en", "es")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefinicoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBiometria()
        configurarNotificacoes()
        configurarTema()
        configurarIdioma()
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

    private fun configurarTema() {
        val radioId = when (temaGuardado()) {
            TEMA_CLARO -> R.id.radioTemaClaro
            TEMA_ESCURO -> R.id.radioTemaEscuro
            else -> R.id.radioTemaSistema
        }
        binding.radioGroupTema.check(radioId)

        binding.radioGroupTema.setOnCheckedChangeListener { _, checkedId ->
            val tema = when (checkedId) {
                R.id.radioTemaClaro -> TEMA_CLARO
                R.id.radioTemaEscuro -> TEMA_ESCURO
                else -> TEMA_SEGUIR_SISTEMA
            }
            definirTema(tema)
            // AppCompatDelegate.setDefaultNightMode() só recria totalmente o
            // visual em Activities que ainda não existiam ou são recriadas —
            // este próprio ecrã precisa de recreate() para mostrar já a
            // mudança, em vez de só na próxima vez que a app abrir.
            recreate()
        }
    }

    private fun configurarIdioma() {
        val nomes = listOf(
            "Idioma do telemóvel",
            "Português",
            "English",
            "Español"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIdioma.adapter = adapter

        val posicaoAtual = codigosIdioma.indexOf(idiomaAtual()).let { if (it == -1) 0 else it }
        binding.spinnerIdioma.setSelection(posicaoAtual, false)

        binding.spinnerIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                definirIdioma(codigosIdioma[position])
                // setApplicationLocales() recria a app automaticamente para
                // aplicar o novo idioma a todos os ecrãs — não é preciso
                // chamar recreate() aqui manualmente.
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
