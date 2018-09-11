package com.hisd3.utils

import ca.uhn.hl7v2.HL7Exception
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hisd3.utils.Crud.UserDao
import com.hisd3.utils.Dto.Hl7OrmDto
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
import spark.Response
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
            options.addOption("hisd3Port", true, "HIS Port")
            options.addOption("ris", true, "RIS Host/Machine")
            options.addOption("risPort", true, "RIS Port")
            options.addOption("smbHost", true, "SMB Host/Machine")
            options.addOption("smburl", true, "SMB smburl")
            options.addOption("smbDir", true, "Directory")
            options.addOption("user", true, "smb username")
            options.addOption("password", true, "smb password")
            options.addOption("start", false, "start hl7 rest service")

            val formatter = HelpFormatter()
            formatter.printHelp("[jarfile] options", options)

            val parser = DefaultParser()
            val cmd = parser.parse(options, args)

            val risHost = cmd.getOptionValue("rishost") ?: "127.0.0.1"
            val risPort = cmd.getOptionValue("risport") ?: "22223"

            val smbHost = cmd.getOptionValue("smbhost") ?: "172.0.0.1"
            val smbUrl = cmd.getOptionValue("smburl") ?: "smb://172.0.0.1/shared"
            val smbUser = cmd.getOptionValue("user") ?: "user"
            val smbPass = cmd.getOptionValue("password") ?: "password"
            val msgReceiver = JsonReceiver()
            val serviceListen = HL7ServiceListener()
            val watcher = Hl7DirectoryWatcher()


            if (cmd.hasOption("start")) {

                serviceListen.startLisenter()
                watcher.startDirWatching(smbHost, smbUser, smbPass, smbUrl)

                path( "/tests"){

                    get("/showvars") { req, res ->
                        "ris =" + risHost + "\nrisport =" + risPort + "\nsmbhost =" + smbHost + "\nsmburl =" + smbUrl + "\n"
                    }
                    get("/ping") { req, res -> "OK" }

                    get("/testsend") { req, res ->
                        HL7Test().transmit()
                    }
                }
                path("/") {

                    before("jsonmsg"){req,res ->

                        if(!req.body()?.isNullOrEmpty()!!) {

                                var data = req.body()
                           // println("data=" + data)
                                var gson = Gson()
                                if (!data.isEmpty()) {
                                   val msgDto :Hl7OrmDto  = gson.fromJson(data, Hl7OrmDto::class.java)

                                   when (msgDto.messageCode){
                                       "ADT_A04" ->{msgReceiver.createAdtMsg(msgDto, risHost, risPort, smbUrl, smbUser, smbPass, smbHost)}

                                       "ORM_O01" ->{msgReceiver.createOrmMsg(msgDto, risHost, risPort, smbUrl, smbUser, smbPass, smbHost)}
                                   }

                                }

                        }
                    }

                }
            }

        }
    }
}


