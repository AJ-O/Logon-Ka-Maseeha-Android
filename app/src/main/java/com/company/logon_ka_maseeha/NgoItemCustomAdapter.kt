package com.company.logon_ka_maseeha

import android.view.LayoutInflater
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NgoItemCustomAdapter(private val donatedItems: ArrayList<NgoItemDisplayData>): RecyclerView.Adapter<NgoItemCustomAdapter.ViewHolder> (){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NgoItemCustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.ngo_list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return donatedItems.size
    }

    override fun onBindViewHolder(holder: NgoItemCustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(donatedItems[position])
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindItems(donateItem: NgoItemDisplayData) {
            //Get the elements from ngo_list_layout
            val productType = itemView.findViewById(R.id.accepted_product_type) as TextView
            val itemAddress = itemView.findViewById(R.id.accepted_item_address) as TextView
            val productStatus = itemView.findViewById(R.id.accepted_item_status) as TextView
            val mobileNo = itemView.findViewById(R.id.accepted_item_mno) as TextView
            val donatedDate = itemView.findViewById(R.id.accepted_item_donated_date) as TextView
            val itemImg = itemView.findViewById(R.id.accepted_item_img) as ImageView

            //Set the values of those elements
            productType.text = donateItem.productType
            itemAddress.text = donateItem.itemAddress
            productStatus.text = donateItem.productStatus
            mobileNo.text = donateItem.mobileNo.toString()
            donatedDate.text = donateItem.donatedDate.toString()
            itemImg.setImageBitmap(Bitmap.createScaledBitmap(donateItem.bmp, 150, 200, false))
        }
    }
}