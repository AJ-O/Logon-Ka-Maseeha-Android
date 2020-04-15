package com.company.logon_ka_maseeha.services
import retrofit2.Call
import retrofit2.http.GET

interface test {

    @GET("testCall")
    fun testFun(): Call<List<testData>>
}