package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.app.databinding.ActivitySuperAdminUsersBinding

/**
 * SuperAdminActivity v2 (ver docs/PLANO_DESENVOLVIMENTO.md secção 5): lista
 * TODOS os utilizadores da plataforma, independentemente da organização, e
 * permite ao superadmin:
 *   - Aprovar contas pendentes (campo "Controlo")
 *   - Mudar o role de qualquer conta (cacador / organizacao / superadmin)
 *
 * Antes desta funcionalidade, aprovar uma conta ou promover alguém a
 * organização só era possível editando manualmente o Firebase Console.
 *
 * Não reutiliza nenhum ecrã de organização existente (ver nota em
 * SuperAdminActivity.kt sobre porque isso não era seguro) — é um ecrã
 * novo, com a sua própria lógica de acesso já corretamente âmbito global.
 */
class SuperAdminUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperAdminUsersBinding
    val auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperAdminUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gv = application as VariaveisGlobais
        if (gv.role != Roles.SUPERADMIN) {
            Toast.makeText(this, this.getString(R.string.msg_acesso_restrito), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.swipeRefreshSuperAdminUsers.setOnRefreshListener {
            carregarUtilizadores()
        }

        carregarUtilizadores()
    }

    private fun carregarUtilizadores() {
        binding.progressSuperAdminUsers.isVisible = true
        binding.progressSuperAdminUsers.postDelayed(
            { binding.progressSuperAdminUsers.isVisible = false; binding.swipeRefreshSuperAdminUsers.isRefreshing = false }, 5000
        )

        val ref = mAuth.getReference("Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressSuperAdminUsers.isVisible = false
                binding.swipeRefreshSuperAdminUsers.isRefreshing = false

                val linhas = ArrayList<String>()
                val uids = ArrayList<String>()

                for (userSnap in snapshot.children) {
                    val nome = userSnap.child("name").getValue(String::class.java)
                        ?: userSnap.key ?: "(sem nome)"
                    val email = userSnap.child("email").getValue(String::class.java) ?: ""
                    val orgLegacy = userSnap.child("Org").getValue() as? Boolean
                    val roleValue = userSnap.child("role").getValue(String::class.java)
                    val role = Roles.resolver(roleValue, orgLegacy)
                    val aprovado = userSnap.child("Controlo").getValue() as? Boolean ?: false

                    val estado = if (aprovado) "aprovado" else "PENDENTE"
                    linhas.add("$nome — $email\nrole: $role · $estado")
                    uids.add(userSnap.key ?: "")
                }

                binding.tSemUsersSuperAdmin.isVisible = linhas.isEmpty()

                val adapter = ArrayAdapter(
                    this@SuperAdminUsersActivity,
                    R.layout.listview_item,
                    linhas
                )
                binding.ListViewUsersSuperAdmin.adapter = adapter

                binding.ListViewUsersSuperAdmin.setOnItemClickListener { _, _, position, _ ->
                    val uid = uids.getOrNull(position) ?: return@setOnItemClickListener
                    abrirGestaoUtilizador(uid, linhas[position])
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressSuperAdminUsers.isVisible = false
                binding.swipeRefreshSuperAdminUsers.isRefreshing = false
                Log.d("SuperAdminUsers", "erro ao carregar utilizadores: ${error.message}")
                this@SuperAdminUsersActivity.mostrarErroLigacao()
            }
        })
    }

    /**
     * Mostra um diálogo simples para mudar o role e o estado de aprovação
     * ("Controlo") de um utilizador. Escreve diretamente no Firebase.
     */
    private fun abrirGestaoUtilizador(uid: String, descricao: String) {
        val opcoes = arrayOf(
            "Aprovar conta (Controlo = true)",
            "Tornar Caçador",
            "Tornar Organização",
            "Tornar SuperAdmin"
        )

        AlertDialog.Builder(this)
            .setTitle(descricao)
            .setItems(opcoes) { _, which ->
                val ref = mAuth.getReference("Users").child(uid)
                when (which) {
                    0 -> ref.child("Controlo").setValue(true)
                    1 -> ref.child("role").setValue(Roles.CACADOR)
                    2 -> ref.child("role").setValue(Roles.ORGANIZACAO)
                    3 -> confirmarPromoverSuperAdmin(uid)
                }
                if (which != 3) {
                    Toast.makeText(this, this.getString(R.string.msg_atualizado), Toast.LENGTH_SHORT).show()
                    carregarUtilizadores()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Promover a superadmin é uma ação sensível (acesso total à plataforma)
     * — pede confirmação extra em vez de aplicar direto como as outras opções.
     */
    private fun confirmarPromoverSuperAdmin(uid: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar")
            .setMessage("Tens a certeza que queres dar acesso TOTAL de SuperAdmin a esta conta? Esta ação dá controlo completo sobre toda a plataforma.")
            .setPositiveButton("Sim, confirmar") { _, _ ->
                mAuth.getReference("Users").child(uid).child("role").setValue(Roles.SUPERADMIN)
                Toast.makeText(this, this.getString(R.string.msg_atualizado), Toast.LENGTH_SHORT).show()
                carregarUtilizadores()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_superadmin, menu)
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
