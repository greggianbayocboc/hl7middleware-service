package com.hisd3.utils.httpservice

import com.google.gson.Gson
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.hl7service.Msgformat
import com.mchange.v1.util.ClosableResource
import org.apache.commons.codec.binary.Base64
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import java.nio.charset.Charset


@Async
open class HttpSenderToHis {


    fun postToHis(params: Msgformat, argument: ArgDto): ResponseEntity<String>? {
        val post = HttpPost(argument.hisd3Host + "/restapi/msgreceiver/hl7postResult")
//      val post = HttpPost("http://127.0.0.1:8080/restapi/msgreceiver/hl7postResult")

        //val auth = "admin" + ":" + "7yq7d&addL$4CAAD"
        val auth = argument.hisd3USer + ":" + argument.hisd3Pass
        val encodedAuth = Base64.encodeBase64(
                auth.toByteArray(Charset.forName("ISO-8859-1")))
        val authHeader = "Basic " + String(encodedAuth)
        post.setHeader(HttpHeaders.AUTHORIZATION, authHeader)
        val httpclient = HttpClientBuilder.create().build()

        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        val gson = Gson()
        post.entity = StringEntity(gson.toJson(params))
        try {
            println("Sending to HIS")
            var response = httpclient.execute(post)
            println("Response from HISD3 :" + response.statusLine.statusCode)
        } catch (e: Exception) {

            e.printStackTrace()
            throw e
        }

        val responseHeaders = org.springframework.http.HttpHeaders()
        return ResponseEntity("Message Receive", responseHeaders, HttpStatus.OK)
    }

    fun testPostToHis(argument: ArgDto):String?{
        println("initiate test post")

        val httpclient = HttpClients.custom().build()
        val post = HttpPost(argument.hisd3Host + "/restapi/msgreceiver/testpost")
        var params = Msgformat()
        params.senderIp = "hl7middleware"
        params.processCode ="testing post"

        val auth = argument.hisd3USer + ":" + argument.hisd3Pass
        val encodedAuth = Base64.encodeBase64(
                auth.toByteArray(Charset.forName("ISO-8859-1")))
        val authHeader = "Basic " + String(encodedAuth)

        post.setHeader(HttpHeaders.AUTHORIZATION, authHeader)
        post.setHeader(HttpHeaders.ACCEPT,"application/json")
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")

        val gson = Gson()
        post.entity = StringEntity(gson.toJson(params))
        var res :String? =null
        try {
            println("Sending to HIS")
            var response = httpclient.execute(post)
            println("Response from HISD3 :" + response.statusLine.statusCode)
            res = EntityUtils.toString(response.getEntity())
        } catch (e: Exception) {

            e.printStackTrace()
            post.releaseConnection()
            throw e
        }
        post.releaseConnection()
        return res
    }
}
