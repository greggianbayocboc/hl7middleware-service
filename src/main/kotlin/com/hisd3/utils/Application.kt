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
            options.addOption("t", false, "display current time")
            options.addOption("c", true, "country code")
            options.addOption("listen_dir", true, "remote directory path")
            options.addOption("start", false, "start hl7 rest service")

            val formatter = HelpFormatter()
            formatter.printHelp("[jarfile] options", options)

            val parser = DefaultParser()
            val cmd = parser.parse(options, args)

            val countryCode = cmd.getOptionValue("c")

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


