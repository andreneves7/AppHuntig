package com.example.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyListAdapter_ListaSocios (private var mCtx: Context,private var resource: Int,private var items: ArrayList<Model>) :
    ArrayAdapter<Model>(mCtx, resource, items){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater: LayoutInflater = LayoutInflater.from(mCtx)

        val view: View = layoutInflater.inflate(resource, null)

        val nome: TextView = view.findViewById(R.id.labelNome)

        val numeroSocio: TextView = view.findViewById(R.id.labelNumeroSocio)



        val person: Model = getItem(position)

//        nome.text = "Nome: " + person.Nome
        nome.text = mCtx.getString(R.string.nome, person.Nome)

        numeroSocio.text = mCtx.getString(R.string.num_socio, person.NumeroSocio.toString())



        return view
    }

    override fun getItem(position: Int): Model {
        return items[position]
    }

}