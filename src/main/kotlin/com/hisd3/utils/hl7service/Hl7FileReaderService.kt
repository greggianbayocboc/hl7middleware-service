package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.AcknowledgmentCode
import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.app.Connection
import ca.uhn.hl7v2.model.v25.message.ACK
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import java.io.*
import java.io.BufferedInputStream




class Hl7FileReaderService {

    fun readMessage(theFile: SmbFileInputStream, theMetadata : Map<String, Any>?): Boolean {

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var parser = context.getPipeParser()

        var bool :Boolean? = false
                val iter = Hl7InputStreamMessageIterator(theFile)
                var conn: Connection? = null

                while (iter.hasNext()) {
                    if (conn == null) {
                        val useTls = false // Should we use TLS/SSL?
                        conn = context.newClient("127.0.0.1", 22222, useTls)
                    }
                     val next = iter.next()

                    println("message : "+ next.toString())
                     var initiator = conn?.initiator
                     try {
                         var response = initiator?.sendAndReceive(next)
                         println("response : "+ response)
                         if (response is ACK) {
                             var ackcode = response.getMSA().acknowledgmentCode.value
                             println(ackcode)
                             if (ackcode == "AA") {
                                 bool = true
                             }
                         }
                         conn?.close()
                     }catch (e: Exception){
                         e.printStackTrace()
                     }
                   conn?.close()
                   conn = null
                }
        theFile.close()
        return bool!!
    }

    fun readSMb(filename: String, auth: NtlmPasswordAuthentication?,url:String):Boolean{

        val url = url+"/Result/"+filename
        val forprocess = SmbFile(url, auth)
            if(!forprocess.isDirectory){
                try {
                    var inFile = SmbFileInputStream(forprocess)
                    //var bMess = BufferedInputStream(inFile)
                    readMessage(inFile, null)
                    //forprocess.delete()
                    println("File read "+ forprocess.toString())
                } catch (e: IOException) {

                    e.printStackTrace()
                }
            }else{
                println("File Created is not a message")
            }

        return true
    }
}