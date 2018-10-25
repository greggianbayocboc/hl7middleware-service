package com.hisd3.utils

import com.google.gson.Gson
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.Dto.Hl7OrmDto
import com.hisd3.utils.Sockets.WSocketHandler
import com.hisd3.utils.hl7service.HL7ServiceListener
import com.hisd3.utils.hl7service.HL7Test
import com.hisd3.utils.hl7service.Hl7DirectoryWatcher
import com.hisd3.utils.rest.JsonReceiver
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import spark.Spark.*
import spark.kotlin.port
import spark.kotlin.staticFiles
import java.util.HashMap
import j2html.TagCreator.header








class Application

    fun bootstrap() {
        get("/bootup") { req, res -> "Test" }
    }



     @Throws(ParseException::class)
     fun main(args: Array<String>) {


        /* println("Printing Arguments")
         args.forEach {

             println(it)
         }*/

         port(4567)
         staticFiles.location("/public")
         port(4567)
         staticFiles.expireTime(600L)
         webSocket("/chat", WSocketHandler::class.java)

         val options = Options()

            options.addOption("hisd3Host", true, "HIS Host/Machine")
            options.addOption("hisd3User", true, "HIS user")
            options.addOption("hisd3Pass", true, "HIS password")
            options.addOption("hisd3Port", true, "HIS Port")
            options.addOption("risHost", true, "RIS Host/Machine")
            options.addOption("risPort", true, "RIS Port")
            options.addOption("smbHost", true, "SMB Host/Machine")
            options.addOption("smbUrl", true, "SMB smburl")
            options.addOption("smbDir", true, "Directory")
            options.addOption("smbUser", true, "smb username")
            options.addOption("smbPass", true, "smb password")
            options.addOption("start", false, "start hl7 rest service")

            val formatter = HelpFormatter()
            formatter.printHelp("[jarfile] options", options)

            val parser = DefaultParser()
            val cmd = parser.parse(options, args)

            var args = ArgDto()
                args.hisd3Host =cmd.getOptionValue("hisd3Host")?:"127.0.0.1"
                args.hisd3Port =cmd.getOptionValue("hisd3Port")?:"8080"
                args.risHost = cmd.getOptionValue("risHost") ?: "127.0.0.1"
                args.risPort = cmd.getOptionValue("risPort") ?: "22223"
                args.smbHost = cmd.getOptionValue("smbHost") ?: "127.0.0.1"
                args.smbUrl = cmd.getOptionValue("smbUrl") ?: "smb://hclab.ace-mc-bohol.com/shared"
                args.smbUser = cmd.getOptionValue("smbUser") ?: "user"
                args.smbPass = cmd.getOptionValue("smbPass") ?: "password"
                args.hisd3USer = cmd.getOptionValue("hisd3User") ?: "adminuser"
                args.hisd3Pass = cmd.getOptionValue("hisd3Pass") ?: "password"


            var gson = Gson()

            if (cmd.hasOption("start")) {

                HL7ServiceListener().startLisenter(args)
                Hl7DirectoryWatcher().startDirWatching(args)


                path( "/tests"){
                    var argumentsData = gson.toJson(args)
                    get("/showvars") { req, res ->

                        val accessControlRequestHeaders = req
                                .headers("Access-Control-Request-Headers")
                        if (accessControlRequestHeaders != null) {
                            res.header("Access-Control-Allow-Headers",
                                    accessControlRequestHeaders)
                        }

                        val accessControlRequestMethod = req
                                .headers("Access-Control-Request-Method")
                        if (accessControlRequestMethod != null) {
                            res.header("Access-Control-Allow-Methods",
                                    accessControlRequestMethod)
                        }

                        argumentsData
                    }

                    get("/ping") { req, res -> "OK" }

                    get("/testsend") { req, res ->
                        HL7Test().transmit()
                    }
                }
                path("/") {
                    post("jsonmsg"){req,res ->

                        if(!req.body()?.isNullOrEmpty()!!) {

                            var data = req.body()
                            val msgDto :Hl7OrmDto  = gson.fromJson(data, Hl7OrmDto::class.java)

                            try {
                                when (msgDto.messageCode) {
                                    "ADT_A04" -> {
                                        JsonReceiver().createAdtMsg(msgDto, args)
                                    }

                                    "ORM_O01" -> {
                                        JsonReceiver().createOrmMsg(msgDto, args)
                                    }
                                    else ->"Sorry, Option not available yet"
                                }
                            }catch (e:Exception){
                               throw IllegalArgumentException(e)
   //                             res.status(500)
  //                              res.body(e.message)

                                println(e.message)
                                return@post Unit

                            }
                        }else{
                            halt(401,"req.body is empty")
                        }
//                        res.body()
//                        res.status(200)
//                        "ok"
                    }

                }
            }

 }





