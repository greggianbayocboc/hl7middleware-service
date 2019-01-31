package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.DefaultHapiContext
import com.hisd3.utils.Dto.ArgDto

class HL7Test {

    fun transmit(args: ArgDto): String? {
        val msg  = "MSH|^~\\&|HL7Soup|Instance1|HL7Soup|Instance2|201407271408||ADT^A04|1817457|D|2.5.1|123456||AL\r"+
        "PID||0797675^^^^MR|454721||Brown^Sam^^^^^B|Smith^Mary^^^^|19780203|M||2106-3|254 East St^^Howick^OH^3252^USA||(216)671-4859|||S|AGN|400003603~1629086|999-8888|\r"+
        "PV1||O|O/R|A|||060277^Allen^Katrina^J^^^|||||||||| ||2668684|||||||||||||||||||||||||201407271408|\r"

        val useTls = false // Should we use TLS/SSL?

        val context = DefaultHapiContext()
        //val handler = ExampleReceiverApplication()

        val p = context.pipeParser
        val adt = p.parse(msg)

        var connection = context.newClient(args.risHost, args.adtRisPort!!.toInt(), useTls)
        var initiator = connection.initiator
        var response = initiator.sendAndReceive(adt)

        val responseString = p.encode(response)
        System.out.println("Received response:\n" + responseString)
        return responseString
//        context.newClient("localhost", port, useTls)
//        initiator = connection.getInitiator()
//        response = initiator.sendAndReceive(adt)
        connection.close()


    }

}
