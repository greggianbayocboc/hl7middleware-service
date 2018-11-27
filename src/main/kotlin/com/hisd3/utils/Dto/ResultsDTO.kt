package com.hisd3.utils.Dto

import java.util.HashMap

class LabResultDTO{
    var parameterData = Paramdata()
    var labResultsList : ArrayList<Any>?=null
}

class Paramdata (
        var interpreter :String? = null,
        var interpreterId :String?=null,
        var observationrequest :String?=null
)