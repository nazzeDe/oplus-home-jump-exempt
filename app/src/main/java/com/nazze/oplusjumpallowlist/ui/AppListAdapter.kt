package com.nazze.oplusjumpallowlist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.nazze.oplusjumpallowlist.R

class AppListAdapter(
    private val onCheckedChange: (packageName: String, checked: Boolean) -> Unit,
) : BaseAdapter() {
    private val items = mutableListOf<AppListItem>()
    private val selected = linkedSetOf<String>()

    fun submit(list: List<AppListItem>, selectedPackages: Set<String>) {
        items.clear()
        items.addAll(list)
        selected.clear()
        selected.addAll(selectedPackages)
        notifyDataSetChanged()
    }

    fun selectedPackages(): Set<String> = selected.toSet()

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): AppListItem = items[position]

    override fun getItemId(position: Int): Long = items[position].packageName.hashCode().toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        val holder = (view.tag as? Holder) ?: Holder(view).also { view.tag = it }
        val item = items[position]

        holder.icon.setImageDrawable(item.icon)
        holder.label.text = item.label
        holder.packageName.text = item.packageName

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.packageName in selected
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selected.add(item.packageName)
            } else {
                selected.remove(item.packageName)
            }
            onCheckedChange(item.packageName, isChecked)
        }

        view.setOnClickListener {
            holder.checkBox.toggle()
        }
        return view
    }

    private class Holder(view: View) {
        val checkBox: CheckBox = view.findViewById(R.id.app_checked)
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val label: TextView = view.findViewById(R.id.app_label)
        val packageName: TextView = view.findViewById(R.id.app_package)
    }
}
