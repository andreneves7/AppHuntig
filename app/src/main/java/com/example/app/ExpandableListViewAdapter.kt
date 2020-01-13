package com.example.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast


class ExpandableListViewAdapter (var context: Context, var expandableListView: ExpandableListView, var title: MutableList<String>,
                                 var subTitle: MutableList<MutableList<String>>): BaseExpandableListAdapter(){
    override fun getGroup(groupPosition: Int): String {
        return title[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if(convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.title_layout,null)
        }
        val title = convertView?.findViewById<TextView>(R.id.lv_title)
        title?.text = getGroup(groupPosition)
        title?.setOnClickListener {
            if(expandableListView.isGroupExpanded(groupPosition))
                expandableListView.collapseGroup(groupPosition)
            else
                expandableListView.expandGroup(groupPosition)
            Toast.makeText(context, getGroup(groupPosition),Toast.LENGTH_SHORT).show()
        }
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return subTitle[groupPosition].size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return subTitle[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if(convertView == null){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.subtitle_layout,null)
        }
        val title = convertView?.findViewById<TextView>(R.id.lv_subtitle)
        title?.text = getChild(groupPosition,childPosition)
        title?.setOnClickListener {
            Toast.makeText(context, getChild(groupPosition,childPosition),Toast.LENGTH_SHORT).show()
        }
        return convertView
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return title.size
    }

}