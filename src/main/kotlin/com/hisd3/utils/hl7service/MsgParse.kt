package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION
import ca.uhn.hl7v2.model.v25.message.ORU_R01
import ca.uhn.hl7v2.model.v25.segment.*
import com.google.gson.Gson
import com.hisd3.utils.Dto.LabResultDTO
import java.util.ArrayList
import java.util.HashMap

class MsgParse {

    fun msgToJson( msg : Message): String {


        val dataList = ArrayList<Any>()
        val params = HashMap<String, Any?>()
        var data = LabResultDTO()

        val oru = msg as ORU_R01

        val msh = getMSH(oru)
        val pid = getPID(oru)
        val pv1 = getPV1(oru)
        val orc = getORC(oru)
        val obrervation= getOBR(msg)

        var messageControlId = msh.getMessageControlID().getValue()
        var obr:OBR? = null

        var patientResult = oru.getPATIENT_RESULT()
        var numObr = patientResult.getORDER_OBSERVATIONReps()

        for (i in 0..numObr - 1) {

            var orderObs = patientResult.getORDER_OBSERVATION(i)
            obr = orderObs.getOBR()

            val comments = StringBuilder()

            val parent = obr.getParent() as ORU_R01_ORDER_OBSERVATION
            val totalNTEs = parent.nteReps
            for (iNTE in 0..totalNTEs - 1) {
                for (obxComment in parent.getNTE(iNTE).comment) {
                    if (comments.length > 0) {
                        comments.append(" ")
                    }
                    comments.append(obxComment.value)
                }
            }

            var numObs = orderObs.getOBSERVATIONReps()
            for (j in 0 until numObs) {

                var item = LabResultItemDTO()
                var obx = orderObs.getOBSERVATION(j).getOBX()

                var values = obx.getObx5_ObservationValue()

                if (obx.observationIdentifier.ce2_Text.value != null) {
                    item.fieldname = obx.observationIdentifier.ce2_Text.value
                }
                val itr = values.iterator()
                var itemValue = ""
                while (itr.hasNext()) {
                    val element = itr.next()
                    itemValue += element.data.toString()
                    //item.value=element.data.toString()
                    System.out.print(element.toString() + " ")
                }
                item.value = itemValue

                if (obx.units?.ce1_Identifier?.value != null) {
                    item.cu_units = obx.units.ce1_Identifier.value.toString() ?: ""
                    //item.cu_units = obxResults.obx6_Units?.ce1_Identifier?.value?.toString() ?: ""
                }
                item.cu_referencerange = obx?.referencesRange.toString()
                //System.out.println("item" + item)

                dataList.add(item)

            }

        }
        if(obr?.obr4_UniversalServiceIdentifier?.ce2_Text!=null){
                    var observation = obr.obr4_UniversalServiceIdentifier.ce2_Text.value
                   params.put("observationrequest",observation)
               }else {
                    var observation = "TEST"
                    params.put("observationrequest",observation)
                }

        var sendingFacilty = msh.msh4_SendingFacility.name
        var dob = pid.dateTimeOfBirth.time
        var patientFull = pid.getPatientName(0).givenName.value + " " + pid.getPatientName(0).familyName.surname.value
        var visitNumber = pv1.visitNumber.idNumber.value
        var patientId = pid.patientID.idNumber.value
        var primaryPhy = pv1.getReferringDoctor(0).givenName.value + " " + pv1.getReferringDoctor(0).familyName.surname.value
        var ward = pv1.assignedPatientLocation.bed.value ?: ""
        var resultInterpreter = obrervation?.principalResultInterpreter.nameOfPerson.cnn3_GivenName.value + obrervation?.principalResultInterpreter.nameOfPerson.cnn2_FamilyName.value
        var interpreterID = obrervation?.principalResultInterpreter.nameOfPerson.idNumber.value

        data.parameterData.interpreter = resultInterpreter
        data.parameterData.interpreterId = interpreterID
//        params.put("interpreterID",interpreterID)
//        params.put("responsibleobserver",resultInterpreter)
//        params.put("revenuecenter", sendingFacilty)
//        params.put("patientname", patientFull)
//        params.put("patientno", patientId)

//        data.parameterData = params
        data.labResultsList = dataList
        var gson = Gson()
    //    println(data)
        return gson.toJson(data)
    }

    private fun getMSH(oru: ORU_R01): MSH {
        return oru.msh
    }

    private fun getPID(oru: ORU_R01): PID {
        //return oru.response.patient.pid
        return oru.patienT_RESULT.patient.pid

    }

    private fun getPV1(oru: ORU_R01): PV1 {
        //return oru.response.patient.visit.pV1
        return oru.patienT_RESULT.patient.visit.pV1
    }

    private fun getORC(oru: ORU_R01): ORC {
        //return oru.response.ordeR_OBSERVATION.orc
        return oru.patienT_RESULT.ordeR_OBSERVATION.orc
    }

    private fun getOBX(oru: ORU_R01): OBX {
        return oru.patienT_RESULT.ordeR_OBSERVATION.observation.obx
    }
    private  fun getOBR(oru : ORU_R01): OBR{
        return oru.patienT_RESULT.ordeR_OBSERVATION.obr
    }
}