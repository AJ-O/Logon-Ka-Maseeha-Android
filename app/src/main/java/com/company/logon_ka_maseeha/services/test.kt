package com.company.logon_ka_maseeha.services
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface test {

    @GET("testCall")
    fun testFun(): Call<List<testData>>

    @POST("sendMailToNgo")
    fun sendMail(@Body ngoDistances: List<Double>): Call<String>
}