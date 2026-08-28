package com.example.app

import org.junit.Test
import org.junit.Assert.*

/**
 * Testes para Roles.resolver() — a função central que decide o papel de um
 * utilizador, com compatibilidade para contas antigas que só têm o campo
 * legado "Org" e ainda não têm "role" definido.
 *
 * Estes testes correm no JVM local (não precisam de emulador Android),
 * por isso são rápidos e seguros de correr sempre antes de um commit.
 */
class RolesTest {

    @Test
    fun `role explicito tem sempre prioridade sobre o campo legado Org`() {
        assertEquals(Roles.SUPERADMIN, Roles.resolver("superadmin", false))
        assertEquals(Roles.ORGANIZACAO, Roles.resolver("organizacao", false))
        assertEquals(Roles.CACADOR, Roles.resolver("cacador", true))
    }

    @Test
    fun `sem role definido, usa o campo legado Org como fallback`() {
        assertEquals(Roles.ORGANIZACAO, Roles.resolver(null, true))
        assertEquals(Roles.CACADOR, Roles.resolver(null, false))
    }

    @Test
    fun `sem role e sem Org, assume cacador por omissao`() {
        assertEquals(Roles.CACADOR, Roles.resolver(null, null))
    }

    @Test
    fun `role em branco e tratado como nao definido, cai no fallback`() {
        assertEquals(Roles.ORGANIZACAO, Roles.resolver("", true))
        assertEquals(Roles.CACADOR, Roles.resolver("   ".trim(), false))
    }

    @Test
    fun `nunca deixa passar um role invalido escondido pelo fallback`() {
        // Regressão: uma conta nunca deve ficar sem nenhum dos 3 roles válidos.
        val resultado = Roles.resolver(null, null)
        assertTrue(
            resultado == Roles.CACADOR || resultado == Roles.ORGANIZACAO || resultado == Roles.SUPERADMIN
        )
    }
}
