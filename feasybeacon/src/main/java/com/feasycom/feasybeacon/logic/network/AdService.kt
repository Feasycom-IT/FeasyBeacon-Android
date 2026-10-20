package com.feasycom.feasybeacon.logic.network

import com.feasycom.feasybeacon.logic.model.AdParams
import com.feasycom.feasybeacon.logic.model.AdResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AdService {

    @POST("lanch/ad/ad")
    suspend fun getAdData(@Body adParams: AdParams): AdResponse

}