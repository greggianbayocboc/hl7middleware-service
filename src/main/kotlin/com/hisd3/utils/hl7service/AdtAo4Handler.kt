package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.AcknowledgmentCode
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.protocol.ReceivingApplication
import ca.uhn.hl7v2.protocol.ReceivingApplicationException
import com.hisd3.utils.httpservice.HttpSenderToHis


class AdtAo4Handler<E> : ReceivingApplication<Message> {

    override fun canProcess(theIn: Message): Boolean {
        return true
    }

    @Throws(ReceivingApplicationException::class, HL7Exception::class)
    override fun processMessage(theMessage: Message?, theMetadata: MutableMap<String, Any>?): Message? {
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
