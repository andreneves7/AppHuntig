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

    // --- isEmailValido ---

    @Test
    fun `emails validos sao aceites`() {
        assertTrue(Validacoes.isEmailValido("joao@example.com"))
        assertTrue(Validacoes.isEmailValido("joao.silva@example.co.uk"))
        assertTrue(Validacoes.isEmailValido("joao+caca@example.com"))
        assertTrue(Validacoes.isEmailValido("j@e.pt"))
    }

    @Test
    fun `emails invalidos sao rejeitados`() {
        assertFalse(Validacoes.isEmailValido(""))
        assertFalse(Validacoes.isEmailValido("joao"))
        assertFalse(Validacoes.isEmailValido("joao@"))
        assertFalse(Validacoes.isEmailValido("@example.com"))
        assertFalse(Validacoes.isEmailValido("joao example.com"))
        assertFalse(Validacoes.isEmailValido("joao@example"))
    }

    // --- isPasswordValida ---

    @Test
    fun `password com 6 ou mais caracteres e valida`() {
        assertTrue(Validacoes.isPasswordValida("123456"))
        assertTrue(Validacoes.isPasswordValida("umapasswordlonga"))
    }

    @Test
    fun `password com menos de 6 caracteres e invalida`() {
        assertFalse(Validacoes.isPasswordValida(""))
        assertFalse(Validacoes.isPasswordValida("12345"))
    }

    // --- comprimentos de campos do registo ---

    @Test
    fun `comprimentos exatos sao aceites`() {
        assertTrue(Validacoes.isTelefoneValido("912345678"))
        // NOTA: um código postal português no formato NNNN-NNN tem 8
        // caracteres (4+hífen+3), mas esta validação (herdada do código
        // original, não alterada aqui) exige exatamente 7 — por exemplo
        // "1234-56" passa, mas "1234-567" (formato real) falha. Pode ser
        // um bug pré-existente que vale a pena confirmares; o teste aqui
        // reflete o comportamento ATUAL do código, não o que seria
        // "correto" para o formato postal real.
        assertTrue(Validacoes.isCodigoPostalValido("1234-56"))
        assertFalse(Validacoes.isCodigoPostalValido("1234-567"))
        assertTrue(Validacoes.isCartaCacaValida("123456"))
        assertTrue(Validacoes.isLicencaArmaValida("12345"))
        assertTrue(Validacoes.isNumeroApoliceValido("1234567890"))
    }

    @Test
    fun `comprimentos errados sao rejeitados`() {
        assertFalse(Validacoes.isTelefoneValido("12345"))
        assertFalse(Validacoes.isCodigoPostalValido("123"))
        assertFalse(Validacoes.isCartaCacaValida("12"))
        assertFalse(Validacoes.isLicencaArmaValida(""))
        assertFalse(Validacoes.isNumeroApoliceValido("123"))
    }
}
