package com.company.logon_ka_maseeha

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList

class DonatedItemCustomAdapter(private val donatedItems: ArrayList<NgoItemDisplayData>): RecyclerView.Adapter<DonatedItemCustomAdapter.ViewHolder> (){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonatedItemCustomAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.ngo_list_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return donatedItems.size
    }

    override fun onBindViewHolder(holder: DonatedItemCustomAdapter.ViewHolder, position: Int) {
        holder.bindItems(donatedItems[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val TAG = "DocSnippets"
        @SuppressLint("SetTextI18n")
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
            val itemStatus = donateItem.productStatus

            if(itemStatus == "Awaiting Response") {
                itemStatusBtn.text = "Accept Item"
            }
            productType.text = donateItem.productType
            itemAddress.text = donateItem.itemAddress
            mobileNo.text = donateItem.mobileNo.toString()
            donatedDate.text = donateItem.donatedDate.toString()
            itemImg.setImageBitmap(Bitmap.createScaledBitmap(donateItem.bmp, 150, 200, false))

            //ADU - Add. delete, update database
            itemStatusBtn.setOnClickListener(ADU(itemFirebaseId, donateItem))
        }

        //Add, Delete, Update
        private fun ADU(itemId: String, donateItem: NgoItemDisplayData) : (View) -> Unit = {
            layoutPosition.also {

                val documentFields = hashMapOf(
                    "Type" to donateItem.productType,
                    "Status" to "Item Accepted",
                    "Address" to donateItem.itemAddress,
                    "ImageName" to donateItem.imageName,
                    "Mobile_No" to donateItem.mobileNo,
                    "Uploaded By" to donateItem.userEmail,
                    "Timestamp" to donateItem.donatedDate
                )

                val db = Firebase.firestore
                //Set data for NGO
                db.collection("NGO").document(donateItem.ngoEmail).collection("Selected_Items").document(itemId).set(documentFields).addOnSuccessListener {
                    //Update data for the individual User
                    db.collection("Users").document(donateItem.userEmail).collection("Donated_Items").document(itemId).update("Status", "Item Accepted").addOnSuccessListener {
                        //Delete document from Items Donated collection
                        db.collection("Items_Donated").document(itemId).delete().addOnSuccessListener {
                            donatedItems.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                        }.addOnFailureListener {
                            //Toast.makeText(this, "Item added to Ngo database", Toast.LENGTH_LONG).show()
                                exception -> Log.i(TAG, "Error adding to database!", exception)
                        }
                    }.addOnFailureListener{
                        exception -> Log.i(TAG, "Error adding to users database", exception)
                    }
                }.addOnFailureListener{
                    exception -> Log.i(TAG, "Error adding to Ngo's database", exception)
                }
            }
        }
    }
}