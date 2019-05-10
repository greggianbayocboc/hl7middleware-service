package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.AcknowledgmentCode
import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.message.ORU_R01
import ca.uhn.hl7v2.model.v25.segment.*
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.protocol.ReceivingApplication
import ca.uhn.hl7v2.protocol.ReceivingApplicationException
import ca.uhn.hl7v2.util.Terser
import com.google.gson.Gson
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.Sockets.WebsocketClient
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
class NteDto{
    var comments :String? = ""
}

class OruRo1Handler<E> : ReceivingApplication<Message> {

    var argument = ArgDto()
    constructor(arges:ArgDto)
    {
        argument = arges

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
        val context = DefaultHapiContext()
        val mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        val parser = context.pipeParser


        //val terser = Terser(theMessage)
        var zdcOrig :String? = null
        var zdc :String? = null
        var str :String?= null
        try{

//          zdc =  terser.get("/.ZDC(0)-4")
            zdcOrig = StringUtils.substring(
                    theMessage.toString(),
                    StringUtils.indexOf(theMessage.toString(),"ZDC"),
                    theMessage.toString().length
            )

            zdc = StringUtils.remove(zdcOrig,"ZDC|0|PDF|")
            zdc = StringUtils.trim(zdc)

        }catch (e:Exception){

          println("No Attachement Found : \n" + e)
        }

        if(zdc !=null){
            str = StringUtils.remove(theMessage.toString(),zdcOrig)
            println("Found attachement at ZDC section")
        }else{
            str = theMessage.toString()
        }

        println("New message received:\n")
        println(str)
//        var xmlparser = context.getXMLParser()
//        var encodedMessage = xmlparser.encode(theMessage)
        val msg = parser.parse(theMessage.toString()) as ca.uhn.hl7v2.model.v25.message.ORU_R01

        val msh= getMSH(msg)
        val pid = getPID(msg)
        val pv1 = getPV1(msg)
        val obr= getOBR(msg)
        val te2 =Terser(msg)
        //Getting the orderslip number located in the visit number

        var visitNumber = pv1.visitNumber.idNumber.value?:""
        val terserpId = te2.get("/.PID-3")
        val patientid = pid.patientIdentifierList[0].idNumber.value

        val casenum = pv1.visitNumber.idNumber.value?:""
        val doctorEmpId = obr.principalResultInterpreter.nameOfPerson.idNumber.value?:""

        val orc = getORC(msg)
        val messageControlId = msh.messageControlID.value?:""
        val accession = orc.fillerOrderNumber.entityIdentifier.value ?:orc.placerOrderNumber.entityIdentifier.value?:""
        val batchnum = te2.get("/.OBR-2")?:te2.get("/.OBR-3")
        //val accession = obr.fillerOrderNumber.entityIdentifier.value
        // Getting the sender IP
        val sender = theMetadata!!.get("SENDING_IP")

//        val dest = "ws://localhost:4567/socketmessenging"
//        val client = WebSocketClient()
//        val socket = WebsocketClient()
//        try {
//            client.start()
//            val echoUri = URI(dest)
//            val request = ClientUpgradeRequest()
//            client.connect(socket, echoUri, request)
//            socket.sendMessage(str!!)
//            //Thread.sleep(1000L)
//        } catch (t: Throwable) {
//            t.printStackTrace()
//        } finally {
//            try {
//                client.stop()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

        val params =  Msgformat()

      //params.msgXML=encodedMessage
        params.attachment = zdc
        params.msgXML=str
        params.senderIp= sender.toString()
        params.bacthnum= batchnum
        params.processCode= obr.universalServiceIdentifier.ce1_Identifier.value
        params.casenum = casenum
        params.pId = terserpId?:patientid
        params.docEmpId = doctorEmpId
        params.jsonList = MsgParse().msgToJson(msg)
        val ack: Message
        ack = try {
               HttpSenderToHis().postToHis(params, argument)
               theMessage!!.generateACK()

        }catch (e: HL7Exception){
            e.printStackTrace()
            theMessage!!.generateACK(AcknowledgmentCode.AE, HL7Exception(e))
        }
//      parsingXml(encodedMessage)
        return ack
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
    private  fun getOBR(oru : ORU_R01): OBR{
        return oru.patienT_RESULT.ordeR_OBSERVATION.obr
    }
}