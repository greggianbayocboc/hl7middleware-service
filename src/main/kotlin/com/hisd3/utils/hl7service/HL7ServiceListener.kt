package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.model.v23.message.ADT_A01
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.Sockets.TutorialSocket
import com.hisd3.utils.Sockets.WSocketChatHandler
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI


class   HL7ServiceListener {

//    @Inject
//    internal var orderSlipRepository: OrderSlipRepository? = null
//
//    @Inject
//    internal var labResultItemRepository: LabResultItemRepository? = null


    fun startLisenter( args:ArgDto) {

        val dest = "ws://localhost:8080/tutorialsokcet"
        val client = WebSocketClient()
        val socket = TutorialSocket()
        try {
            Thread.sleep(1000L)
            client.start()
            val echoUri = URI(dest)
            val request = ClientUpgradeRequest()
            var conn =client.connect(socket, echoUri, request)
            //socket.getLatch()
            socket.onText(null,"echo")
            //socket.onText(null,"test")


        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            try {
                client.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        var port = 22222 // The port to listen on
        val useTls = false // Should we use TLS/SSL?

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var server = context.newServer(port, useTls)

        val oruHandler = OruRo1Handler<Any>(args,socket)
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