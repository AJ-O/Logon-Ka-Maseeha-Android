package com.company.logon_ka_maseeha

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val listItems: ArrayList<ListItem>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(listItems[position])
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindItems(listItem: ListItem) {
            val productType = itemView.findViewById(R.id.donated_item_type) as TextView
            val productStatus = itemView.findViewById(R.id.donated_item_status) as TextView
            val donatedDate = itemView.findViewById(R.id.donated_date) as TextView
            val userMobileNo = itemView.findViewById(R.id.donated_item_mno) as TextView
            val donatedImg = itemView.findViewById(R.id.donated_image) as ImageView

            donatedImg.setImageBitmap(Bitmap.createScaledBitmap(listItem.bmp, 200, 200, false))
            productType.text = listItem.productType
            productStatus.text = listItem.productStatus
            donatedDate.text = listItem.donateDate.toString()
            userMobileNo.text = listItem.mobileNo.toString()
        }
    }
}