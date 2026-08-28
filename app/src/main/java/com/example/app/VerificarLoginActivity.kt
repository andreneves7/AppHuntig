package com.example.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.provider.AlarmClock
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class VerificarLoginActivity : AppCompatActivity() {

    private val a = FirebaseAuth.getInstance().currentUser
    private val b = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (a == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // Login por biometria (ver Utils.kt): opt-in, desativado por
            // omissão — só afeta quem o ativou explicitamente em
            // ProfileActivity. Para todos os outros, o comportamento é
            // EXATAMENTE o mesmo de antes desta funcionalidade existir
            // (vai direto para rotearUtilizador()).
            if (biometriaEstaAtivada() && biometriaDisponivel()) {
                pedirBiometriaEDepoisRotear()
            } else {
                rotearUtilizador()
            }
        }
    }

    private fun pedirBiometriaEDepoisRotear() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    rotearUtilizador()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Falhou, foi cancelado, ou o utilizador escolheu "usar
                    // password" — por segurança, termina a sessão e volta ao
                    // ecrã de login normal, em vez de deixar entrar sem
                    // confirmar a identidade.
                    Log.d("VerificarLogin", "biometria falhou/cancelada: $errString")
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@VerificarLoginActivity, LoginActivity::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Impressão digital/rosto não reconhecido, mas ainda não é
                    // um erro definitivo — o sistema deixa a pessoa tentar de
                    // novo sozinho, não fazemos nada aqui.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Confirma a tua identidade")
            .setSubtitle("Usa a tua biometria para entrar")
            .setNegativeButtonText("Usar password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun rotearUtilizador() {
        val uid = a?.uid ?: return
        val c = b.getReference("Users").child(uid)
        c.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val org = dataSnapshot.child("Org").value as? Boolean
                val roleValue = dataSnapshot.child("role").getValue(String::class.java)
                val role = Roles.resolver(roleValue, org)

                val gv = application as VariaveisGlobais
                gv.role = role

                when (role) {
                    Roles.SUPERADMIN -> startActivity(
                        Intent(this@VerificarLoginActivity, SuperAdminActivity::class.java)
                    )
                    Roles.ORGANIZACAO -> startActivity(
                        Intent(this@VerificarLoginActivity, OrgActivity::class.java)
                    )
                    else -> {
                        val marca = 0
                        val intent = Intent(
                            this@VerificarLoginActivity,
                            FiltrosActivity::class.java
                        ).apply {
                            putExtra(AlarmClock.EXTRA_MESSAGE, marca)
                        }
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("todo_fix", "erro Firebase: ${error.message}")
                this@VerificarLoginActivity.mostrarErroLigacao()
            }
        })
        finish()
    }
}
