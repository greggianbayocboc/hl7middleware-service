package com.hisd3.utils.hl7service

import com.hisd3.utils.Dto.ArgDto
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import org.apache.commons.io.FileUtils
import org.quartz.*
import sun.invoke.empty.Empty
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


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
                    if (sFile.list().isNotEmpty()) {
                        System.out.println("Directory is not empty!")
                        sFile.list().forEach {

                            val url = smbpath + it
                            val forprocess = SmbFile(url, auth)

                            try {
                                var inFile = BufferedReader(InputStreamReader(SmbFileInputStream(forprocess)))
                                var res = Hl7FileReaderService().readMessage(inFile, null)!!
                                if ( res == true) {
                                    println("response: "+ res)
                                    forprocess.delete()
                                }
                                else{
                                    println("false res: " + res)
                                   var newPath = SmbFile(smbpath+"/UNMATCH/"+forprocess.name, auth)
                                   forprocess.copyTo (newPath)
                                   forprocess.delete()
                                }
                            } catch (e: IOException) {

                               e.printStackTrace()

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