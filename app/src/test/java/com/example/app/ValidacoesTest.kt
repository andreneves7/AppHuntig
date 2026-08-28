package com.example.app

import org.junit.Test
import org.junit.Assert.*

/**
 * Testes para Validacoes.kt (lógica extraída de EventoActivity para ficar
 * testável em JVM local, sem precisar de emulador).
 */
class ValidacoesTest {

    // --- isTimeValid ---

    @Test
    fun `horas validas no formato HH-mm sao aceites`() {
        assertTrue(Validacoes.isTimeValid("00:00"))
        assertTrue(Validacoes.isTimeValid("09:30"))
        assertTrue(Validacoes.isTimeValid("23:59"))
        assertTrue(Validacoes.isTimeValid("9:30"))
    }

    @Test
    fun `horas invalidas sao rejeitadas`() {
        assertFalse(Validacoes.isTimeValid("24:00"))
        assertFalse(Validacoes.isTimeValid("12:60"))
        assertFalse(Validacoes.isTimeValid("abc"))
        assertFalse(Validacoes.isTimeValid(""))
        assertFalse(Validacoes.isTimeValid("12h30"))
        assertFalse(Validacoes.isTimeValid("12:5"))
    }

    // --- isPeriodoValido ---

    @Test
    fun `data de fim depois da data de inicio e valida`() {
        assertTrue(
            Validacoes.isPeriodoValido(
                anoInicio = 2026, mesInicio = 8, diaInicio = 1,
                anoFim = 2026, mesFim = 8, diaFim = 5
            )
        )
    }

    @Test
    fun `mesmo dia de inicio e fim e valido`() {
        assertTrue(
            Validacoes.isPeriodoValido(
                anoInicio = 2026, mesInicio = 8, diaInicio = 10,
                anoFim = 2026, mesFim = 8, diaFim = 10
            )
        )
    }

    @Test
    fun `data de fim antes da data de inicio e invalida - bug corrigido nesta sessao`() {
        assertFalse(
            Validacoes.isPeriodoValido(
                anoInicio = 2026, mesInicio = 8, diaInicio = 10,
                anoFim = 2026, mesFim = 8, diaFim = 5
            )
        )
    }

    @Test
    fun `data de fim num ano anterior e invalida`() {
        assertFalse(
            Validacoes.isPeriodoValido(
                anoInicio = 2026, mesInicio = 1, diaInicio = 1,
                anoFim = 2025, mesFim = 12, diaFim = 31
            )
        )
    }

    @Test
    fun `periodo que atravessa a virada do ano e valido`() {
        assertTrue(
            Validacoes.isPeriodoValido(
                anoInicio = 2026, mesInicio = 12, diaInicio = 30,
                anoFim = 2027, mesFim = 1, diaFim = 2
            )
        )
    }
}
