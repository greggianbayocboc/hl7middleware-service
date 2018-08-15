package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION
import ca.uhn.hl7v2.model.v25.message.ORU_R01
import ca.uhn.hl7v2.model.v25.segment.*
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.protocol.ReceivingApplication
import ca.uhn.hl7v2.protocol.ReceivingApplicationException
import ca.uhn.hl7v2.util.Terser
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
import java.util.*

class Msgformat{
     var msgXML:String?=""
     var senderIp:String?=""
     var orderslipId:String?=""
     var pId:String?=""
     var jsonList:String? = null
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
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)
        println("Received message:\n")

       // System.out.println("Meta=>" + theMetadata)

        val p = context.getGenericParser()


        var xmlparser = context.getXMLParser()
        var encodedMessage = xmlparser.encode(theMessage)


        var msg = p.parse(encodedMessage) as ca.uhn.hl7v2.model.v25.message.ORU_R01

        val msh= getMSH(msg)
        val pid = getPID(msg)
        val pv1 = getPV1(msg)

        //Getting the orderslip number located in the visit number

        var visitNumber = pv1.visitNumber.idNumber.value
        var pId = pid.patientID.idNumber.value
        //var patientId = pid.pid2_PatientIDExternalID.id.value.toString()?:""

        var orc = getORC(msg)
        val messageControlId = msh.messageControlID.value

        // Getting the sender IP
        var sender = theMetadata.get("SENDING_IP")

        //Parsing xml to json

        val post = HttpPost("http://localhost:8080/restapi/msgreceiver/hl7postResult")

        val auth = "admin" + ":" + "7yq7d&addL$4CAAD"
        val encodedAuth = Base64.encodeBase64(
                auth.toByteArray(Charset.forName("ISO-8859-1")))
        val authHeader = "Basic " + String(encodedAuth)
        post.setHeader(HttpHeaders.AUTHORIZATION, authHeader)
        val httpclient = HttpClientBuilder.create().build()
        val params =  Msgformat()

        params.msgXML=encodedMessage
        params.senderIp= sender.toString()
        params.orderslipId=messageControlId
        params.pId=pId
        params.jsonList = MsgParse().msgToJson(theMessage)

        post.setHeader(HttpHeaders.CONTENT_TYPE,"application/json")

        var gson = Gson()
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

    private  fun getOBX(oru : ORU_R01): OBX{
        return oru.patienT_RESULT.ordeR_OBSERVATION.observation.obx
    }

}