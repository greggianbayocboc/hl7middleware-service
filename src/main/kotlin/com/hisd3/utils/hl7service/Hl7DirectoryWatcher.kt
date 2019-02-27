package com.hisd3.utils.hl7service

import com.hisd3.utils.Dto.ArgDto
import jcifs.smb.NtlmPasswordAuthentication

import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import java.io.*
import java.nio.file.*



class Hl7DirectoryWatcher {

    private fun Path.watch() : WatchService {
        //Create a watch service


        val watchService = this.fileSystem.newWatchService()

        //Register the service, specifying which events to watch
        register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_DELETE)

        //Return the watch service
        return watchService
    }


    fun startDirWatching( args:ArgDto) {
        var auth = NtlmPasswordAuthentication(null,args.smbUser, args.smbPass)
        val smbpath = args.smbUrl+"/Result/"
        var sFile :SmbFile
        sFile = SmbFile(smbpath, auth)
//        try{
//            System.out.println("Checking Unread Messages")
//            if(sFile.list().isNotEmpty()){
//                System.out.println("Directory is not empty!")
//                fileScrapper(sFile,smbpath,auth)
//            }
//        }catch (e: Exception){
//            throw e
//        }

       try {
           System.out.println("Start HL7 Directory Watcher")

//           val myDir = Paths.get("c:/Shared")
           sFile?.connect()
           val paths =sFile?.uncPath

           System.out.println(paths)

           val myDir = Paths.get(paths)

             val watcher = myDir.watch()
               while(true){
               //The watcher blocks until an event is available
               val key = watcher.take()
               System.out.println("watching ================= ")
               //Now go through each event on the folder
               key.pollEvents().forEach { it ->
                   //Print output according to the event
                   when(it.kind().name()){
                       "ENTRY_CREATE" -> {
                           println("${it.context()} was created")
                           val url = args.smbUrl+"/Result/"+it.context()
                           val forprocess = SmbFile(url, auth)
                               try {
                                  var inFile = BufferedReader(InputStreamReader(SmbFileInputStream(forprocess)))

                                  if(Hl7FileReaderService().readMessage(inFile, null)!!){
                                      forprocess.delete()
                                  }

                               } catch (e: IOException) {
                                   println("error parsing" + e)
                               }
                       }
                       "ENTRY_MODIFY" -> println("${it.context()} was modified")
                       "OVERFLOW" -> println("${it.context()} overflow")
                       "ENTRY_DELETE" -> println("${it.context()} was deleted")
                   }
               }
               //Call reset() on the key to watch for future events
               key.reset()

           }

   } catch (x: IOException) {
        System.err.println("watcher error: "+ x)
    }
   }

   fun fileScrapper ( sFile :SmbFile,smbpath:String,auth :NtlmPasswordAuthentication){

            sFile.list().forEach {
                val url = smbpath + it
                val forprocess = SmbFile(url, auth)

                try {
                    var inFile = BufferedReader(InputStreamReader(SmbFileInputStream(forprocess)))
                    if(Hl7FileReaderService().readMessage(inFile, null)!!){
                        forprocess.delete()
                    }
                } catch (e: IOException) {
                    println("error parsing" + e)
                }
            }

       return
   }
}