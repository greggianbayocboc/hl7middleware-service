package com.hisd3.utils.Dto

import com.hisd3.utils.hl7service.LabResultItemDTO
import java.util.HashMap

class LabResultDTO{
    var parameterData = Paramdata()
    var labResultsList : ArrayList<LabResultItemDTO>?=null
}

class Paramdata (
        var interpreter :String? = null,
        var interpreterId :String?=null,
        var observationrequest :String?=null
)