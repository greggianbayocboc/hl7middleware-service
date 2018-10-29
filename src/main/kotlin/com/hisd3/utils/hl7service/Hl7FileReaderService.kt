package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.app.Connection
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator
import java.io.*


class Hl7FileReaderService {

//    @Inject
//    internal var hl7MsgProcessor: Hl7MsgProcessor?=null
//
//    @Inject
//    internal var orderSlipRepository: OrderSlipRepository?=null
//
//    @Inject
//    internal var labResultItemRepository: LabResultItemRepository?=null

    fun readMessage(theFile: BufferedReader, theMetadata : Map<String, Any>?){

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

//        var inStream :FileReader? =null
//        var buffInStream :BufferedReader? =null

        var parser = context.getPipeParser()

            try {
//                 inStream = FileReader(theFile)
//                 buffInStream = BufferedReader(inStream)
                val iter = Hl7InputStreamMessageIterator(theFile)
                var conn: Connection? = null
                while (iter.hasNext()) {

                    var next = iter.next()
                   // hl7MsgProcessor?.processORU(next, theMetadata)
                    if (conn == null) {
                        val useTls = false // Should we use TLS/SSL?
                        conn = context.newClient("127.0.0.1", 22222, useTls)

                    }
                        try {
                            var initiator = conn?.initiator
                            var response = initiator?.sendAndReceive(next)
                            val responseString = parser.encode(response)
                            System.out.println("Printing Encoded Message: " + responseString)

                        } catch (e: IOException) {
                           throw IllegalArgumentException(e.message)
                           // throw HL7Exception(e)
                            System.out.println("Didn't send out this message!")
                            conn?.close()
                            conn = null
                        }
                    conn?.close()
                    conn = null
                }
                theFile.close()
               // filename.delete()

            } catch (e: IOException) {
                System.err.println("Missing file: "+e)

            }
    }
}