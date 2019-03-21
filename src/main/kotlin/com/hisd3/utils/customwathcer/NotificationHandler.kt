package com.hisd3.utils.customwathcer

/**
 *
 * Defines a default handler for notification callbacks.
 *
 * @author Ryan Beckett
 * @version 1.0
 */
abstract class NotificationHandler {

    /**
     * A callback for the addition of a file in the watched directory.
     *
     * @param file
     * The relative file name of the newly created file.
     */
    abstract fun handleNewFile(file: String)

    /**
     * A callback for the deletion of a file in the watched directory.
     *
     * @param file
     * The relative file name of the deleted file.
     */
    abstract fun handleDeletedFile(file: String)
}