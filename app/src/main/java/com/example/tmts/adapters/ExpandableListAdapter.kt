package com.example.tmts.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.beans.MovieDetails

class ExpandableListAdapter(
    private val context: Context,
    private val expandableListTitle: List<String>,
    private val expandableListDetail: HashMap<String, List<String>>

) : BaseExpandableListAdapter() {



    init {
        // Prendere la lista di Movies Visti da FirebaseInteraction
        expandableListDetail["Film Visti"] = listOf("Film1", "Film2")
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return expandableListDetail[expandableListTitle[groupPosition]]?.size?.plus(1) ?: 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return expandableListTitle[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return if (childPosition == expandableListDetail[expandableListTitle[groupPosition]]?.size) {
            "Add"
        } else {
            expandableListDetail[expandableListTitle[groupPosition]]!![childPosition]
        }
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val listTitle = getGroup(groupPosition) as String
        if (convertView == null) {
            val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.expandable_list_group,null)
        }
        val listTitleTextView = convertView!!.findViewById<TextView>(R.id.expandable_list_title)
        listTitleTextView.text = listTitle
        return convertView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val expandedListText = getChild(groupPosition, childPosition) as String
        val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        convertView = layoutInflater.inflate(R.layout.expandable_list_item,null)

        val expandedListTextView = convertView!!.findViewById<TextView>(R.id.expandable_list_item)
        val buttonAdd = convertView.findViewById<Button>(R.id.btn_add_elv_item)

        if (childPosition == expandableListDetail[expandableListTitle[groupPosition]]?.size) {
            expandedListTextView.visibility = View.GONE
            buttonAdd.visibility = View.VISIBLE
            buttonAdd.setOnClickListener {
                // Gestisci il clic del bottone aggiungi
                Toast.makeText(context, "Add button clicked in group $groupPosition", Toast.LENGTH_SHORT).show()
                // Aggiungi la logica per aggiungere un nuovo elemento
            }
        } else {
            expandedListTextView.text = getChild(groupPosition, childPosition) as String
            expandedListTextView.visibility = View.VISIBLE
            buttonAdd.visibility = View.GONE
        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

}

