package com.hisd3.utils

import ca.uhn.hl7v2.HL7Exception
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hisd3.utils.Crud.UserDao
import com.hisd3.utils.hl7service.HL7ServiceListener
import com.hisd3.utils.hl7service.HL7Test
import com.hisd3.utils.hl7service.Hl7DirectoryWatcher
import com.hisd3.utils.rest.JsonReceiver
import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.eclipse.jetty.websocket.api.StatusCode
import spark.Request
import spark.Spark.*
import java.io.IOException

class Application {

    fun bootstrap() {
        get("/bootup") { req, res -> "Test" }
    }


    companion object {

        @Throws(ParseException::class)
        @JvmStatic
        fun main(args: Array<String>) {

            val options = Options()

            options.addOption("hisd3host", true, "HIS Host/Machine")
            options.addOption("hisPort", false, "HIS Port")
            options.addOption("ris", true, "RIS Host/Machine")
            options.addOption("risPort", false, "RIS Port")
            options.addOption("lis", true, "LIS Host/Machine")
            options.addOption("dir", false, "Directory")
            options.addOption("user", true, "username")
            options.addOption("password", true, "password")
            options.addOption("start", false, "start hl7 rest service")

            val formatter = HelpFormatter()
            formatter.printHelp("[jarfile] options", options)

            val parser = DefaultParser()
            val cmd = parser.parse(options, args)

            val countryCode = cmd.getOptionValue("c")
            val hisd3Host = cmd.getOptionValue("hisd3Host")
            val risHost = cmd.getOptionValue("risHost")
            val lisHost = cmd.getOptionValue("lisHost")
            if (countryCode == null) {
                // print default date

            } else {
                // print date for country specified by countryCode
                println("Country Code Specified")
            }

            if (cmd.hasOption("t")) {
                // print the date and time
                println("Has T Specified")
            } else {
                // print the date
            }

            if (cmd.hasOption("start"))

            HL7ServiceListener().startLisenter()

            get("/ping") { req, res -> "OK" }

            path("/hl7middleware")
            {
                get("/testsend") { req, res ->
                    HL7Test().transmit()
                }

                post("/jsonmsg") { req, res ->

                    var data = req.body()
                    try {
                        JsonReceiver().createOrmMsg(data)
                    } catch (e: IOException) {
                        throw IllegalArgumentException(e.message)
                    }
                }
            }
            Hl7DirectoryWatcher().startDirWatching()
        }
    }
}


