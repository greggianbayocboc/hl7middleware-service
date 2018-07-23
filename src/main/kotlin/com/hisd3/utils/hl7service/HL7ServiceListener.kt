package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.protocol.ReceivingApplication


class   HL7ServiceListener {

//    @Inject
//    internal var orderSlipRepository: OrderSlipRepository? = null
//
//    @Inject
//    internal var labResultItemRepository: LabResultItemRepository? = null


    public fun startLisenter() {

        var port = 22222 // The port to listen on
        val useTls = false // Should we use TLS/SSL?

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.3")
        context.setModelClassFactory(mcf)

        var server = context.newServer(port, useTls)

        val handler = ExampleReceiverApplication<Any>()
        val ormHandler =OrmOru<Any>()
//      var handler = ExampleReceiverApplication<Any>(orderSlipRepository!!,labResultItemRepository!!)

//        server.registerApplication("ADT", "A01", handler)
//        server.registerApplication("ADT", "A04", handler)
        server.registerApplication("ORU", "R01", ormHandler)

        //server.registerApplication(handler)
        server.registerConnectionListener(MyConnectionListener())
        server.setExceptionHandler(MyExceptionHandler())
        server.startAndWait()
        System.out.println("Start HL7 Service Listener")
    }

}