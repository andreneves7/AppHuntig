package com.example.app

/**
 * Constantes centrais para os papéis (roles) de utilizador da app.
 *
 * Usar SEMPRE estas constantes em vez de strings soltas espalhadas pelo
 * código, para evitar erros de comparação (ex: "SuperAdmin" vs "superadmin")
 * e para termos um único sítio a atualizar se algum dia mudarmos os nomes.
 *
 * Guardado no Firebase em Users/{uid}/role.
 * Ver docs/PLANO_DESENVOLVIMENTO.md secção 3 para o desenho completo,
 * incluindo o motivo de manter também o campo legado "Org" durante a
 * transição.
 */
object Roles {
    const val CACADOR = "cacador"
    const val ORGANIZACAO = "organizacao"
    const val SUPERADMIN = "superadmin"

    /**
     * Deriva o role a partir dos dados atuais do Firebase, com compatibilidade
     * para contas antigas que só têm o campo booleano "Org" e ainda não têm
     * "role" definido.
     *
     * @param roleValue valor lido do campo "role" (pode ser null se a conta
     *   ainda não tiver sido migrada)
     * @param orgLegacy valor lido do campo legado "Org" (booleano)
     */
    fun resolver(roleValue: String?, orgLegacy: Boolean?): String {
        if (!roleValue.isNullOrBlank()) {
            return roleValue
        }
        return if (orgLegacy == true) ORGANIZACAO else CACADOR
    }
}
