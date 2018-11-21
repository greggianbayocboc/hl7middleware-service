package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.AcknowledgmentCode
import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.app.Connection
import ca.uhn.hl7v2.model.v25.message.ACK
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator
import jcifs.smb.SmbFileInputStream
import java.io.*


class Hl7FileReaderService {

    fun readMessage(theFile: SmbFileInputStream, theMetadata : Map<String, Any>?): Boolean? {

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var parser = context.getPipeParser()

            try {
                val iter = Hl7InputStreamMessageIterator(theFile)
                var conn: Connection? = null
                while (iter.hasNext()) {

                    if (conn == null) {
                        val useTls = false // Should we use TLS/SSL?
                        conn = context.newClient("127.0.0.1", 22222, useTls)
                    }
                        try {
                            val next = iter.next()
                            var initiator = conn?.initiator
                            var response = initiator?.sendAndReceive(next)
                            if (response is ACK) {

//                                val ack = response as ACK
                                var ackcode = response.getMSA().acknowledgmentCode.value

                                if (ackcode != "AA") {
                                    theFile.close()
                                    return false
                                }
                            }
                            theFile.close()
                            return true
                            println(response.toString())

                        } catch (e: IOException) {
                           //throw IllegalArgumentException(e.message)
                           // throw HL7Exception(e)
                            System.out.println("Didn't send out this message!")
                            conn?.close()
                            return false
                        }
//                    conn?.close()
//                    conn = null
                }
            } catch (e: IOException) {
                System.err.println("Missing file: "+e)
            }
        return null
    }
}