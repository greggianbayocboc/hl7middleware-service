package com.hisd3.utils.hl7service

import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import java.io.IOException
import java.nio.file.*
import java.nio.file.WatchEvent
import java.nio.file.FileSystems
import java.nio.file.WatchKey
import java.nio.file.StandardWatchEventKinds.*
import java.io.File


class Hl7DirectoryWatcher {

//    @Inject
//    internal var hl7FileReader: Hl7FileReaderService?=null
//
//
//    @Inject
//    internal var hl7configrepository: Hl7ConfigRepository?=null

    fun startDirWatching() {
       System.out.println("Start HL7 Directory Watcher")

       //var globalHl7Config = hl7configrepository?.findGlobalConfigEnabled()?.firstOrNull()

       var ntlmPasswordAuthentication = NtlmPasswordAuthentication("127.0.0.1","Administrator", "ZEAlot007!")

       val path = "smb://127.0.0.1/Shared"

       val sFile = SmbFile(path, ntlmPasswordAuthentication)
       val paths =sFile.uncPath
       System.out.println(paths)
//       val myDir = Paths.get("c:/Shared")
       val myDir = Paths.get(paths)
        System.out.println(myDir)
       try {
           val watcher = FileSystems.getDefault().newWatchService()
           var key = myDir.register(watcher,
           ENTRY_CREATE,
           ENTRY_DELETE,
           ENTRY_MODIFY)

       while (true) {
           // wait for key to be signaled
           System.out.println("watching ================= ")
           val key: WatchKey
           try {
               key = watcher.take()
           } catch (x: InterruptedException) {
               return
           }

           for (event in key.pollEvents()) {
               val kind = event.kind()

               // The filename is the
               // context of the event.
               val ev = event as WatchEvent<Path>
               val filename = ev.context()
               // This key is registered only
               // for ENTRY_CREATE events,
               // but an OVERFLOW event can
               // occur regardless if events
               // are lost or discarded.
               if (kind === OVERFLOW) {
                   continue
               }
               else if (ENTRY_CREATE == kind) {
                   // A new Path was created
                   val child = myDir.resolve(filename)
                   // Output
                   System.out.println("New path created: " + child)

                   val file = File(child.toString())
                    try{
                        Hl7FileReaderService().readMessage(file, null)

                    }catch (x : IOException){
                        System.err.println("error: "+ x)
                    }
               }
           }
           // Reset the key -- this step is critical if you want to
           // receive further watch events.  If the key is no longer valid,
           // the directory is inaccessible so exit the loop.
           val valid = key.reset()
           if (!valid) {
               break
           }
       }
   } catch (x: IOException) {
        System.err.println("error: "+ x)
    }
   }
}