package com.hisd3.utils.rest

import com.google.gson.GsonBuilder
import com.hisd3.utils.Dto.OrmMessageDto
import spark.Spark.post

class JsonReceiverV2 {
    val gson = GsonBuilder().disableHtmlEscaping().create()

    init{
        post("jsonmsg"){req,res ->

            if(!req.body()?.isEmpty()!!) {

                val data = req.body()
                val msgDto :OrmMessageDto  = gson.fromJson(data, OrmMessageDto::class.java)

                try {
                    when (msgDto.msg.messageCode) {
                        "ADT_A04" -> {
                            res.type("application/json")

                        }

                        "ORM_O01" -> {
                            res.type("application/json")

                        }
                        else ->"Sorry, Option not available yet"
                    }
                }catch (e:Exception){
                    throw IllegalArgumentException(e)
                    //                             res.status(500)
                    //                              res.body(e.message)

                    //println(e.message)
                    return@post Unit

                }
            }else {
                res.type("application/json")
                res.body("req.body is empty")
            }
//                        res.body()
//                        res.status(200)
//                        "ok"
        }
    }

}