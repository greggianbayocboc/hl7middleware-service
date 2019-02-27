package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import com.hisd3.utils.Dto.ArgDto


class   HL7ServiceListener {

//    @Inject
//    internal var orderSlipRepository: OrderSlipRepository? = null
//
//    @Inject
//    internal var labResultItemRepository: LabResultItemRepository? = null


    fun startLisenter( args:ArgDto) {



        var port = 22222 // The port to listen on
        val useTls = false // Should we use TLS/SSL?

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var server = context.newServer(port, useTls)

        val oruHandler = OruRo1Handler<Any>(args)
        val adtHandler = AdtAo4Handler<Any>()
//        server.registerApplication("ADT", "A01", handler)
        server.registerApplication("ADT", "A04", adtHandler)
        server.registerApplication("ORU", "R01", oruHandler)

        //server.registerApplication(handler)
        server.registerConnectionListener(MyConnectionListener())
        server.setExceptionHandler(MyExceptionHandler())
        server.startAndWait()
        //System.out.println("Start HL7 Service Listener")

    }

}