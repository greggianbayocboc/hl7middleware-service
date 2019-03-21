package com.hisd3.utils.customwathcer

import com.hisd3.utils.hl7service.Hl7FileReaderService
import java.net.MalformedURLException
import java.util.ArrayList
import java.util.Arrays
import java.util.logging.Level
import java.util.logging.Logger
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbException
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileInputStream
import java.io.IOException

/**
 *
 * A facade for receiving notifcations for SMB file modifications through the
 * JCIFS API.
 *
 * This class provides notification for file creation and deletion in a SMB/CIFS
 * directory. A callback is produced on modification and the modified file name
 * is given to the default handler.
 *
 * @author Ryan Beckett
 * @version 1.0
 */
class SmbNotification
/**
 * Creates a new non-running notifier.
 *
 * @param url
 * An smb directory path of the form
 * `smb://host/dir/`. See [SmbFile] for more
 * information on constructing URLs.
 *
 * @param authentication
 * The authentication information.
 *
 * @param handler
 * A default handler for notification callbacks.
 *
 * @throws IllegalArgumentException
 * If `authentication` or `handler` are
 * null.
 *
 * @throws MalformedURLException
 * If `dir` is a malformed URL.
 *
 * @throws SmbException
 * If an underlying communication error occurs.
 */
@Throws(IllegalArgumentException::class, MalformedURLException::class, SmbException::class)
constructor(url: String,
        /**
         * Get the authentication.
         *
         * @return The authentication.
         */
            /**
             * Set the authentication. Setting a new authentication will have no
             * effect. Once the notifier has made the connection, the user session
             * will persist throughout the notifier's life cycle.
             *
             * @param handler
             * The new authentication.
             */
            var authentication: NtlmPasswordAuthentication?,
        /**
         * Get the callback handler.
         *
         * @return The handler.
         */
            /**
             * Set the callback handler
             *
             * @param handler
             * The new handler.
             */
            var handler: NotificationHandler?) {
    private var smb: SmbFile? = null
    private val files: MutableList<String>
    private val logger: Logger
    /**
     * Check whether the notifier is running.
     *
     * @return Returns `true` if the notifier is running; otherwise,
     * returns `false`.
     */
    var isRunning: Boolean = false
        private set
    private var stopped: Boolean = false

    init {
        if (authentication == null)
            throw IllegalArgumentException()
        if (handler == null)
            throw IllegalArgumentException()
        logger = Logger.getLogger("SmbNotification")
        files = ArrayList()
        connect(url)
        createFileList()
    }

    @Throws(MalformedURLException::class)
    private fun connect(dir: String) {
        smb = SmbFile(dir, authentication)
        smb!!.listFiles().forEach {
            if(!it.isDirectory){
                println("on connect file found" +it.name)
                try {
                    var inFile = SmbFileInputStream(it)
                    //var bMess = BufferedInputStream(inFile)
                    Hl7FileReaderService().readMessage(inFile, null)
                    //forprocess.delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    }

    @Throws(SmbException::class)
    private fun createFileList() {
        for (f in smb!!.list()) {
            files.add(f)
        }
    }

    /**
     * Listen for modifications to the directory. A new thread is started that
     * polls for modifications every `millis` milliseconds. If the
     * notifier is running already, a new thread will not be created.
     *
     * @param millis
     * The number of milliseconds to wait per poll.
     *
     * @return Returns `true` if the notifier is started and a new
     * thread is spawned; otherwise, returns `false` if the
     * notifier is running already.
     *
     * @throws IllegalStateException
     * If `listen` is called after `close` is
     * called.
     */
    fun listen(millis: Long): Boolean {
        if (stopped)
            throw IllegalStateException("Cannot restart the notifier.")
        if (isRunning)
            return false
        isRunning = true
        val t = Thread(Runnable {
            while (isRunning) {
                try {
                    checkForFileDeletion()
                    checkForNewFile()
                    Thread.sleep(millis)
                } catch (e: SmbException) {
                    log(e.message!!, Level.SEVERE)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        })
        t.start()
        return true
    }

    @Throws(SmbException::class)
    private fun checkForFileDeletion() {
        val smbFiles = Arrays.asList(*smb!!.list())
        var f: String? = null
        for (i in files.indices)
            f = files[i]
        if (f != null && !smbFiles.contains(f)) {
            files.remove(f)

            handler!!.handleDeletedFile(f)
        }
    }

    @Throws(SmbException::class)
    private fun checkForNewFile() {
        for (f in smb!!.list()) {
            if (!files.contains(f)) {
                files.add(f)
                handler!!.handleNewFile(f)
            }
        }
    }

    /**
     * Stop listening for notifications. The polling thread is terminated. Once
     * this method is called, if notifier is started again, an exception is
     * thrown.
     */
    fun stop() {
        isRunning = false
        stopped = true
    }

    private fun log(message: String, level: Level) {
        logger.log(level, message)
    }

}