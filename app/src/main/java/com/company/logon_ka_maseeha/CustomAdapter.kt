package com.company.logon_ka_maseeha

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.logon_ka_maseeha.UserPage.Companion.TAG
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CustomAdapter(private val listItems: ArrayList<ListItem>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>(){
    companion object {
        val db = Firebase.firestore
    }

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

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindItems(listItem: ListItem) {
            val productType = itemView.findViewById(R.id.donated_item_type) as TextView
            val productStatus = itemView.findViewById(R.id.donated_item_status) as TextView
            val donatedDate = itemView.findViewById(R.id.donated_date) as TextView
            val userMobileNo = itemView.findViewById(R.id.donated_item_mno) as TextView
            val donatedImg = itemView.findViewById(R.id.donated_image) as ImageView
            val rmItem = itemView.findViewById(R.id.remove_item) as Button

            donatedImg.setImageBitmap(Bitmap.createScaledBitmap(listItem.bmp, 200, 200, false))
            productType.text = listItem.productType
            productStatus.text = listItem.productStatus
            donatedDate.text = listItem.donateDate.toString()
            userMobileNo.text = listItem.mobileNo.toString()
            val delItemRef = listItem.fbItemUserRef
            val email = listItem.email

            donatedImg.setOnClickListener(increaseSize())

            if(listItem.productStatus == "Awaiting Response") {
                rmItem.setOnClickListener(remove(delItemRef, email))
            } else {
                rmItem.visibility = View.GONE
            }
        }

        private fun increaseSize(): (View) -> Unit = {
            layoutPosition.also {
                //TODO create popup!
                Log.i(TAG, "Need to create popups")
            }
        }

        private fun remove(delId: String, email: String): (View) -> Unit = {
            layoutPosition.also {

                db.collection("Users/$email/Donated_Items").document(delId).delete().addOnSuccessListener {
                    Log.i(TAG, "Item Deleted")
                    db.collection("Items_Donated").document(delId).delete().addOnSuccessListener {
                        Log.i(TAG, "Item Deleted from Items Donated collection")
                    }.addOnFailureListener{
                        Log.i(TAG, "Failed to delete in Items donated collection")
                    }
                }.addOnFailureListener {
                    exception -> Log.w(TAG, "Error -- ", exception)
                }

                listItems.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
            }
        }
    }
}