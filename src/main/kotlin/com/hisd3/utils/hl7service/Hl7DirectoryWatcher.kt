package com.hisd3.utils.hl7service

import com.hisd3.utils.Dto.ArgDto
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import java.io.*
import java.nio.file.*
import java.nio.file.WatchEvent
import java.nio.file.FileSystems
import java.nio.file.WatchKey
import java.nio.file.StandardWatchEventKinds.*


class Hl7DirectoryWatcher {

//    @Inject
//    internal var hl7FileReader: Hl7FileReaderService?=null
//
//
//    @Inject
//    internal var hl7configrepository: Hl7ConfigRepository?=null
    private fun Path.watch() : WatchService {
        //Create a watch service
        val watchService = this.fileSystem.newWatchService()

        //Register the service, specifying which events to watch
        register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW, StandardWatchEventKinds.ENTRY_DELETE)

        //Return the watch service
        return watchService
    }

    fun startDirWatching( args:ArgDto) {

       System.out.println("Start HL7 Directory Watcher")

       //var globalHl7Config = hl7configrepository?.findGlobalConfigEnabled()?.firstOrNull()

//       var ntlmPasswordAuthentication:NtlmPasswordAuthentication;


       var ntlmPasswordAuthentication = NtlmPasswordAuthentication(null,args.smbUser, args.smbPass)
      //var ntlmPasswordAuthentication = NtlmPasswordAuthentication("172.16.10.9","lisuser","p@ssw0rd")
        val path = "smb://"+args.smbHost+"/hl7host/Result/"
       val smbpath = args.smbUrl+"/Result/"
       val sFile = SmbFile(smbpath, ntlmPasswordAuthentication)
       val paths =sFile.uncPath

        if(sFile.list().isNotEmpty()){
            System.out.println("Directory is not empty!")
            fileScrapper(sFile,paths)
        }


       System.out.println(paths)

        //val myDir = Paths.get("c:/Shared")
       val myDir = Paths.get(paths)

       try {
           val watcher = FileSystems.getDefault().newWatchService()
           var key = myDir.register(watcher,
           ENTRY_CREATE,
           ENTRY_DELETE,
           ENTRY_MODIFY)
           while(true){
               //The watcher blocks until an event is available
               val key = watcher.take()
               System.out.println("watching ================= ")
               //Now go through each event on the folder
               key.pollEvents().forEach { it ->
                   //Print output according to the event
                   when(it.kind().name()){
                       "ENTRY_CREATE" -> {
                                 var filetoread = File(myDir.toString()+"/"+it.context().toString())
                                 //val child = myDir.resolve(filename)
                                   println("${filetoread} was created")
                                 //  val file = File(child.toString())
                                    fileScrapper(sFile,paths)
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

   fun fileScrapper ( sFile :SmbFile,paths :String){

            sFile.list().forEach {
                var filename = File(paths+it.toString())
                System.out.println("New FIle: "+filename)

                var inStream :FileReader? =null
                var buffInStream :BufferedReader? =null

                try{
                    inStream = FileReader(filename)
                    buffInStream = BufferedReader(inStream)

                   Hl7FileReaderService().readMessage(buffInStream, null)
                   filename.delete()
                   System.err.println("Deleted: "+ filename)
                }catch (x : IOException){
//                    throw IllegalArgumentException(x.message)
                    System.err.println("error: "+ x)
                }
                finally {
                    inStream?.close()
                    buffInStream?.close()

                }
            }

       return
   }
}