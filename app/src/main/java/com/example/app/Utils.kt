package com.example.app

import android.content.Context
import android.widget.Toast

/**
 * Mostra um erro visível ao utilizador quando uma leitura/escrita do Firebase falha
 * (ex: sem ligação à internet, ou pedido recusado pelas regras de segurança).
 *
 * Antes desta função existir, praticamente todos os `onCancelled` da app só
 * escreviam num `Log.d` — o utilizador via o ecrã simplesmente parado, sem
 * perceber que algo tinha falhado. Ver docs/PLANO_DESENVOLVIMENTO.md.
 */
fun Context.mostrarErroLigacao() {
    Toast.makeText(this, this.getString(R.string.msg_nao_foi_possivel_carregar_os_dados_verif), Toast.LENGTH_LONG
    ).show()
}

/**
 * Guarda o instante do último clique de cada View individualmente — clicar
 * no botão A não bloqueia clicar logo a seguir no botão B, só bloqueia
 * cliques repetidos no MESMO botão dentro do intervalo.
 *
 * Usa WeakHashMap para nunca impedir a View de ser recolhida pelo garbage
 * collector — a entrada desaparece sozinha quando a View deixa de existir.
 */
private val ultimosCliques = java.util.WeakHashMap<android.view.View, Long>()

/**
 * Substituto de setOnClickListener para botões que disparam uma ação
 * importante e não-repetível (gravar dados no Firebase, submeter um
 * formulário) — ignora cliques repetidos no mesmo botão dentro de
 * [intervaloMs], para não criar dados duplicados se o utilizador tocar
 * duas vezes seguidas (ecrã lento a reagir, dedo a tremer, ligação lenta
 * a fazer parecer que o primeiro toque não registou).
 *
 * Não desativa visualmente o botão — só ignora o clique extra, de forma
 * silenciosa e sem "piscar" o botão para cinzento.
 */
fun android.view.View.evitarDuploClique(intervaloMs: Long = 800, acao: (android.view.View) -> Unit) {
    setOnClickListener { view ->
        val agora = System.currentTimeMillis()
        val ultimo = ultimosCliques[view] ?: 0L
        if (agora - ultimo >= intervaloMs) {
            ultimosCliques[view] = agora
            acao(view)
        }
    }
}

/**
 * Pede a permissão de notificações (POST_NOTIFICATIONS), obrigatória a
 * partir do Android 13. Em versões anteriores do Android, esta permissão
 * nem existe — o pedido é automaticamente ignorado, seguro chamar sempre.
 *
 * Chamar isto a partir de um ecrã que fica visível o tempo suficiente para
 * o utilizador responder ao diálogo do sistema (não funciona bem em
 * Activities que se fecham logo a seguir, como VerificarLoginActivity).
 */
fun androidx.appcompat.app.AppCompatActivity.pedirPermissaoNotificacoes() {
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
        return
    }
    val permissao = android.Manifest.permission.POST_NOTIFICATIONS
    if (androidx.core.content.ContextCompat.checkSelfPermission(this, permissao)
        != android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(permissao), 1001)
    }
}
