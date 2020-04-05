package com.company.logon_ka_maseeha

import android.view.LayoutInflater
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val TAG = "DocSnippets"
        fun bindItems(donateItem: NgoItemDisplayData) {
            //Get the elements from ngo_list_layout
            val productType = itemView.findViewById(R.id.accepted_product_type) as TextView
            val itemAddress = itemView.findViewById(R.id.accepted_item_address) as TextView
            val mobileNo = itemView.findViewById(R.id.accepted_item_mno) as TextView
            val donatedDate = itemView.findViewById(R.id.accepted_item_donated_date) as TextView
            val itemImg = itemView.findViewById(R.id.accepted_item_img) as ImageView
            val itemStatusBtn = itemView.findViewById(R.id.item_status_btn) as Button

            //Set the values of those elements
            val itemFirebaseId = donateItem.documentId
            val ngoEmail = donateItem.ngoEmail
            val itemStatus = donateItem.productStatus
            itemStatusBtn.text = itemStatus
            productType.text = donateItem.productType
            itemAddress.text = donateItem.itemAddress
            mobileNo.text = donateItem.mobileNo.toString()
            donatedDate.text = donateItem.donatedDate.toString()
            itemImg.setImageBitmap(Bitmap.createScaledBitmap(donateItem.bmp, 150, 200, false))

            itemStatusBtn.setOnClickListener(changeStatus(itemFirebaseId, ngoEmail, itemStatus))
        }

        private fun changeStatus(itemId: String, ngoEmail: String, itemStatus: String) : (View) -> Unit = {
            layoutPosition.also {
                val db = Firebase.firestore
                val docRef = db.collection("NGO").document(ngoEmail).collection("Items_List").document(itemId)
                val newStatus = getNextStatus(itemStatus)
                docRef.update("Items Status", newStatus).addOnSuccessListener {
                    Log.i(TAG, "Value Updated!")
                }.addOnFailureListener {
                    exception -> Log.i(TAG, "Error: ", exception)
                }
            }
        }

        private fun getNextStatus(itemStatus: String): String {
            if (itemStatus == "Accepted Items") {
                return "Item Collected"
            } else if (itemStatus == "Item Collected") {
                return "Item Donated"
            }
            return ""
        }
    }
}