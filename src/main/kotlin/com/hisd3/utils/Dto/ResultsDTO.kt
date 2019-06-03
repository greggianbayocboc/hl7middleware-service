package com.hisd3.utils.Dto

import com.hisd3.utils.hl7service.LabResultItemDTO
import com.hisd3.utils.hl7service.NteDto
import java.util.HashMap

class LabResultDTO{
    var parameterData = Paramdata()
    var labResultsList : ArrayList<LabResultItemDTO>?=null
    var comments : ArrayList<NteDto>?=null
}

class Paramdata (
        var interpreter :String? = null,
        var interpreterId :String?=null,
        var processCoe :String?=null,
        var observationrequest :String?=null
)
