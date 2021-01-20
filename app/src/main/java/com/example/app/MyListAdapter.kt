package com.example.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore


class MyListAdapter(var mCtx: Context, var resource: Int, var items: ArrayList<Model>) :
    ArrayAdapter<Model>(mCtx, resource, items) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(resource, null)
        var nome: TextView = view.findViewById(R.id.labelNome)
        var numeroCC: TextView = view.findViewById(R.id.labelNumeroCC)
        var numeroSocio: TextView = view.findViewById(R.id.labelNumeroSocio)


        var person: Model = items[position]

        nome.text = "Nome: " + person.Nome
        numeroCC.text = "Nº Carta Caçador: " + person.NumeroCC.toString()
        numeroSocio.text = "Nº Socio: " + person.NumeroSocio.toString()


        return view
    }


}