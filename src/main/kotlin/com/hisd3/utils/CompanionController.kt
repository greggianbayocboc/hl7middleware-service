package com.hisd3.utils

import spark.Request
import spark.Response

class CompanionController {

    companion object {
        val controllOne = {req: Request,res: Response ->

            var data = req.queryParams("name")

            fun sample(args :String?,args2:String?){

            }
            "hello controller one " +data
        }

    }
}