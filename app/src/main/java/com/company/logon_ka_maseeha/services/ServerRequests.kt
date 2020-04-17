package com.company.logon_ka_maseeha.services

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface ServerRequests {

    //@Body converts data sent as parameters to JSON
    @POST("sendMailToNgo")
    fun sendMail(@Body ngoDistances: List<Double>): Call<MailSuccessResponse>
}