package com.example.app

import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.app.databinding.ActivityAjudaBinding

/**
 * Ecrã de Ajuda/FAQ — perguntas frequentes construídas dinamicamente em
 * Kotlin (não em XML fixo), para não ter risco de sobreposição de
 * elementos que não consigo verificar sem renderizar.
 */
class AjudaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjudaBinding

    private val perguntas = listOf(
        "Como peço para entrar num grupo?" to
            "No menu principal, escolhe \"Lista Grupos Disponíveis\", encontra o grupo que te interessa e pede adesão. A organização vai aprovar ou rejeitar o teu pedido.",
        "A minha conta está pendente de aprovação, o que significa?" to
            "Depois de te registares, uma conta tem de ser aprovada antes de poderes entrar na app. Aguarda a aprovação — não precisas de fazer mais nada.",
        "Como marco presença num evento?" to
            "Abre os detalhes do evento e toca em \"Marcar Presença\". Se o evento já tiver atingido o limite de participantes, ficas automaticamente numa lista de espera.",
        "O que é a lista de espera?" to
            "Quando um evento tem limite de participantes e já está cheio, quem se tenta inscrever a seguir entra numa lista de espera em vez de ficar inscrito de imediato.",
        "Como faço check-in por QR Code?" to
            "No menu principal, escolhe \"Check-in por QR Code\" e aponta a câmara ao código mostrado pela organização no local do evento.",
        "Como ativo o login por biometria?" to
            "Vai a Definições (menu principal) e liga o interruptor \"Login por biometria\". O teu telemóvel precisa de ter impressão digital ou reconhecimento facial já configurado.",
        "Não estou a receber notificações, o que faço?" to
            "Confirma em Definições que as notificações estão ligadas, e confirma também nas definições do próprio telemóvel (Definições do Android > Apps > HuntingEvent > Notificações) que a permissão está concedida.",
        "Como mudo o idioma da app?" to
            "Vai a Definições e escolhe o idioma no menu — podes escolher Português, Inglês, Espanhol, ou deixar a app seguir o idioma do teu telemóvel.",
        "Esqueci-me da password, o que faço?" to
            "No ecrã de login, toca em \"Esqueceu-se da password?\" e segue as instruções — vais receber um email para definires uma password nova."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAjudaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        construirPerguntas()
    }

    private fun construirPerguntas() {
        val densidade = resources.displayMetrics.density

        for ((pergunta, resposta) in perguntas) {
            val tPergunta = TextView(this)
            tPergunta.text = pergunta
            tPergunta.textSize = 17f
            tPergunta.setTypeface(tPergunta.typeface, Typeface.BOLD)
            tPergunta.setPadding(0, (20 * densidade).toInt(), 0, (4 * densidade).toInt())
            binding.containerAjuda.addView(tPergunta)

            val tResposta = TextView(this)
            tResposta.text = resposta
            tResposta.textSize = 15f
            binding.containerAjuda.addView(tResposta)
        }
    }
}
