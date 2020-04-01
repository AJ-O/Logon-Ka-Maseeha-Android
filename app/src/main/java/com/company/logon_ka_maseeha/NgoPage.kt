package com.company.logon_ka_maseeha

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_ngo_page.*

class NgoPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ngo_page)

        val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
        val globalNgoName = sharedPreferences.getString("ngoName", "")

        val ngoNameEle = ngoName as TextView
        ngoNameEle.text = globalNgoName

        ngoItems.setOnClickListener{
            val intent = Intent(this, NgoItems::class.java)
            startActivity(intent)
        }

        collections.setOnClickListener{
            val intent = Intent(this, DonatedItemLists::class.java)
            startActivity(intent)
        }
    }
}
