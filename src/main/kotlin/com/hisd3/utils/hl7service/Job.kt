package com.hisd3.utils.hl7service

import com.hisd3.utils.Dto.ArgDto
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import org.quartz.*
import java.io.IOException


class JobA : Job {
    @Throws(JobExecutionException::class)
    override fun execute(arg0: JobExecutionContext) {

        val dataMap =arg0.getMergedJobDataMap()
        var smbUser = dataMap.getString("user")
        var smbPass = dataMap.getString("pass")
        var smbUrl = dataMap.getString("smb")

        println("Executing LIS Scanning Job")
        var auth = NtlmPasswordAuthentication(null,smbUser, smbPass)
        val smbpath = smbUrl+"/Result/"
        var sFile : SmbFile
        sFile = SmbFile(smbpath, auth)

            try{
            System.out.println("Checking Unread Messages")
            if(sFile.list().isNotEmpty()){
                System.out.println("Directory is not empty!")
                sFile.list().forEach {
                    val url = smbpath + it
                    val forprocess = SmbFile(url, auth)

                    try {
                        var inFile = SmbFileInputStream(forprocess)
                        if(Hl7FileReaderService().readMessage(inFile, null)!!){
                            forprocess.delete()
                        }
                    } catch (e: IOException) {
                        println("error parsing" + e)
                    }
                }
            }else{ System.out.println("Directory is empty!")}
        }catch (e: Exception){
            throw e
        }
    }

}