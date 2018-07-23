package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v23.message.ORU_R01
import ca.uhn.hl7v2.model.v23.segment.MSH
import ca.uhn.hl7v2.model.v23.segment.ORC
import ca.uhn.hl7v2.model.v23.segment.PID
import ca.uhn.hl7v2.model.v23.segment.PV1
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.protocol.ReceivingApplication
import ca.uhn.hl7v2.protocol.ReceivingApplicationException
import java.io.IOException

class OrmOru<E> : ReceivingApplication<Message> {

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

        System.out.println("Meta=>" + theMetadata)

        val p = context.getGenericParser()

        var xmlparser = context.getXMLParser()
        var encodedMessage = xmlparser.encode(theMessage)

        var msg = p.parse(encodedMessage) as ca.uhn.hl7v2.model.v23.message.ORU_R01

        val msh = getMSH(msg)
        val pid = getPID(msg)
        val pv1 = getPV1(msg)
        //val nk1List = getNK1List(msg)
        var visitNumber = pv1.pv119_VisitNumber.id.value
        var patientId = pid.pid2_PatientIDExternalID.id.value

        var orc = getORC(msg)

        val messageControlId = msh.messageControlID.value


//        var patientNumber:String? = null
//        patientNumber = patientId.getIDNumber().getValue()
//
//        var encounterId: String? = null
//        encounterId = visitNumber.getIDNumber().getValue()


        System.out.println("Sender =>" )
        System.out.println("Visit number =>" + visitNumber)

        var sender = theMetadata.get("SENDING_IP")
//        val newHL7Result = LabResultItem()
//        val orderspecific = orderSlipRepository?.findOrderSlipByOrderno(visitNumber)?.lastOrNull()
//        if (orderspecific != null){
//            orderspecific.status = OrderSlipStatus.COMPLETED
//            orderSlipRepository?.save(orderspecific)
//            newHL7Result.orderSlip = orderspecific
//            newHL7Result.hl7Msg = encodedMessage
//            newHL7Result.patientNo = patientId
//            newHL7Result.sender= sender?.toString()
//            labResultItemRepository?.save(newHL7Result)
//        }else{
//            newHL7Result.hl7Msg = encodedMessage
//            newHL7Result.patientNo = patientId
//            newHL7Result.sender= sender?.toString()
//            labResultItemRepository?.save(newHL7Result)
//        }
        // Now generate a simple acknowledgment message and return it
        try {
            var acknowlege = theMessage.generateACK()
            return acknowlege


        } catch (e: IOException) {
            throw HL7Exception(e)
        }

    }

//    @Throws(HL7Exception::class)
//    fun getNK1List(oru: ORU_R01): List<NK1> {
//        val res = ArrayList<NK1>()
//        // there will always be at least one NK1, even if the original message does not contain one
//        for (i in 0..oru.patienT_RESULT.patient.nK1Reps - 1) {
//            // if the setIDNK1 value is null, this NK1 is blank
//            if (oru.patienT_RESULT.patient.getNK1(i).setIDNK1.value != null) {
//                res.add(oru.patienT_RESULT.patient.getNK1(i))
//            }
//        }
//        return res
//    }

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


}