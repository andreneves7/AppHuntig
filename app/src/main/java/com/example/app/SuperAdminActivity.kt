package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.app.databinding.ActivitySuperAdminBinding

/**
 * Painel de gestão global da plataforma, acessível apenas a contas com
 * role == Roles.SUPERADMIN (ver Roles.kt e LoginActivity).
 *
 * Fase atual (v1): lista todas as organizações registadas.
 * Próximos passos previstos (ver docs/PLANO_DESENVOLVIMENTO.md):
 *   - Ver/editar todos os utilizadores da plataforma, independentemente da organização
 *   - Ver/editar todos os grupos e eventos, independentemente da organização
 *   - Aprovar/rejeitar novas organizações diretamente daqui
 *
 * NOTA IMPORTANTE: os ecrãs existentes de organização (ex: ListaSociosOrgActivity,
 * OrgActivity) foram construídos a partir do princípio de que o utilizador
 * autenticado É o admin dessa organização específica (comparam o uid do
 * utilizador com o campo "admin" de cada grupo/organização). Não são
 * diretamente reutilizáveis para o SuperAdmin ver dados de OUTRAS
 * organizações sem alterações a essa lógica de comparação. Por isso os
 * botões "Ver todos os utilizadores" / "Ver todos os grupos" ainda não
 * navegam para esses ecrãs — fica assinalado como próximo passo no plano,
 * para não dar a entender uma funcionalidade que ainda não está pronta.
 */
class SuperAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperAdminBinding
    val auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Segurança extra do lado do cliente: se por alguma razão esta Activity
        // for aberta sem o utilizador ter role de superadmin, expulsa-o.
        // A validação verdadeira e obrigatória tem de estar nas regras de
        // segurança do Firebase (ver docs/PLANO_DESENVOLVIMENTO.md secção 4.1),
        // isto é apenas uma proteção adicional na app.
        val gv = application as VariaveisGlobais
        if (gv.role != Roles.SUPERADMIN) {
            Toast.makeText(this, "Acesso restrito", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        carregarOrganizacoes()

        binding.bTodosUtilizadoresSuperAdmin.setOnClickListener {
            Toast.makeText(
                this,
                "Ainda em desenvolvimento — ver docs/PLANO_DESENVOLVIMENTO.md",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.bTodosGruposSuperAdmin.setOnClickListener {
            Toast.makeText(
                this,
                "Ainda em desenvolvimento — ver docs/PLANO_DESENVOLVIMENTO.md",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun carregarOrganizacoes() {
        val ref = mAuth.getReference("Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nomesOrgs = ArrayList<String>()
                val uidsOrgs = ArrayList<String>()

                for (userSnap in snapshot.children) {
                    val orgLegacy = userSnap.child("Org").getValue() as? Boolean
                    val roleValue = userSnap.child("role").getValue(String::class.java)
                    val role = Roles.resolver(roleValue, orgLegacy)

                    if (role == Roles.ORGANIZACAO) {
                        val nome = userSnap.child("name").getValue(String::class.java)
                            ?: userSnap.key ?: "(sem nome)"
                        nomesOrgs.add(nome)
                        uidsOrgs.add(userSnap.key ?: "")
                    }
                }

                binding.tSemOrgsSuperAdmin.isVisible = nomesOrgs.isEmpty()

                val adapter = ArrayAdapter(
                    this@SuperAdminActivity,
                    R.layout.listview_item,
                    nomesOrgs
                )
                binding.ListViewOrgsSuperAdmin.adapter = adapter

                binding.ListViewOrgsSuperAdmin.setOnItemClickListener { _, _, position, _ ->
                    val uid = uidsOrgs.getOrNull(position)
                    Log.d("SuperAdmin", "organizacao selecionada uid=$uid")
                    Toast.makeText(
                        this@SuperAdminActivity,
                        "${nomesOrgs[position]} (uid: $uid)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("SuperAdmin", "fail ao carregar organizacoes: ${error.message}")
                Toast.makeText(
                    this@SuperAdminActivity,
                    "Erro ao carregar organizações",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita_org, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.signOut2) {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
