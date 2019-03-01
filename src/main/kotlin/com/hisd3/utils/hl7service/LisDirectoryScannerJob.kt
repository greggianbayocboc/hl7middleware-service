package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.llp.HL7Reader
import ca.uhn.hl7v2.util.Hl7InputStreamMessageIterator
import com.hisd3.utils.Dto.ArgDto
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import org.apache.commons.io.FileUtils
import org.omg.CORBA.Environment
import org.quartz.*
import sun.invoke.empty.Empty
import java.io.*


class LisDirectoryScannerJob : Job {
    @Throws(JobExecutionException::class)
    override fun execute(arg0: JobExecutionContext) {

        val dataMap =arg0.getMergedJobDataMap()
        var smbUser = dataMap.getString("user")
        var smbPass = dataMap.getString("pass")
        var smbUrl = dataMap.getString("smb")
        var host = dataMap.getString("host")

        println("Executing LIS Scanning Job")
        var auth = NtlmPasswordAuthentication(null,smbUser, smbPass)
        val smbpath = smbUrl+"/Result/"
        var sFile : SmbFile
        try {
            sFile = SmbFile(smbpath, auth)
                try {
                    System.out.println("Checking Unread Messages")
                    if (sFile.listFiles().isNotEmpty() ) {
                        println("\n " + sFile.list().toString())
                        sFile.listFiles().forEach {
                            if(!it.isDirectory){
                                try {
                                    var inFile = SmbFileInputStream(it)
                                   if(Hl7FileReaderService().readMessage(inFile, null)) {
                                        it.delete()

                                   }else{

                                       var newPath = SmbFile(smbpath+"/UNMATCH/"+it.name, auth)
                                       it.copyTo (newPath)
                                       it.delete()
                                   }
                                } catch (e: IOException) {

                                   e.printStackTrace()
                                }
                            }else{
                                println("No unread messages")
                            }
                        }

                    } else {
                        System.out.println("Directory is empty!")
                    }
                }   catch (e: Exception) {
                    e.printStackTrace()
                    }

        }catch (e:Exception){
            //println("Smb Connection "+ e)
            throw e
        }
    }

}