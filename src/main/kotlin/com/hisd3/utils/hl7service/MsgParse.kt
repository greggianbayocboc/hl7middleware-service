package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT
import ca.uhn.hl7v2.model.v25.message.ORU_R01
import ca.uhn.hl7v2.model.v25.segment.*
import ca.uhn.hl7v2.util.Terser
import com.google.gson.Gson
import com.hisd3.utils.Dto.LabResultDTO
import java.util.ArrayList
import java.util.HashMap

class MsgParse {

    fun msgToJson( msg : ORU_R01): String {


        val dataList = ArrayList<LabResultItemDTO>()
        var commentsDto = ArrayList<NteDto>()
        val params = HashMap<String, Any?>()
        var data = LabResultDTO()

        //val oru = msg as ORU_R01

        val msh = getMSH(msg)
        val pid = getPID(msg)
        val pv1 = getPV1(msg)
        val orc = getORC(msg)
        val obrervation= getOBR(msg)

        var messageControlId = msh.getMessageControlID().getValue()
        var obr:OBR? = null

        var patientResult = msg.getPATIENT_RESULT()
        var numObr = patientResult.getORDER_OBSERVATIONReps()

        for (i in 0..numObr - 1) {

            var orderObs = patientResult.getORDER_OBSERVATION(i)
            obr = orderObs.getOBR()

            val comments = StringBuilder()
            var comment1 = NteDto()
            val terser = Terser(msg)
            //val parent = obr.getParent() as ORU_R01_ORDER_OBSERVATION
           var parent = orderObs.getNTE()
            val totalNTEs = orderObs.nteReps

            for (iNTE in 0..totalNTEs - 1) {

                var  commentsval =  terser.get("/.OBSERVATION(${i})/NTE(${iNTE})-3")

//            var  commentsval = parent.comment.get(iNTE).
                if(commentsval !=null)
                comment1.comments = commentsval.toString().replace("\\.br\\","")
                else ""
//               println(parent.getComment(iNTE).value.toString())
 //               for (obxComment in parent.getComment(iNTE).value) {
//                    if (comments.length > 0) {
//                        comments.append(" ")
//                    }
//                    comments.append(obxComment)
 //                   comment1.comments = comments.toString()
                //}
                commentsDto.add(comment1)
            }

            var numObs = orderObs.getOBSERVATIONReps()
            for (j in 0 until numObs) {

                var item = LabResultItemDTO()
                var obx = orderObs.getOBSERVATION(j).getOBX()

                var values = obx.getObservationValue()

                if (obx.observationIdentifier.ce2_Text.value != null) {
                    item.fieldname = obx.observationIdentifier.ce2_Text.value
                }
                val itr = values.iterator()
                var itemValue = ""
                while (itr.hasNext()) {

                    val element = itr.next()
                    itemValue += element.data.toString()
                    //item.value=element.data.toString()
                    //System.out.print(element.toString() + " ")
                }

                item.value = itemValue

                if (obx.units?.ce1_Identifier?.value != null) {
                    item.cu_units = obx?.units?.ce1_Identifier?.value
                    //item.cu_units = obxResults.obx6_Units?.ce1_Identifier?.value?.toString() ?: ""
                }
                item.cu_referencerange = obx?.referencesRange?.value
                //System.out.println("item" + item)
                item.responsibleobserver = obx?.getObx16_ResponsibleObserver(0)?.familyName?.surname?.value
                dataList.add(item)

            }

        }



        var observation:String? = null
        if(obr?.universalServiceIdentifier?.ce2_Text!=null){
                     observation = obr.universalServiceIdentifier.ce1_Identifier.value+"-"+ obr.universalServiceIdentifier.ce2_Text.value
               }else {
                     observation = "TEST"

                }

        var sendingApplication = msh.sendingApplication.toString()
        var dob = pid.dateTimeOfBirth.time.value
        var patientFull = pid.getPatientName(0).givenName.value + " " + pid.getPatientName(0).familyName.surname.value
        var visitNumber = pv1.visitNumber.idNumber.value
        var patientId = pid.patientID.idNumber.value
        var primaryPhy = pv1.getReferringDoctor(0).givenName.value + " " + pv1.getReferringDoctor(0).familyName.surname.value
        var ward = pv1.assignedPatientLocation.bed.value ?: ""
        var resultInterpreter = obrervation?.principalResultInterpreter.nameOfPerson.cnn3_GivenName.value + obrervation?.principalResultInterpreter.nameOfPerson.cnn2_FamilyName.value?:""
        var interpreterID = obrervation?.principalResultInterpreter.nameOfPerson.idNumber.value?:""

        data.parameterData.processCoe = ""
        data.parameterData.observationrequest = observation?:""
        data.parameterData.interpreter = resultInterpreter?:""
        data.parameterData.interpreterId = interpreterID?:""

//        params.put("interpreterID",interpreterID)
//        params.put("responsibleobserver",resultInterpreter)
//        params.put("revenuecenter", sendingFacilty)
//        params.put("patientname", patientFull)
//        params.put("patientno", patientId)

//        data.parameterData = params
        data.labResultsList = dataList
        data.comments = commentsDto
        var gson = Gson()
        //println(data)
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
