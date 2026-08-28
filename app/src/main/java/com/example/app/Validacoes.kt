package com.example.app

import java.util.Calendar
import java.util.regex.Pattern

/**
 * Validações puras (sem dependências do Android), extraídas de EventoActivity
 * para poderem ser testadas com JUnit normal, sem precisar de emulador.
 */
object Validacoes {

    /**
     * Confirma que uma hora está no formato HH:mm (00:00 a 23:59).
     */
    fun isTimeValid(horas: String): Boolean {
        val expression = "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        return pattern.matcher(horas).matches()
    }

    /**
     * Confirma que uma data de fim não é anterior à data de início.
     * Os meses aqui são no formato "humano" (1-12), não o formato 0-indexado
     * do java.util.Calendar — a conversão é feita internamente.
     */
    fun isPeriodoValido(
        anoInicio: Int, mesInicio: Int, diaInicio: Int,
        anoFim: Int, mesFim: Int, diaFim: Int
    ): Boolean {
        val inicio = Calendar.getInstance()
        inicio.set(anoInicio, mesInicio - 1, diaInicio, 0, 0, 0)

        val fim = Calendar.getInstance()
        fim.set(anoFim, mesFim - 1, diaFim, 0, 0, 0)

        return !fim.before(inicio)
    }
}
