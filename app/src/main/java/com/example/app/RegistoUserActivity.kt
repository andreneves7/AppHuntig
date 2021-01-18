package com.example.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_registo_user.*

class RegistoUserActivity : AppCompatActivity() {

    //    val mAuth = FirebaseFirestore.getInstance()
    val mAuth = FirebaseDatabase.getInstance();

    //val gAuth = FirebaseFirestore.getInstance().collection("Grupos")
    val Auth = FirebaseAuth.getInstance()

    //val mStorage = FirebaseStorage.getInstance().reference
    lateinit var gv: VariaveisGlobais


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo_user)
        gv = application as VariaveisGlobais


        val outros = addPais_Outros
        val linceca = addLicencaCacaPortugal
        val nomeSeguradoraExtra = addNomeSeguradoraExtra
        val numApoliceExtra = addNumeroApoliceExtra
        val numCaca = addNumPassCaca
        val licencaP = editTextLicencaPortugal
        val licencaE = addLicencaCacaEspanha
        val passaporte = addNumero_Passaporte
        val dni = addDNI
        val bi = addCartao
        val nif = addNif
        val licencaEspanha = addEspanhaExtra


        val btnPop = bPais_User
        val buttonRegistar = bRegistar

        val e = checkBoxEspanha
        val p = checkBoxPortugal


        var g = ""


        nomeSeguradoraExtra.setVisibility(View.INVISIBLE)
        numApoliceExtra.setVisibility(View.INVISIBLE)
        numCaca.setVisibility(View.INVISIBLE)
        linceca.setVisibility(View.INVISIBLE)
        licencaP.setVisibility(View.INVISIBLE)
        licencaEspanha.setVisibility(View.INVISIBLE)
        licencaE.setVisibility(View.INVISIBLE)


        e.setVisibility(View.INVISIBLE)
        p.setVisibility(View.INVISIBLE)


        outros.isInvisible = true
        dni.isInvisible = true
        bi.isInvisible = true
        nif.isInvisible = true
        passaporte.isInvisible = true






        btnPop.setOnClickListener {

            val popMenu = PopupMenu(this@RegistoUserActivity, btnPop)
            popMenu.menuInflater.inflate(R.menu.menu_pop2, popMenu.menu)
            popMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item!!.itemId) {

                        R.id.checkPortugal -> {
                            "Portugal"
                            bi.isVisible = true
                            nif.isVisible = true
                            nif.isFocusableInTouchMode = true
                            licencaP.setVisibility(View.VISIBLE)
                            e.setVisibility(View.VISIBLE);

                            p.setVisibility(View.INVISIBLE);
                            dni.isInvisible = true
                            outros.isInvisible = true
                            passaporte.isInvisible = true
                            licencaE.setVisibility(View.INVISIBLE)


                            //Log.d("RegistoUser", "putas")

                            g = "Portugal"
                            e.setOnClickListener {
                                if (e.isChecked) {

                                    nomeSeguradoraExtra.setVisibility(View.VISIBLE)
                                    numApoliceExtra.setVisibility(View.VISIBLE)
                                    numCaca.setVisibility(View.VISIBLE)
                                    licencaEspanha.setVisibility(View.VISIBLE)

                                } else {

                                    licencaEspanha.setVisibility(View.INVISIBLE)
                                    nomeSeguradoraExtra.setVisibility(View.INVISIBLE)
                                    numApoliceExtra.setVisibility(View.INVISIBLE)
                                    numCaca.setVisibility(View.INVISIBLE)
                                    linceca.setVisibility(View.INVISIBLE)
                                }
                            }

                        }
                        R.id.checkOutros -> {
                            outros.isVisible = true
                            passaporte.isVisible = true
                            nif.isInvisible = true
                            dni.isInvisible = true
                            bi.isInvisible = true
                            numCaca.setVisibility(View.VISIBLE)



                            licencaEspanha.setVisibility(View.INVISIBLE)
                            licencaP.setVisibility(View.INVISIBLE)
                            licencaE.setVisibility(View.INVISIBLE)
                            p.setVisibility(View.INVISIBLE);
                            e.setVisibility(View.INVISIBLE);
                            Log.d("RegistoUser", "$g")
                        }
                        R.id.checkEspanha -> {
                            "Espanha"
                            dni.isVisible = true
                            bi.isInvisible = true
                            nif.isInvisible = true
                            outros.isInvisible = true
                            passaporte.isInvisible = true
                            licencaE.setVisibility(View.VISIBLE)
                            p.setVisibility(View.VISIBLE)
                            Log.d("RegistoUser", "putas2")




                            g = "Espanha"

                            licencaP.setVisibility(View.INVISIBLE)
                            e.setVisibility(View.INVISIBLE)

                            p.setOnClickListener {
                                if (p.isChecked) {
                                    nomeSeguradoraExtra.setVisibility(View.VISIBLE)
                                    numApoliceExtra.setVisibility(View.VISIBLE)
                                    numCaca.setVisibility(View.VISIBLE)
                                    linceca.setVisibility(View.VISIBLE)
                                } else {
                                    nomeSeguradoraExtra.setVisibility(View.INVISIBLE)
                                    numApoliceExtra.setVisibility(View.INVISIBLE)
                                    numCaca.setVisibility(View.INVISIBLE)
                                    linceca.setVisibility(View.INVISIBLE)
                                }
                            }
                        }
                    }
                    return true
                }

            })
            popMenu.show()
        }

        //portugal


        //outros
        passaporte.text.toString()
        g = outros.text.toString()

        //espanha
        dni.text.toString()
        licencaE.text.toString()

        buttonRegistar.setOnClickListener {


            val email = addEmail.text.toString()
            val password = addPass.text.toString()
            val name = addNome.text.toString()
            val tele = addTele.text.toString()
            val local = addLocalidade.text.toString()
            val morada = addMorada.text.toString()
            val postal = addPostal.text.toString()
            val cartaCaca = addCartaCaca.text.toString()
            val licencaArma = addLicencaArma.text.toString()
            val nomeSeguradora = addNomeSeguradora.text.toString()
            val numApolice = addNumeroApolice.text.toString()

            registoAuth(
                password,
                email,
                name,
                tele,
                morada,
                local,
                postal,
                cartaCaca,
                licencaArma,
                nomeSeguradora,
                numApolice,
                g,
                bi,
                nif,
                licencaP,
                passaporte,
                dni, licencaE,
                e,
                p,
                nomeSeguradoraExtra,
                numApoliceExtra,
                numCaca,
                linceca,
                licencaEspanha

            )


        }
    }


    private fun registoAuth(
        password: String,
        email: String,
        name: String,
        tele: String,
        morada: String,
        local: String,
        postal: String,
        cartaCaca: String,
        licencaArma: String,
        nomeSeguradora: String,
        numApolice: String,
        g: String,
        bi: EditText,
        nif: EditText,
        licencaP: EditText,
        passaporte: EditText,
        dni: EditText,
        licencaE: EditText,
        e: Switch,
        p: Switch,
        nomeSeguradoraExtra: EditText,
        numApoliceExtra: EditText,
        numCaca: EditText,
        linceca: EditText,
        licencaEspanha: EditText
    ) {

        val teste = arrayListOf<EditText>(
            addPass,
            addEmail,
            addNome,
            addTele,
            addLocalidade,
            addMorada,
            addPostal,
            addCartaCaca,
            addLicencaArma,
            addNomeSeguradora,
            addNumeroApolice
//                bi,
//                nif,
//                licencaP,
//                passaporte,
//                dni,
//                licencaE,
//                nomeSeguradoraExtra,
//                numApoliceExtra,
//                numCaca,
//                linceca,
//                licencaEspanha
        )



        if (verificaCampos(teste) == true) {

//
//        if (!password.isEmpty() && !email.isEmpty() && !name.isEmpty() && !tele.isEmpty() && !morada.isEmpty() && !local.isEmpty()
//            && !postal.isEmpty() && !cartaCaca.isEmpty() && !licencaArma.isEmpty() && !nomeSeguradora.isEmpty() && !numApolice.isEmpty()
//        ) {

            if (tele.length == 9 && postal.length == 7 && cartaCaca.length == 6 && licencaArma.length == 5 && numApolice.length == 10) {
                if (!g.isEmpty()) {


                    Auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { it ->

                            if (!it.isSuccessful) return@addOnCompleteListener

                            Log.d(
                                "RegistoUser",
                                "user auth com uid: ${it.result?.user?.uid}"
                            )
                            register(
                                name,
                                email,
                                tele,
                                morada,
                                local,
                                postal,
                                cartaCaca,
                                licencaArma,
                                nomeSeguradora,
                                numApolice,
                                g,
                                bi,
                                nif,
                                licencaP,
                                e,
                                p,
                                licencaEspanha,
                                nomeSeguradoraExtra,
                                numApoliceExtra,
                                numCaca,
                                linceca, dni, licencaE, passaporte
                            )
                            when {
                                it.isSuccessful -> {
                                    Toast.makeText(
                                        this,
                                        "Registo COM sucesso",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                                else -> {
                                    Toast.makeText(
                                        this,
                                        "Registo sem sucesso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }


                        }


                        .addOnFailureListener { exception: Exception ->
                            Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG)
                                .show()
                        }


                } else {
                    Toast.makeText(this, "Selecione um pais ", Toast.LENGTH_SHORT).show()
                }


////copiado para cada pais com as suas restricoes
//                Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { it ->
//
//                    if (!it.isSuccessful) return@addOnCompleteListener
//
//                    Log.d("RegistoUser", "user auth com uid: ${it.result?.user?.uid}")
//                    //register()
//                    when {
//                        it.isSuccessful -> {
//                            Toast.makeText(this, "Registo COM sucesso", Toast.LENGTH_SHORT).show()
//                            Auth.signOut()
//                        }
//                        else -> {
//                            Toast.makeText(this, "Registo sem sucesso", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//
//                }
//
//
//                    .addOnFailureListener { exception: Exception ->
//                        Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
//                    }
            } else {
                // Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
                Log.d("RegistoUser", "nao registo")
            }

        }
        //        else if (email.isEmpty()) {
//            addEmail.error = "This is error"
//        }
    }


    private fun register(
        name: String,
        email: String,
        tele: String,
        morada: String,
        local: String,
        postal: String,
        cartaCaca: String,
        licencaArma: String,
        nomeSeguradora: String,
        numApolice: String,
        g: String,
        bi: EditText,
        nif: EditText,
        licencaP: EditText,
        e: Switch,
        p: Switch,
        licencaEspanha: EditText,
        nomeSeguradoraExtra: EditText,
        numApoliceExtra: EditText,
        numCaca: EditText,
        linceca: EditText,
        dni: EditText,
        licencaE: EditText,
        passaporte: EditText
    ) {

        val uid = Auth.uid.toString()
//        val ref = mAuth.document("$uid")
//
        val NomeSeguradoraExtra = nomeSeguradoraExtra.text.toString()
        val NumApoliceExtra = numApoliceExtra.text.toString()
        val NumCaca = numCaca.text.toString()

        val pessoa = HashMap<String, Any>()
//        val pessoa: MutableMap<String, Any> = HashMap()

        pessoa["uid"] = uid
        pessoa["email"] = email
        pessoa["name"] = name
        pessoa["telemovel"] = tele
        pessoa["morada"] = morada
        pessoa["localidade"] = local
        pessoa["Codigo Postal"] = postal
        pessoa["Carta Caçadore"] = cartaCaca
        pessoa["Licença Arma"] = licencaArma
        pessoa["Nome Seguradora"] = nomeSeguradora
        pessoa["Numero Apolice"] = numApolice
        //pessoa["grupo"] = ArrayList<String>()
        pessoa["Pais"] = g
        pessoa["FirstTime"] = true
        pessoa["Org"] = false
        pessoa["Controlo"] = false

//        val pessoa = hashMapOf(
//            "uid" to uid,
//            "email" to email
//        )


        //mAuth.collection("Users").document("$uid").set(pessoa)


        if (g == "Portugal") {

            val Bi = bi.text.toString()
            val Nif = nif.text.toString()
            val LicencaP = licencaP.text.toString()
            val teste = arrayListOf<EditText>(
                bi,
                nif,
                licencaP
            )

            if (verificaCampos(teste) == true) {


                pessoa["BI"] = Bi
                pessoa["Nif"] = Nif
                pessoa["Licenca Portugal"] = LicencaP


                Log.d("RegistoUser", "user firestore registo1")

                val LicencaEspanha = licencaEspanha.text.toString()
                if (e.isChecked) {
                    val teste2 = arrayListOf<EditText>(

                        nomeSeguradoraExtra,
                        numApoliceExtra,
                        numCaca,
                        licencaEspanha
                    )

                    if (verificaCampos(teste2) == true) {


                        pessoa["Licenca Espanha"] = LicencaEspanha
                        pessoa["nome Seguradora Extra"] = NomeSeguradoraExtra
                        pessoa["numero Apolice Extra"] = NumApoliceExtra
                        pessoa["Numero Passaporte Europeu"] = NumCaca
                        mAuth.getReference("Users").child(uid).setValue(pessoa)

                        sendEmailVerification()
                        Log.d("RegistoUser", "email enviado")
                        clearInputs()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Auth.signOut()

                        Log.d("RegistoUser", "user firestore registo2")


                    }


                } else {
                    mAuth.getReference("Users").child(uid).setValue(pessoa)

                    sendEmailVerification()
                    Log.d("RegistoUser", "email enviado")
                    clearInputs()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                    Log.d("RegistoUser", "user firestore registo3")
                }
            } else {

                val user = Auth.currentUser
                user?.delete()
            }


        } else if (g == "Espanha") {

            val Dni = dni.text.toString()
            val LicencaE = licencaE.text.toString()
            val teste = arrayListOf<EditText>(
//                passaporte,
                dni,
                licencaE
//                nomeSeguradoraExtra,
//                numApoliceExtra,
//                numCaca,
//                linceca,
//                licencaEspanha
            )

            if (verificaCampos(teste) == true) {

                pessoa["Dni"] = Dni
                pessoa["Licenca Espanha"] = LicencaE
                Log.d("RegistoUser", "user firestore registo4")

                val LicencaPortuguesa = linceca.text.toString()


//

                if (p.isChecked) {
                    val teste2 = arrayListOf<EditText>(

                        nomeSeguradoraExtra,
                        numApoliceExtra,
                        numCaca,
                        linceca
                    )
                    if (verificaCampos(teste2) == true) {

                        pessoa["Licenca Portugal"] = LicencaPortuguesa
                        pessoa["nome Seguradora Extra"] = NomeSeguradoraExtra
                        pessoa["numero Apolice Extra"] = NumApoliceExtra
                        pessoa["Numero Passaporte Europeu"] = NumCaca
                        mAuth.getReference(uid).setValue(pessoa)


                        sendEmailVerification()
                        Log.d("RegistoUser", "email enviado")
                        clearInputs()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Auth.signOut()


                        Log.d("RegistoUser", "user firestore registo5")
                    }
                } else {
                    mAuth.getReference(uid).setValue(pessoa)

                    sendEmailVerification()
                    Log.d("RegistoUser", "email enviado")
                    clearInputs()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    Auth.signOut()

                    Log.d("RegistoUser", "user firestore registo6")
                }
            } else {

                val user = Auth.currentUser
                user?.delete()
            }
        } else {

            val Passaporte = passaporte.text.toString()
            if (!Passaporte.isEmpty()) {

                pessoa["Passaporte"] = Passaporte
                mAuth.getReference(uid).setValue(pessoa)

                sendEmailVerification()
                Log.d("RegistoUser", "email enviado")
                clearInputs()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Auth.signOut()


                Log.d("RegistoUser", "user firestore registo7")

            }

        }


    }


    private fun sendEmailVerification() {
        val user = Auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Email Verfication")
            builder.setMessage("Please confirm email")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(
                    applicationContext,
                    android.R.string.yes, Toast.LENGTH_SHORT
                )
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
                cont++;
            }
        }
        if (cont == 0) {
            valido = true
        }
        return valido
    }


    private fun clearInputs() {
        addNome.text.clear()
        addEmail.text.clear()
        addPass.text.clear()
    }


}

