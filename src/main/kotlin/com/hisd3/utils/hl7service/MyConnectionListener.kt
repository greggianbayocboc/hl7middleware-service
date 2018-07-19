package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.app.Connection
import ca.uhn.hl7v2.app.ConnectionListener

class MyConnectionListener : ConnectionListener {

    lateinit var senderIp: Connection
    override fun connectionReceived(theC: Connection) {
        System.out.println("New connection received: " + theC.getRemoteAddress().toString())
        senderIp = theC
    }
    override fun connectionDiscarded(theC: Connection) {
        System.out.println("Lost connection from: " + theC.getRemoteAddress().toString())
    }

}