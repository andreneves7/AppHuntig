package com.example.app

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.app.databinding.ActivityMainBinding
import java.util.*


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val mAuth = FirebaseDatabase.getInstance()
    val Auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val loginBtn = binding.bLogin
        val loginOrg = binding.bLoginOrg
        val regTxt = binding.bRegisto

        // Pré-preenche com o último email usado com sucesso, para poupar o
        // utilizador de o escrever de novo sempre que abre a app.
        val emailGuardado = getSharedPreferences("apphuntig_prefs", MODE_PRIVATE)
            .getString("ultimo_email", null)
        if (emailGuardado != null) {
            binding.idEmail.setText(emailGuardado)
        }

        loginOrg.setVisibility(View.INVISIBLE)

        loginBtn.setOnClickListener(View.OnClickListener { view ->
            login()
        })

        regTxt.setOnClickListener(View.OnClickListener { view ->
            register()
        })

        loginOrg.setOnClickListener(View.OnClickListener { view ->
            loginOrg()
        })

        binding.tEsqueciPassword.setOnClickListener {
            recuperarPassword()
        }
    }

    /**
     * Envia um email de recuperação de password através do Firebase Auth.
     * Documentação oficial: https://firebase.google.com/docs/auth/android/manage-users#send_a_password_reset_email
     */
    /**
     * Guarda o email para pré-preencher da próxima vez (ver onCreate). Só é
     * chamado depois de um login com sucesso — nunca guarda algo que o
     * utilizador escreveu por engano.
     */
    private fun guardarEmailLembrado(email: String) {
        getSharedPreferences("apphuntig_prefs", MODE_PRIVATE)
            .edit()
            .putString("ultimo_email", email)
            .apply()
    }

    private fun recuperarPassword() {
        val emailInput = android.widget.EditText(this)
        emailInput.hint = "Email"
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        // Pré-preenche com o que já estiver escrito no campo de email do login, se houver.
        emailInput.setText(binding.idEmail.text.toString())

        val padding = (16 * resources.displayMetrics.density).toInt()
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = padding
        params.rightMargin = padding
        emailInput.layoutParams = params
        container.addView(emailInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Recuperar password")
            .setMessage("Introduz o teu email. Vamos enviar-te um link para definires uma password nova.")
            .setView(container)
            .setCancelable(true)
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Enviar") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, this.getString(R.string.msg_introduz_o_teu_email), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                Auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, this.getString(R.string.msg_email_enviado_verifica_a_tua_caixa_de_en), Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // Nota: por segurança, o Firebase às vezes não distingue "email não existe"
                        // de outros erros. Mostramos uma mensagem genérica em vez de confirmar/negar
                        // se o email está registado, para não revelar essa informação a terceiros.
                        Log.d("Login", "erro ao enviar reset de password: ${task.exception?.message}")
                        Toast.makeText(this, this.getString(R.string.msg_nao_foi_possivel_enviar_o_email_verifica), Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }

    fun loginOrg() {
        val emailTxt = binding.idEmail
        val passwordTxt = binding.idPassword

        var email = emailTxt.text.toString()
        var password = passwordTxt.text.toString()

        if (!email.isEmpty() && !password.isEmpty()) {
            Auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    guardarEmailLembrado(email)
                    if (Auth.currentUser!!.isEmailVerified) {


                        val ver = mAuth.getReference("Users").child(Auth.currentUser!!.uid)
                        ver.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                val org = dataSnapshot.child("Org").getValue() as? Boolean
                                val roleValue = dataSnapshot.child("role").getValue(String::class.java)
                                val role = Roles.resolver(roleValue, org)



                                if (role == Roles.ORGANIZACAO || role == Roles.SUPERADMIN) {

                                    val gv = application as VariaveisGlobais
                                    gv.role = role

                                    // SuperAdmin também consegue entrar por este ecrã (login de
                                    // organização), mas vai para o painel de gestão total em vez
                                    // do painel normal da organização.
                                    val destino = if (role == Roles.SUPERADMIN)
                                        SuperAdminActivity::class.java else OrgActivity::class.java

                                    val intent = Intent(this@LoginActivity, destino)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)


                                    Toast.makeText(this@LoginActivity, this@LoginActivity.getString(R.string.msg_successfully_logged_in), Toast.LENGTH_LONG
                                    )
                                        .show()
                                    Log.d("Login", "user ${Auth.currentUser?.uid}")
                                } else {

                                    Toast.makeText(this@LoginActivity, this@LoginActivity.getString(R.string.msg_so_pode_entrar_com_cacador), Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.d("Login", "fail dados")
                            }
                        })


                    } else {
                        Toast.makeText(this, this.getString(R.string.msg_verifique_email), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, this.getString(R.string.msg_error_logging_in), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, this.getString(R.string.msg_please_fill_up_the_credetianls), Toast.LENGTH_LONG).show()
        }
    }

    private fun login() {
        val emailTxt = binding.idEmail
        val passwordTxt = binding.idPassword

        var email = emailTxt.text.toString()
        var password = passwordTxt.text.toString()


        val uid = Auth.currentUser?.uid

        Log.d("Login", "user ${uid}")

        if (!email.isEmpty() && !password.isEmpty()) {

            Auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        guardarEmailLembrado(email)
                        if (Auth.currentUser!!.isEmailVerified) {
                            val ver = mAuth.getReference("Users").child(Auth.currentUser!!.uid)
                            ver.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // This method is called once with the initial value and again
                                    // whenever data at this location is updated.
                                    val org = dataSnapshot.child("Org").getValue() as? Boolean
                                    val roleValue = dataSnapshot.child("role").getValue(String::class.java)
                                    val role = Roles.resolver(roleValue, org)
                                    val controlo = dataSnapshot.child("Controlo").getValue()


                                    Log.d("Login", "user ${Auth.currentUser?.uid} role=$role")

                                    // SuperAdmin tem sempre acesso, independentemente do campo
                                    // "Controlo" (aprovação), pois é a conta de gestão da própria
                                    // plataforma, criada manualmente pelo dono do projeto.
                                    if (role == Roles.SUPERADMIN) {
                                        val gv = application as VariaveisGlobais
                                        gv.role = role
                                        val intent = Intent(this@LoginActivity, SuperAdminActivity::class.java)
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        return
                                    }

                                    if (controlo == true) {

                                        if (role == Roles.CACADOR) {

                                            val gv = application as VariaveisGlobais
                                            gv.role = role

                                            ver.addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(first: DataSnapshot) {


                                                    val v = first.child("FirstTime").getValue()

                                                    Log.d("Login", "user primeira ${v}")


                                                    //verifica se a conta esta ser inicializada pela primeira vez
                                                    if (v == true) {


                                                        val intent =
                                                            Intent(
                                                                this@LoginActivity,
                                                                PreferenciasActivity::class.java
                                                            )
                                                        intent.flags =
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(
                                                                Intent.FLAG_ACTIVITY_NEW_TASK
                                                            )
                                                        startActivity(intent)

//                                                    val p = HashMap<String, Any>()
//                                                    p["FirstTime"] = false
//                                                    ver.updateChildren(p)
                                                    } else {

                                                        val marca = 0

                                                        val intent =
                                                            Intent(
                                                                this@LoginActivity,
                                                                FiltrosActivity::class.java
                                                            ).apply {
                                                                putExtra(AlarmClock.EXTRA_MESSAGE, marca)
                                                            }
                                                        intent.flags =
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(
                                                                Intent.FLAG_ACTIVITY_NEW_TASK
                                                            )
                                                        startActivity(intent)
                                                        //startActivity(Intent(this, home::class.java))
                                                    }


                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.d("Login", "fail dados")
                                                }
                                            })

                                        } else {
                                            Toast.makeText(this@LoginActivity, this@LoginActivity.getString(R.string.msg_esta_fazer_login_errado_mudar_para_organ), Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    } else {

                                        Toast.makeText(this@LoginActivity, this@LoginActivity.getString(R.string.msg_tem_esperar_pela_aprovacao), Toast.LENGTH_SHORT
                                        ).show();

                                    }

                                }


                                override fun onCancelled(error: DatabaseError) {
                                    // Failed to read value
                                    Log.d("Login", "fail dados")
                                }


                            })
                            Toast.makeText(this, this.getString(R.string.msg_successfully_logged_in), Toast.LENGTH_LONG
                            )
                                .show()
                            Log.d("Login", "user ${Auth.currentUser?.uid}")


                        } else {
                            Toast.makeText(this, this.getString(R.string.msg_verifique_email), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, this.getString(R.string.msg_error_logging_in), Toast.LENGTH_SHORT)
                            .show()
                    }

                }
        } else {
            Toast.makeText(this, this.getString(R.string.msg_please_fill_up_the_credetianls), Toast.LENGTH_LONG).show()
        }


    }

    private fun register() {

        val intent = Intent(this, RegistoUserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_direita_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val regTxt = binding.bRegisto
        val loginBtn = binding.bLogin
        val loginOrg = binding.bLoginOrg



        if (item!!.itemId == R.id.Cacador) {

            regTxt.setVisibility(View.VISIBLE)
            loginBtn.setVisibility(View.VISIBLE)
            loginOrg.setVisibility(View.INVISIBLE)

        }

        if (item.itemId == R.id.Organizacao) {

            regTxt.setVisibility(View.INVISIBLE)
            loginBtn.setVisibility(View.INVISIBLE)
            loginOrg.setVisibility(View.VISIBLE)
        }



        return super.onOptionsItemSelected(item)
    }


}
