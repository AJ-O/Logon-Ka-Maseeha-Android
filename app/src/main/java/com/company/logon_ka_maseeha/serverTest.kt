package com.company.logon_ka_maseeha

import android.util.Log
import android.widget.Toast
import com.company.logon_ka_maseeha.services.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private fun getCallTest() {

    val TAG = "DocSnippets"
    val testService: test = testBuilder.buildService(test::class.java)
    val requestCall: Call<List<testData>> = testService.testFun()

    requestCall.enqueue(object : Callback<List<testData>> {
        override fun onResponse(call: Call<List<testData>>, response: Response<List<testData>>) {
            Log.i(TAG, "Kind of got response")
            if (response.isSuccessful) {
                val retData: List<testData> = response.body()!!
                for (value in retData) {
                    Log.i(TAG, value.name.toString())
                    Log.i(TAG, value.pass.toString())
                }
            }
        }

        override fun onFailure(call: Call<List<testData>>, t: Throwable) {
            Log.i(TAG, "Failed response")
            //Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
        }
    })
}

private fun postCallTest(){

    val TAG = "DocSnippets"
    val ngoDistancesList: HashMap<String, Double> = hashMapOf("ash" to 3.0, "ish" to 5.0)
    val mailService: ServerRequests = ServiceBuilder.buildService(ServerRequests::class.java)
    val requestCall: Call<MailSuccessResponse> = mailService.sendMail(ngoDistancesList)
    //TODO Check for values expected for return vs sending

    requestCall.enqueue(object: Callback<MailSuccessResponse> {
        override fun onResponse(call: Call<MailSuccessResponse>, response: Response<MailSuccessResponse>){
            if(response.isSuccessful) {
                Log.i(TAG, "Sent Data!")
                Log.i(TAG, response.body()!!.toString())
            }
        }
        override fun onFailure(call: Call<MailSuccessResponse>, t: Throwable) {
            Log.i(TAG, "${t.message}")
        }
    })
}