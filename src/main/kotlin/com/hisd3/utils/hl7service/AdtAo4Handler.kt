package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.AcknowledgmentCode
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.protocol.ReceivingApplication
import ca.uhn.hl7v2.protocol.ReceivingApplicationException
import com.hisd3.utils.Sockets.WebsocketClient
import org.apache.commons.lang3.StringUtils
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI


class AdtAo4Handler<E> : ReceivingApplication<Message> {

    override fun canProcess(theIn: Message): Boolean {
        return true
    }

    @Throws(ReceivingApplicationException::class, HL7Exception::class)
    override fun processMessage(theMessage: Message?, theMetadata: MutableMap<String, Any>?): Message? {

        var zdcOrig :String? = null

        try{

//          zdc =  terser.get("/.ZDC(0)-4")
            zdcOrig = StringUtils.substring(
                    theMessage.toString(),
                    StringUtils.indexOf(theMessage.toString(),"ZDC"),
                    theMessage.toString().length
            )


        }catch (e:Exception){

            println(e)
        }

        val str = StringUtils.remove(theMessage.toString(),zdcOrig)
//        val dest = "ws://localhost:4567/tutorialsokcet"
//        val client = WebSocketClient()
//        try {
//            val socket = WebsocketClient()
//            client.start()
//            val echoUri = URI(dest)
//            val request = ClientUpgradeRequest()
//            client.connect(socket, echoUri, request)
//            socket.sendMessage(str)
//            //Thread.sleep(1000L)
//        } catch (t: Throwable) {
//            t.printStackTrace()
//        }
//        finally {
//            try {
//                client.stop()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }


        var ack: Message
        ack = try {
            theMessage!!.generateACK()


        }catch (e: HL7Exception){
            e.printStackTrace()
            theMessage!!.generateACK(AcknowledgmentCode.AE, HL7Exception(e))
        }

//        parsingXml(encodedMessage)
        return ack


    }
}
