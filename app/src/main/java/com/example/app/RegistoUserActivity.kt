package com.example.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.app.databinding.ActivityRegistoUserBinding

/**
 * Formulário de registo — simplificado para recolher só o que é
 * necessário ao funcionamento da app (nome, email, password, telemóvel,
 * número da Carta de Caçador). Antes recolhia também morada, código
 * postal, contribuinte, bilhete de identidade/passaporte/DNI, licença de
 * uso e porte de arma, e dados de seguradora — removido a pedido
 * explícito, por serem dados pessoais sensíveis sem necessidade real para
 * o funcionamento da app (ver docs/PLANO_DESENVOLVIMENTO.md).
 */
class RegistoUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistoUserBinding
    val mAuth = FirebaseDatabase.getInstance()

    val auth = FirebaseAuth.getInstance()

    lateinit var gv: VariaveisGlobais


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistoUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gv = application as VariaveisGlobais

        val buttonRegistar = binding.bRegistar

        buttonRegistar.evitarDuploClique {
            val email = binding.addEmail.text.toString()
            val password = binding.addPass.text.toString()
            val name = binding.addNome.text.toString()
            val tele = binding.addTele.text.toString()
            val cartaCaca = binding.addCartaCaca.text.toString()

            registoAuth(email, password, name, tele, cartaCaca)
        }
    }

    private fun registoAuth(
        email: String,
        password: String,
        name: String,
        tele: String,
        cartaCaca: String
    ) {

        val teste = arrayListOf<EditText>(
            binding.addPass,
            binding.addEmail,
            binding.addNome,
            binding.addTele,
            binding.addCartaCaca
        )

        if (verificaCampos(teste) != true) {
            return
        }

        // Validação de formato de email — usa Validacoes.isEmailValido (regex
        // Kotlin puro) em vez de android.util.Patterns.EMAIL_ADDRESS, para a
        // validação poder ser testada em ValidacoesTest.kt sem precisar do
        // framework Android.
        if (!Validacoes.isEmailValido(email)) {
            binding.addEmail.error = "Email inválido"
            return
        }

        // O Firebase Auth exige um mínimo de 6 caracteres na password; validamos
        // aqui primeiro para dar feedback imediato, em vez de esperar pela
        // resposta do servidor.
        if (!Validacoes.isPasswordValida(password)) {
            binding.addPass.error = "A password tem de ter pelo menos 6 caracteres"
            return
        }

        var camposComprimentoInvalido = false
        if (!Validacoes.isTelefoneValido(tele)) {
            binding.addTele.error = "Deve ter 9 dígitos"
            camposComprimentoInvalido = true
        }
        if (!Validacoes.isCartaCacaValida(cartaCaca)) {
            binding.addCartaCaca.error = "Deve ter 6 caracteres"
            camposComprimentoInvalido = true
        }
        if (camposComprimentoInvalido) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { resultado ->
                if (!resultado.isSuccessful) return@addOnCompleteListener

                Log.d("RegistoUser", "user auth com uid: ${resultado.result?.user?.uid}")
                register(name, email, tele, cartaCaca)

                Toast.makeText(
                    this,
                    this.getString(R.string.msg_registo_com_sucesso),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception: Exception ->
                Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
            }
    }

    private fun register(name: String, email: String, tele: String, cartaCaca: String) {
        val uid = auth.uid.toString()

        val pessoa = HashMap<String, Any>()
        pessoa["uid"] = uid
        pessoa["email"] = email
        pessoa["name"] = name
        pessoa["telemovel"] = tele
        pessoa["Carta Caçadore"] = cartaCaca
        pessoa["FirstTime"] = true
        pessoa["Org"] = false // campo legado, mantido por compatibilidade — ver Roles.kt
        pessoa["role"] = Roles.CACADOR
        pessoa["Controlo"] = false

        mAuth.getReference("Users").child(uid).setValue(pessoa)

        // PerfisPublicos: cópia mínima (nome + carta de caçador) num nó
        // legível por qualquer utilizador autenticado — necessário para
        // ecrãs como a lista de pendentes/sócios de um grupo mostrarem quem
        // é quem, sem precisarem de acesso ao registo completo (email,
        // telemóvel) de outra pessoa. Ver docs/PLANO_DESENVOLVIMENTO.md.
        val perfilPublico = HashMap<String, Any>()
        perfilPublico["name"] = name
        perfilPublico["Carta Caçadore"] = cartaCaca
        perfilPublico["uid"] = uid
        mAuth.getReference("PerfisPublicos").child(uid).setValue(perfilPublico)

        sendEmailVerification()
        Log.d("RegistoUser", "email enviado")
        clearInputs()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        auth.signOut()
    }


    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Email Verfication")
            builder.setMessage("Please confirm email")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }


    fun verificaCampos(
        array: ArrayList<EditText>
    ): Boolean {

        var cont = 0
        var valido = false
        for (item in array) {
            if (item.text.isEmpty()) {
                item.error = "Falta Preencher"
                cont++
            }
        }
        if (cont == 0) {
            valido = true
        }
        return valido
    }


    private fun clearInputs() {
        binding.addNome.text.clear()
        binding.addEmail.text.clear()
        binding.addPass.text.clear()
    }


}
