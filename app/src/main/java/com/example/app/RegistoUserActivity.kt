package com.example.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registo_user.*
import java.util.*
import kotlin.collections.ArrayList

const val EXTRA_MESSAGE = "ola"

class RegistoUserActivity : AppCompatActivity() {

    val mAuth = FirebaseFirestore.getInstance().collection("Users")
    val gAuth = FirebaseFirestore.getInstance().collection("Grupos")
    val Auth = FirebaseAuth.getInstance()
    val mStorage = FirebaseStorage.getInstance().reference
    lateinit var gv: VariaveisGlobais
    val valor = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registo_user)
        gv = application as VariaveisGlobais


     formaAssociacoes()

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
        val outros = addPais_Outros
        val linceca = addLicencaCacaPortugal
        val nomeSeguradoraExtra = addNomeSeguradoraExtra
        val numApoliceExtra = addNumeroApoliceExtra
        val numCaca = addNumPassCaca
        val licencaP = editTextLicencaPortugal
        val passaporte = addNumero_Passaporte
        val dni = addDNI
        val bi = addCartao
        val nif = addNif



        val bEspanha = buttonAddicionar
        val b = button5
        val bPortugal = bAdd
        val btnPop = bPais_User

        val e = checkBoxEspanha
        val p = checkBoxPortugal

        val spinner = findViewById<Spinner>(R.id.spinner)
        val spinner2 = findViewById<Spinner>(R.id.spinnerAssociacoes)
        val spinner3 = findViewById<Spinner>(R.id.spinnerZonas)

        val putas2 = linearLayoutInfoAssPortugal
        val ZonasLicença = linearLayoutZonas
        val extraEspanha = linearLayoutInfoExtraEspanha
        val s = scrollView2
        val scrooll = scrollView3


        var g = ""


        nomeSeguradoraExtra.setVisibility(View.INVISIBLE)
        numApoliceExtra.setVisibility(View.INVISIBLE)
        numCaca.setVisibility(View.INVISIBLE)
        linceca.setVisibility(View.INVISIBLE)
        licencaP.setVisibility(View.INVISIBLE)
        putas2.setVisibility(View.INVISIBLE)
        spinner2.setVisibility(View.INVISIBLE)
        spinner3.setVisibility(View.INVISIBLE)
        ZonasLicença.setVisibility(View.INVISIBLE)
        e.setVisibility(View.INVISIBLE)
        p.setVisibility(View.INVISIBLE)
        spinner.setVisibility(View.INVISIBLE)
        extraEspanha.setVisibility(View.INVISIBLE)
        s.setVisibility(View.INVISIBLE)
        scrooll.setVisibility(View.INVISIBLE)

        outros.isInvisible = true
        dni.isInvisible = true
        bi.isInvisible = true
        nif.isInvisible = true
        passaporte.isInvisible = true


        b.setOnClickListener {
            extra()
        }


        bPortugal.setOnClickListener {
            lincecaPortugal()
        }

        bEspanha.setOnClickListener {
            licencaEspanha()
        }



        btnPop.setOnClickListener {

            val popMenu = PopupMenu(this@RegistoUserActivity, btnPop)
            popMenu.menuInflater.inflate(R.menu.menu_pop2, popMenu.menu)
            popMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item!!.itemId) {

                        R.id.checkPortugal -> {
                            "Portugal"
                            bi.isVisible = true
                            bi.text.toString()
                            nif.isVisible = true
                            nif.text.toString()
                            nif.isFocusableInTouchMode = true
                            dni.isInvisible = true
                            outros.isInvisible = true
                            passaporte.isInvisible = true
                            Log.d("RegistoUser", "putas")

                            g = "Portugal"

                            spinner2.setVisibility(View.VISIBLE)
                            putas2.setVisibility(View.VISIBLE)
                            spinner3.setVisibility(View.INVISIBLE)
                            ZonasLicença.setVisibility(View.INVISIBLE)
                            s.setVisibility(View.VISIBLE)
                            licencaP.setVisibility(View.VISIBLE)
                            licencaP.text.toString()
                            spinner2()


                            p.setVisibility(View.INVISIBLE);
                            e.setVisibility(View.VISIBLE);
                            e.setOnClickListener {
                                if (e.isChecked) {
                                    extraEspanha.setVisibility(View.VISIBLE);
                                    spinner.setVisibility(View.VISIBLE);
                                    s.setVisibility(View.VISIBLE)
                                    numCaca.setVisibility(View.VISIBLE)
                                    scrooll.setVisibility(View.VISIBLE)
                                    spinner()
                                } else {
                                    spinner.setVisibility(View.INVISIBLE)
                                    extraEspanha.setVisibility(View.INVISIBLE)
                                    s.setVisibility(View.INVISIBLE)
                                    nomeSeguradoraExtra.setVisibility(View.INVISIBLE)
                                    numApoliceExtra.setVisibility(View.INVISIBLE)
                                    numCaca.setVisibility(View.INVISIBLE)
                                    linceca.setVisibility(View.INVISIBLE)
                                    scrooll.setVisibility(View.INVISIBLE)
                                }
                            }

                        }
                        R.id.checkOutros -> {
                            outros.isVisible = true
                            passaporte.isVisible = true
                            passaporte.text.toString()
                            nif.isInvisible = true
                            dni.isInvisible = true
                            bi.isInvisible = true
                            numCaca.setVisibility(View.VISIBLE)
                            g = outros.text.toString()
                            Log.d("RegistoUser", "$g")
                        }
                        R.id.checkEspanha -> {
                            "Espanha"
                            dni.isVisible = true
                            dni.text.toString()
                            bi.isInvisible = true
                            nif.isInvisible = true
                            outros.isInvisible = true
                            passaporte.isInvisible = true
                            Log.d("RegistoUser", "putas2")

                            spinner2.setVisibility(View.INVISIBLE)
                            spinner3.setVisibility(View.VISIBLE)
                            ZonasLicença.setVisibility(View.VISIBLE)
                            licencaP.setVisibility(View.INVISIBLE)
                            putas2.setVisibility(View.INVISIBLE)
                            s.setVisibility(View.VISIBLE)
                            spinnerZonas()

                            g = "Espanha"

                            p.setVisibility(View.VISIBLE)
                            e.setVisibility(View.INVISIBLE)
                            spinner.setVisibility(View.INVISIBLE)
                            extraEspanha.setVisibility(View.INVISIBLE)
                            p.setOnClickListener {
                                if (p.isChecked) {
                                    nomeSeguradoraExtra.setVisibility(View.VISIBLE)
                                    numApoliceExtra.setVisibility(View.VISIBLE)
                                    numCaca.setVisibility(View.VISIBLE)
                                    linceca.setVisibility(View.VISIBLE)
                                } else {
                                    spinner.setVisibility(View.INVISIBLE)
                                    extraEspanha.setVisibility(View.INVISIBLE)
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


//        registoAuth(password, email, name)

    }

    fun formaAssociacoes(){
        val dados = gAuth
        dados.get().addOnSuccessListener { result ->
            if (result != null) {


                for (Asso in result) {
                    valor.add(Asso.get("nome").toString())
                }
            }
        }
    }


    fun licencaEspanha() {
        val numEspanha = editTextNumLicenca
        val ll = layoutVer
        val cb = TextView(this)
        val ct = TextView(this)

        val ld = LinearLayout(this)
        ld.setOrientation(LinearLayout.HORIZONTAL)

        val bt = Button(this)
        bt.setText("remove")

        if (numEspanha.text.toString() != "") {
            gv.numEspanha = numEspanha.text.toString().toInt()

            ll.addView(ld)
            cb.setText("Numero: " + "${gv.numEspanha}" + " ");
            ct.setText("Zona: " + "${gv.check}  ");
            ld.addView(ct)
            ld.addView(cb)
            ld.addView(bt)


        }

        bt.setOnClickListener {


            ll.removeView(ld)


        }
    }

    fun lincecaPortugal() {
        val ll = layoutVer
        val NumSocio = editTextNumSocio
        val cb = TextView(this)
        val ct = TextView(this)

        val ld = LinearLayout(this)
        ld.setOrientation(LinearLayout.HORIZONTAL)

        val bt = Button(this)
        bt.setText("remove")

        if (NumSocio.text.toString() != "") {
            gv.numSocio = NumSocio.text.toString().toInt()

            ll.addView(ld)
            cb.setText("Numero: " + "${gv.numSocio}" + " ");
            ct.setText("Associativa: " + "${gv.Associacao}  ");
            ld.addView(ct)
            ld.addView(cb)
            ld.addView(bt)


        }

        bt.setOnClickListener {


            ll.removeView(ld)


        }
    }


    fun extra() {
        val et = editTextExtra
        val ver = layoutVer2
        val cb = TextView(this)
        val ct = TextView(this)

        val ld = LinearLayout(this)
        ld.setOrientation(LinearLayout.HORIZONTAL)

        val bt = Button(this)
        bt.setText("remove")

        if (et.text.toString() != "") {

            ver.addView(ld)
            cb.setText("Numero: " + "${et.text.toString()}" + " ");
            ct.setText("Zona: " + "${gv.extra}  ");
            ld.addView(ct)
            ld.addView(cb)
            ld.addView(bt)


        }

        bt.setOnClickListener {


            ver.removeView(ld)


        }
    }


    fun spinner() {
        val languages = resources.getStringArray(R.array.zonas)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, languages
            )
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {

                    gv.extra = languages[position]

                    Log.d("testea", "${gv.extra}")

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }

            }

        }
    }


    fun spinner2() {

        if (spinnerAssociacoes != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, valor
            )
                .also { adapter ->

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerAssociacoes.adapter = adapter
                }

            spinnerAssociacoes.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {

                    gv.Associacao = valor[position]

                    Log.d("testeb", "${gv.Associacao}")

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }

            }

        }
    }

    fun spinnerZonas() {
        val languages = resources.getStringArray(R.array.zonas)
        if (spinnerZonas != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, languages
            )
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerZonas.adapter = adapter
                }

            spinnerZonas.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {

                    gv.extra = languages[position]

                    Log.d("testea", "${gv.extra}")

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }

            }

        }


    }


    private fun registoAuth(password: String, email: String, name: String) {

        //if (!password.isEmpty() && !email.isEmpty() && !name.isEmpty()) {
            Auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { it ->

                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegistoUser", "user auth com uid: ${it.result?.user?.uid}")
                register(name, email)
                when {
                    it.isSuccessful -> {
                        Toast.makeText(this, "Registo COM sucesso", Toast.LENGTH_SHORT).show()
                        Auth.signOut()
                    }
                    else -> {
                        Toast.makeText(this, "Registo sem sucesso", Toast.LENGTH_SHORT).show()
                    }
                }


            }


                .addOnFailureListener { exception: Exception ->
                    Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                }
//        } else {
//            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
//            Log.d("RegistoUser", "nao registo")
//        }

    }


    private fun register(name: String, email: String) {

        val uid = Auth.uid.toString()
        val ref = mAuth.document("$uid")

        val pessoa = HashMap<String, Any>()
        pessoa["uid"] = uid
        pessoa["name"] = name
        pessoa["email"] = email
        pessoa["grupo"] = ArrayList<String>()
        ref.set(pessoa)
        Log.d("RegistoUser", "user firestore registo")


        sendEmailVerification()
        Log.d("RegistoUser", "email enviado")
        clearInputs()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)


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


    private fun clearInputs() {
        addNome.text.clear()
        addEmail.text.clear()
        addPass.text.clear()
    }

    private fun validar(){
        val texto_erros = ""

    }

}

