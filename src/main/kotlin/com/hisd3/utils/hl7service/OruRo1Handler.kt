package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.AcknowledgmentCode
import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION
import ca.uhn.hl7v2.model.v25.message.ACK
import ca.uhn.hl7v2.model.v25.message.ORU_R01
import ca.uhn.hl7v2.model.v25.segment.*
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.parser.PipeParser
import ca.uhn.hl7v2.protocol.ReceivingApplication
import ca.uhn.hl7v2.protocol.ReceivingApplicationException
import ca.uhn.hl7v2.util.Terser
import com.google.gson.Gson
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.Sockets.TutorialSocket
import com.hisd3.utils.Sockets.WSocketChatHandler
import com.hisd3.utils.httpservice.HttpSenderToHis
import org.apache.commons.lang3.StringUtils
import java.net.URI

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient



class Msgformat{
     var msgXML:String?=""
     var senderIp:String?=""
     var orderslipId:String?=null
     var pId:String?=null
     var jsonList:String? = null
     var casenum:String?=null
     var docEmpId:String?=null
     var attachment:String?=null
     var bacthnum:String?=null
     var processCode:String?=null
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

    var argument = ArgDto()
    var socks = TutorialSocket()
    constructor(arges:ArgDto,socket:TutorialSocket)
    {
        argument = arges
        socks = socket
    }

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
     override fun processMessage(theMessage: Message?, theMetadata: MutableMap<String, Any>?): Message? {

        var gson = Gson()
        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)
        //println("Received message:\n")
       // System.out.println("Meta=>" + theMetadata)
       // val pgen = context.getGenericParser()
       // val hapiMsg = pgen.parse(theMessage.toString())

        val parser = context.pipeParser

        val terser = Terser(theMessage)
        var zdcOrig :String? = null
        var zdc :String? = null

        try{

//          zdc =  terser.get("/.ZDC(0)-4")
            zdcOrig = StringUtils.substring(
                    theMessage.toString(),
                    StringUtils.indexOf(theMessage.toString(),"ZDC"),
                    theMessage.toString().length
            )

            zdc =  StringUtils.remove(zdcOrig,"ZDC|0|PDF|")
            zdc=StringUtils.trim(zdc)
            //println("ZDC:" +zdc)
        }catch (e:Exception){

          println(e)
        }
       // println("ZDC:" +zdc.toString())


        var str = StringUtils.remove(theMessage.toString(),zdcOrig)
        var xmlparser = context.getXMLParser()
        var encodedMessage = xmlparser.encode(theMessage)
        var msg = parser.parse(str) as ca.uhn.hl7v2.model.v25.message.ORU_R01

        val msh= getMSH(msg)
        val pid = getPID(msg)
        val pv1 = getPV1(msg)
        val obr= getOBR(msg)
        val te2 =Terser(msg)
        //Getting the orderslip number located in the visit number

        var visitNumber = pv1.visitNumber.idNumber.value?:""
        var terserpId = te2.get("/.PID-3")
        var patientid = pid.patientIdentifierList[0].idNumber.value

        val casenum = pv1.visitNumber.idNumber.value?:""
        var doctorEmpId = obr.principalResultInterpreter.nameOfPerson.idNumber.value?:""

        var orc = getORC(msg)
        val messageControlId = msh.messageControlID.value?:""
        val accession = orc.fillerOrderNumber.entityIdentifier.value ?:orc.placerOrderNumber.entityIdentifier.value?:""
        val orderID = obr.placerOrderNumber.entityIdentifier.value?:obr.fillerOrderNumber.entityIdentifier.value
        //val accession = obr.fillerOrderNumber.entityIdentifier.value
        // Getting the sender IP
        var sender = theMetadata!!.get("SENDING_IP")

        try {
            println("trying socket messaging")
            socks.onText(null,"crisnil")
        }catch (e: Exception){
            e.printStackTrace()
        }
        //Parsing xml to json

        val params =  Msgformat()

        //params.msgXML=encodedMessage
        params.attachment = zdc?:null
        params.msgXML=str
        params.senderIp= sender.toString()
        params.bacthnum=orderID?:""
        params.processCode=obr.universalServiceIdentifier.ce1_Identifier.value
        params.casenum = casenum?:""
        params.pId = patientid ?: terserpId
        params.docEmpId = doctorEmpId?:""
        params.jsonList = MsgParse().msgToJson(theMessage!!)
        var ack: Message
        ack = try {
            HttpSenderToHis().postToHis(params, argument)
            theMessage.generateACK()
        }catch (e: HL7Exception){
            e.printStackTrace()
            theMessage.generateACK(AcknowledgmentCode.AE, HL7Exception(e))
        }
//        parsingXml(encodedMessage)
        return ack
    }

//    @Async
//    open fun postToHis(params :Msgformat){
//        val post = HttpPost(argument.hisd3Host+":"+argument.hisd3Port+"/restapi/msgreceiver/hl7postResult")
////      val post = HttpPost("http://127.0.0.1:8080/restapi/msgreceiver/hl7postResult")
//
//        //val auth = "admin" + ":" + "7yq7d&addL$4CAAD"
//        val auth = argument.hisd3USer + ":" + argument.hisd3Pass
//        val encodedAuth = Base64.encodeBase64(
//                auth.toByteArray(Charset.forName("ISO-8859-1")))
//        val authHeader = "Basic " + String(encodedAuth)
//        post.setHeader(HttpHeaders.AUTHORIZATION, authHeader)
//        val httpclient = HttpClientBuilder.create().build()
//
//        post.setHeader(HttpHeaders.CONTENT_TYPE,"application/json")
//        val gson = Gson()
//        post.entity = StringEntity(gson.toJson(params))
//        try{
//            var response = httpclient.execute(post)
//        }catch (e:Exception)
//        {throw e}
//
//    }

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
    private  fun getOBR(oru : ORU_R01): OBR{
        return oru.patienT_RESULT.ordeR_OBSERVATION.obr
    }
}