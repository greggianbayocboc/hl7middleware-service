package com.hisd3.utils.customwathcer

import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.hl7service.Hl7FileReaderService
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import java.io.BufferedInputStream
import java.io.IOException


/**
 *
 * Runs the SMB directory notifier. Specify the SMB parameters as command-line
 * arguments.
 *
 * @author Ryan Beckett
 */
class SmbNotifier {

    private var notifier: SmbNotification? = null
    private var auth: NtlmPasswordAuthentication? = null
    private var handler: NotificationHandler? = null

    fun run(url: String, domain: String?, user: String, pass: String) {
        //val userInfo = "$domain;$user:$pass"
        auth = NtlmPasswordAuthentication("",user,pass)
        var path = url+"/Result/"
        handler = object : NotificationHandler() {

            override fun handleNewFile(file: String) {
                println("$file added.")
                //Hl7FileReaderService().readSMb(file,auth,url)

                val url = url+"/Result/"+file
                val forprocess = SmbFile(url, auth)
                    if(!forprocess.isDirectory){
                        try {
                            var inFile = SmbFileInputStream(forprocess)
                            //var bMess = BufferedInputStream(inFile)
                            Hl7FileReaderService().readMessage(inFile, null)
                            forprocess.delete()
                            println("File read "+ inFile.toString())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }else{
                        println("File Created is not a message")
                    }
                }

            override fun handleDeletedFile(file: String) {
                println("$file deleted.")
            }

        }
        try {
            notifier = SmbNotification(path, auth, handler)
            notifier!!.listen(500)
            while (true)
            ;
            // modify the SMB directory and watch the console!!
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

        fun notify(args: ArgDto) {
            if (args!=null) {
                SmbNotifier().run(args.smbUrl!!, null, args.smbUser!!, args.smbPass!!)
            }
        }


}