package com.example.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.app.databinding.ActivityOnboardingBinding

/**
 * Ecrã de boas-vindas simples, mostrado uma única vez, na primeira vez que
 * um caçador faz login (ver o ponto onde "FirstTime" é lido em
 * LoginActivity.kt). No fim (ou se saltar), segue para PreferenciasActivity
 * — o mesmo ecrã que já era o próximo passo antes desta funcionalidade
 * existir, agora com uma introdução à app antes.
 *
 * Ecrã construído de raiz (layout novo, sem mexer em nenhum já existente),
 * para não correr o risco de sobrepor elementos em layouts já ocupados que
 * não consigo renderizar para confirmar.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var passoAtual = 0

    private data class Par(val titulo: String, val descricao: String)

    private val passos = listOf(
        Par(
            "Bem-vindo à HuntingEvent",
            "Liga-te a associações e grupos de caça, e fica a par dos seus eventos."
        ),
        Par(
            "Encontra grupos",
            "Explora os grupos disponíveis e pede adesão às associações que te interessam."
        ),
        Par(
            "Eventos e presenças",
            "Vê os eventos dos teus grupos, inscreve-te, e marca presença no dia — mesmo por QR code."
        ),
        Par(
            "Tudo pronto",
            "Só falta escolheres as tuas preferências para começares."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mostrarPasso(0)

        binding.bSaltarOnboarding.setOnClickListener {
            terminar()
        }

        binding.bSeguinteOnboarding.setOnClickListener {
            if (passoAtual < passos.size - 1) {
                mostrarPasso(passoAtual + 1)
            } else {
                terminar()
            }
        }
    }

    private fun mostrarPasso(indice: Int) {
        passoAtual = indice
        val passo = passos[indice]
        binding.tituloOnboarding.text = passo.titulo
        binding.descricaoOnboarding.text = passo.descricao
        binding.indicadorOnboarding.text = "${indice + 1} / ${passos.size}"
        binding.bSeguinteOnboarding.text = if (indice == passos.size - 1) "Começar" else "Seguinte"
    }

    private fun terminar() {
        val intent = Intent(this, PreferenciasActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
