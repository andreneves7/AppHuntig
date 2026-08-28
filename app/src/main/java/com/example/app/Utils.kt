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
