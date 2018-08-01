package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v23.group.ORU_R01_ORDER_OBSERVATION
import ca.uhn.hl7v2.model.v23.message.ORU_R01
import ca.uhn.hl7v2.model.v23.segment.*
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.protocol.ReceivingApplication
import ca.uhn.hl7v2.protocol.ReceivingApplicationException
import com.google.gson.Gson
import com.hisd3.utils.Dto.ResultsDTO
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.IOUtils
import org.apache.http.HttpHeaders
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Years
import java.io.IOException
import java.nio.charset.Charset
import java.util.HashMap

class Msgformat{
     var msgXML:String?=""
     var senderIp:String?=""
     var orderslipId:String?=""
     var pId:String?=""
     var dataList:String?=null
}

class LabResultItemDTO {

    var testname: String=""

    var show: Boolean?=false

    var header: Boolean?=false

    var showheader : Boolean?=null

    var disabled: Boolean?=false

    var cu_referencerange: String?=""

    var cu_units: String?=""

    var flags: String?=""

    var fieldname: String?=""

    var type: String?=""

    var date: String?=""

    var value: String=""

    var stValue:String?=""

    var body:String?=""

    var responsibleobserver:String?=""

}

class OruRo1Handler<E> : ReceivingApplication<Message> {

    /**
     * {@inheritDoc}
     */
    override fun canProcess(theIn: Message): Boolean {
        return true
    }

    /**
     * {@inheritDoc}
     */
    @Throws(ReceivingApplicationException::class, HL7Exception::class)
    override fun processMessage(theMessage: Message, theMetadata: Map<String, Any>): Message {

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.3")
        context.setModelClassFactory(mcf)
        println("Received message:\n")

        //System.out.println("Meta=>" + theMetadata)

        val p = context.getGenericParser()

        var xmlparser = context.getXMLParser()
        var encodedMessage = xmlparser.encode(theMessage)

        //Getting the orderslip number located in the visit number

        var msg = p.parse(encodedMessage) as ca.uhn.hl7v2.model.v23.message.ORU_R01
        val msh= getMSH(msg)
        val pid = getPID(msg)
        val pv1 = getPV1(msg)

        var visitNumber = pv1.pv119_VisitNumber.id.value
        var pId = pid.getPid3_PatientIDInternalID(0).id.value.toString()
        var patientId = pid.pid2_PatientIDExternalID.id.value.toString()


        // Getting the sender IP
        var sender = theMetadata.get("SENDING_IP")


        val post = HttpPost("http://localhost:8080/restapi/msgreceiver/hl7postResult")

        val auth = "admin" + ":" + "7yq7d&addL$4CAAD"
        val encodedAuth = Base64.encodeBase64(
                auth.toByteArray(Charset.forName("ISO-8859-1")))
        val authHeader = "Basic " + String(encodedAuth)
        post.setHeader(HttpHeaders.AUTHORIZATION, authHeader)
        val httpclient = HttpClientBuilder.create().build()
        val params =  Msgformat()
        var gson = Gson()
        params.msgXML=encodedMessage
        params.senderIp= sender.toString()
        params.orderslipId=visitNumber
        params.pId=pId?:patientId
        params.dataList = this.processMsg(encodedMessage)

        post.setHeader(HttpHeaders.CONTENT_TYPE,"application/json")


        post.entity = StringEntity(gson.toJson(params))
        try{
            var response = httpclient.execute(post)
           // println(response.statusLine.statusCode)
        }
        catch (e: IOException){
            e.printStackTrace()
        }


       // System.out.println(IOUtils.toString(post.entity.content))

        try {
            var acknowlege = theMessage.generateACK()
            return acknowlege


        } catch (e: IOException) {
            throw HL7Exception(e)
        }

    }

    private fun processMsg(xmlData : String?) :String{

        val dataList = ArrayList<Any>()
        val params = HashMap<String, Any?>()
        var data = ResultsDTO()

        if(xmlData.isNullOrEmpty()){

            var item = LabResultItemDTO()
            item.fieldname = "No Results From Provider "

            dataList.add(item)
        }else{

            var context = DefaultHapiContext()
            var mcf = CanonicalModelClassFactory("2.3")
            context.setModelClassFactory(mcf)

            val p = context.getGenericParser()

            var msg = p.parse(xmlData) as ca.uhn.hl7v2.model.v23.message.ORU_R01

            val msh = getMSH(msg)
            val pid = getPID(msg)
            val pv1 = getPV1(msg)
           // val obx = getOBX(msg)


            // val nk1List = getNK1List(msg)
            //var oberver = obx
            var dob = pid.dateOfBirth
            var patientFull = pid.pid5_PatientName
            var visitNumber = pv1.pv119_VisitNumber.id.value
            var patientId = pid.pid2_PatientIDExternalID.id.value
           var primaryPhy = pv1.getReferringDoctor(0).givenName.value+" "+pv1.getReferringDoctor(0).familyName.value
            var ward = pv1.assignedPatientLocation.bed.value

            val patientResult = msg.response
            val numObr = patientResult.ordeR_OBSERVATIONReps

            /** forming data for pdf**/
            val orderObs = patientResult.getORDER_OBSERVATION(0)

            val obr = orderObs.obr
            var requestedDate =  obr.requestedDateTime.degreeOfPrecision.value

            val responsibleObserverID=obr.obr32_PrincipalResultInterpreter.opName.cn1_IDNumber.value?:""
            val responsibleObserverfname=obr.obr32_PrincipalResultInterpreter.opName.cn3_GivenName.value?:""
            val responsibleObserverLname=obr.obr32_PrincipalResultInterpreter.opName.cn2_FamilyName.value?:""
            val responsibleObserverAuth=obr.obr32_PrincipalResultInterpreter.opName.assigningAuthority.universalID.value?:""
            val principalResultObserver =obr.obr32_PrincipalResultInterpreter.toString()?:""

            params.put("responsibleobserver",responsibleObserverID+" "+responsibleObserverfname+" "+responsibleObserverLname+" "+responsibleObserverAuth)

            if(obr.obr4_UniversalServiceIdentifier.ce2_Text!=null){
                var observation = obr.obr4_UniversalServiceIdentifier.ce2_Text.value
                params.put("observationrequest",observation)
            }else {
                var observation = "TEST"
                params.put("observationrequest",observation)
            }

            for (i in 0..numObr - 1) {

                val orderObs = patientResult.getORDER_OBSERVATION(i)
                val obr = orderObs.obr
                //item.testname = obr.universalServiceIdentifier.identifier.value + obr.universalServiceIdentifier.ce2_Text.value

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

                val numObs = orderObs.observationReps
                val errorInHL7Queue: HL7Exception? = null

                for (j in 0..numObs - 1) {
                    var item = LabResultItemDTO()

                    val obxResults = orderObs.getOBSERVATION(j).obx
                    var values = obxResults.getObx5_ObservationValue()

                    //  if(obxResults.obx2_ValueType.value == "ST"){
                    if(obxResults.obx3_ObservationIdentifier.ce2_Text != null){
                        item.fieldname=obxResults.obx3_ObservationIdentifier.ce2_Text.toString()
                    }
                    val itr = values.iterator()
                    var itemValue = ""
                    while (itr.hasNext()) {
                        val element = itr.next()
                        itemValue +=element.data.toString()
                        //item.value=element.data.toString()
                        System.out.print(element.toString() + " ")
                    }
                    item.value = itemValue

                    if(obxResults.obx6_Units?.ce1_Identifier?.value !=null) {
                        item.cu_units = obxResults.obx6_Units?.ce1_Identifier?.value?.toString() ?: ""
                    }
                    item.cu_referencerange = obxResults?.obx7_ReferencesRange?.value?.toString()?:""

                    dataList.add(item)
                }
            }

//            params.put("revenuecenter", msh.msh4_SendingFacility)
//            params.put("patientname",  patientFull)
//            params.put("patientno",patientId)
//           // params.put("age", Years.yearsBetween(DateTime.parse(dateDob.toString()), org.joda.time.DateTime.now()).years)
//            params.put("physician", primaryPhy )
//            params.put("ward", ward)
//            params.put("ancilliaryDoctor", principalResultObserver)
//            params.put("daterequested",requestedDate)

//            params.put("category", orderSlipResult?.category)
//            params.put("title",orderSlipResult?.revenuecenter?.toUpperCase())

        }

            data.parameterData=params
            data.labResultsList=dataList
        var gson = Gson()
        println(data)
        return gson.toJson(data)
    }

    private fun getMSH(oru: ORU_R01): MSH {
        return oru.msh
    }

    private fun getPID(oru: ORU_R01): PID {
        return oru.response.patient.pid
        //oru.patienT_RESULT.patient.pid

    }

    private fun getPV1(oru: ORU_R01): PV1 {
        return oru.response.patient.visit.pV1
        //oru.patienT_RESULT.patient.visit.pV1
    }

    private fun getORC(oru: ORU_R01): ORC {
        return oru.response.ordeR_OBSERVATION.orc
        //oru.patienT_RESULT.ordeR_OBSERVATION.orc
    }

//    private  fun getOBX(oru : ORU_R01): OBX{
//        return oru.response.getORDER_OBSERVATION(0).observation.obx
//    }


}