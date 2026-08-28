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
import com.example.app.databinding.ActivitySuperAdminGruposBinding

/**
 * SuperAdminActivity v2 (ver docs/PLANO_DESENVOLVIMENTO.md secção 5): lista
 * TODOS os grupos da plataforma, independentemente de quem os administra.
 *
 * Fica deliberadamente mais simples que SuperAdminUsersActivity — mostra
 * nome, admin e número de membros pendentes/aceites, sem editar diretamente
 * (editar um grupo de outra organização é uma operação mais delicada,
 * fica para uma iteração futura se vier a ser necessária).
 */
class SuperAdminGruposActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperAdminGruposBinding
    val auth = FirebaseAuth.getInstance()
    val mAuth = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperAdminGruposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gv = application as VariaveisGlobais
        if (gv.role != Roles.SUPERADMIN) {
            Toast.makeText(this, this.getString(R.string.msg_acesso_restrito), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        carregarGrupos()
    }

    private fun carregarGrupos() {
        binding.progressSuperAdminGrupos.isVisible = true
        binding.progressSuperAdminGrupos.postDelayed(
            { binding.progressSuperAdminGrupos.isVisible = false }, 5000
        )

        val ref = mAuth.getReference("Grupos")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressSuperAdminGrupos.isVisible = false

                val linhas = ArrayList<String>()
                val detalhes = ArrayList<String>()

                for (grupoSnap in snapshot.children) {
                    val nome = grupoSnap.child("nome").getValue(String::class.java)
                        ?: grupoSnap.key ?: "(sem nome)"
                    val numero = grupoSnap.child("Numero").getValue(String::class.java) ?: "?"
                    val admin = grupoSnap.child("admin").getValue(String::class.java) ?: "(sem admin)"
                    val numMembros = grupoSnap.child("membros").childrenCount
                    val numPendentes = grupoSnap.child("Pendentes").childrenCount

                    linhas.add("$nome (nº $numero)\n$numMembros membros · $numPendentes pendentes")
                    detalhes.add("Admin (uid): $admin")
                }

                binding.tSemGruposSuperAdmin.isVisible = linhas.isEmpty()

                val adapter = ArrayAdapter(
                    this@SuperAdminGruposActivity,
                    R.layout.listview_item,
                    linhas
                )
                binding.ListViewGruposSuperAdmin.adapter = adapter

                binding.ListViewGruposSuperAdmin.setOnItemClickListener { _, _, position, _ ->
                    AlertDialog.Builder(this@SuperAdminGruposActivity)
                        .setTitle(linhas[position])
                        .setMessage(detalhes.getOrNull(position) ?: "")
                        .setPositiveButton("Fechar", null)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressSuperAdminGrupos.isVisible = false
                Log.d("SuperAdminGrupos", "erro ao carregar grupos: ${error.message}")
                this@SuperAdminGruposActivity.mostrarErroLigacao()
            }
        })
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
