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



class MyListAdapter(var mCtx: Context, var resource:Int, var items: ArrayList<Model>)
    : ArrayAdapter<Model>( mCtx , resource , items ){

    val mAuth = FirebaseFirestore.getInstance()




    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val layoutInflater : LayoutInflater = LayoutInflater.from(mCtx)

        val view : View = layoutInflater.inflate(resource , null )
        val imageView : ImageView = view.findViewById(R.id.imageViewLogo)
        var textView : TextView = view.findViewById(R.id.editNome)
        var textView1 : TextView = view.findViewById(R.id.editNumero)


        var person : Model = items[position]

        Glide.with(this.context).load(person.photo).into(imageView)
        textView.text = person.title
        textView1.text = person.desc


        return view
    }



}