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

/**
 * Inicia a câmara para ler um QR code de check-in de evento (ver
 * DetalhesEventoActivity.mostrarQRCode() para o que é codificado). Usa
 * IntentIntegrator da biblioteca zxing-android-embedded — trata da
 * permissão de câmara e do ecrã de leitura sozinho.
 *
 * Chamar processarResultadoScanQR() no onActivityResult da Activity que
 * chamou isto, para tratar o resultado.
 */
fun androidx.appcompat.app.AppCompatActivity.iniciarScanQR() {
    val integrator = com.journeyapps.barcodescanner.IntentIntegrator(this)
    integrator.setPrompt("Aponta a câmara para o QR code do evento")
    integrator.setBeepEnabled(true)
    integrator.initiateScan()
}

/**
 * Processa o resultado de iniciarScanQR(). Devolve true se o resultado era
 * mesmo de um scan de QR code (tratado aqui, incluindo o caso de o
 * utilizador ter cancelado) — nesse caso a Activity chamadora NÃO deve
 * chamar super.onActivityResult(). Devolve false se o resultado não tinha
 * nada a ver com QR code, e a Activity deve continuar o processamento
 * normal (super.onActivityResult()).
 */
fun androidx.appcompat.app.AppCompatActivity.processarResultadoScanQR(
    requestCode: Int,
    resultCode: Int,
    data: android.content.Intent?
): Boolean {
    val result = com.journeyapps.barcodescanner.IntentIntegrator.parseActivityResult(
        requestCode, resultCode, data
    ) ?: return false

    val nomeEvento = result.contents
    if (nomeEvento == null) {
        // Utilizador cancelou a leitura, nada a fazer.
        return true
    }

    val gv = application as VariaveisGlobais
    gv.detalhes = nomeEvento
    startActivity(android.content.Intent(this, DetalhesEventoActivity::class.java))
    return true
}

// --- Login por biometria ---
// Funcionalidade opt-in (desativada por omissão) — só afeta quem a ativa
// explicitamente em ProfileActivity. Não substitui a autenticação do
// Firebase (que já persiste a sessão sozinha entre aberturas da app);
// é uma camada extra a pedir confirmação da identidade antes de deixar
// entrar, útil se o telemóvel ficar destrancado nas mãos de outra pessoa.

private const val PREFS_BIOMETRIA = "apphuntig_prefs"
private const val CHAVE_BIOMETRIA = "biometria_ativada"

fun android.content.Context.biometriaEstaAtivada(): Boolean {
    return getSharedPreferences(PREFS_BIOMETRIA, android.content.Context.MODE_PRIVATE)
        .getBoolean(CHAVE_BIOMETRIA, false)
}

fun android.content.Context.definirBiometriaAtivada(ativada: Boolean) {
    getSharedPreferences(PREFS_BIOMETRIA, android.content.Context.MODE_PRIVATE)
        .edit()
        .putBoolean(CHAVE_BIOMETRIA, ativada)
        .apply()
}

/**
 * Confirma que o dispositivo tem biometria configurada e disponível AGORA
 * (BIOMETRIC_STRONG — impressão digital ou reconhecimento facial seguro,
 * não um simples desbloqueio por padrão/PIN fraco).
 */
fun android.content.Context.biometriaDisponivel(): Boolean {
    val biometricManager = androidx.biometric.BiometricManager.from(this)
    return biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
        androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
}

// --- Notificações (preferência local, "silenciar" sem desativar o token) ---

fun android.content.Context.notificacoesEstaoAtivadas(): Boolean {
    return getSharedPreferences(PREFS_BIOMETRIA, android.content.Context.MODE_PRIVATE)
        .getBoolean("notificacoes_ativadas", true)
}

fun android.content.Context.definirNotificacoesAtivadas(ativadas: Boolean) {
    getSharedPreferences(PREFS_BIOMETRIA, android.content.Context.MODE_PRIVATE)
        .edit()
        .putBoolean("notificacoes_ativadas", ativadas)
        .apply()
}

// --- Modo escuro ---
// Usa AppCompatDelegate (funciona porque o tema base passou a ser
// Theme.AppCompat.DayNight, ver styles.xml) + forceDarkAllowed no tema para
// os muitos elementos com cores fixas nos layouts que não têm uma versão
// noturna própria preparada manualmente.

fun android.content.Context.modoEscuroEstaAtivado(): Boolean {
    return getSharedPreferences(PREFS_BIOMETRIA, android.content.Context.MODE_PRIVATE)
        .getBoolean("modo_escuro_ativado", false)
}

fun android.content.Context.definirModoEscuroAtivado(ativado: Boolean) {
    getSharedPreferences(PREFS_BIOMETRIA, android.content.Context.MODE_PRIVATE)
        .edit()
        .putBoolean("modo_escuro_ativado", ativado)
        .apply()
    aplicarModoEscuro(ativado)
}

fun aplicarModoEscuro(ativado: Boolean) {
    val modo = if (ativado) {
        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
    } else {
        androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
    }
    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(modo)
}
