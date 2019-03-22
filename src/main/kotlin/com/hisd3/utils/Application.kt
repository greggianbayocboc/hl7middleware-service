package com.hisd3.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.Dto.Hl7OrmDto
import com.hisd3.utils.Dto.obritem
import com.hisd3.utils.Sockets.WebsocketClient
import com.hisd3.utils.customtypes.IntegratedFacilities
import com.hisd3.utils.customwathcer.SmbNotifier
import com.hisd3.utils.hl7service.HL7ServiceListener
import com.hisd3.utils.hl7service.HL7Test
import com.hisd3.utils.hl7service.Hl7DirectoryWatcher
import com.hisd3.utils.hl7service.LisJobHandler
import com.hisd3.utils.httpservice.HttpSenderToHis
import com.hisd3.utils.rest.JsonReceiver
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import spark.Spark.*
import spark.kotlin.port
import spark.kotlin.staticFiles


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
        // port(4567)
         staticFiles.location("/public")
         port(4567)
         staticFiles.expireTime(600L)
         //webSocket("/chat",WSocketChatHandler::class.java)
         webSocket("/socketmessenging",WebsocketClient::class.java)

         val options = Options()

            options.addOption("hisd3Host", true, "HIS Host/Machine")
            options.addOption("hisd3User", true, "HIS user")
            options.addOption("hisd3Pass", true, "HIS password")
            options.addOption("hisd3Port", true, "HIS Port")
            options.addOption("risHost", true, "RIS Host/Machine")
            options.addOption("ormRisPort", true, "ORM RIS Port")
            options.addOption("adtRisPort", true, "ADT RIS Port")
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

            val args = ArgDto()
                args.hisd3Host =cmd.getOptionValue("hisd3Host")?:"http://127.0.0.1:8080"
                args.hisd3Port =cmd.getOptionValue("hisd3Port")?:"8080"
                args.risHost = cmd.getOptionValue("risHost") ?: "127.0.0.1"
                //args.risHost = cmd.getOptionValue("risHost") ?: "172.16.17.190"
                args.ormRisPort = cmd.getOptionValue("ormRisPort") ?: "10101"
                args.adtRisPort = cmd.getOptionValue("adtRisPort") ?: "10100"
                args.smbHost = cmd.getOptionValue("smbHost") ?: "HCLAB"
                args.smbUrl = cmd.getOptionValue("smbUrl") ?: "smb://172.16.10.9/Hl7Host"
                args.smbUser = cmd.getOptionValue("smbUser") ?: "lisuser"
                args.smbPass = cmd.getOptionValue("smbPass") ?: "p@ssw0rd"
                args.hisd3USer = cmd.getOptionValue("hisd3User") ?: "admin"
                args.hisd3Pass = cmd.getOptionValue("hisd3Pass") ?: "password"


            val gson = GsonBuilder().disableHtmlEscaping().create()

            if (cmd.hasOption("start")) {

                path( "/tests"){
                    val argumentsData = gson.toJson(args)
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
                        HL7Test().transmit(args)
                    }

                    get("/testsendV2") { req, res ->
                        val jData = Hl7OrmDto()
                            jData.hospitalName = ""
                            jData.pidPatientId = "P111111"
                            jData.integratedFacilities = IntegratedFacilities.RIS
                        val sampleObr = ArrayList<obritem>()
                        
                            for (i in 1..5) {
                                val itemobr = obritem()
                                itemobr.identifier = "PROCESSCODE" + i
                                itemobr.nameservice = "Service" + i

                                sampleObr.add(itemobr)
                            }
                            jData.obrArray = sampleObr
                        try {
                            JsonReceiver().createOrmMsg(jData, args)
                        }catch (e : Exception){
                            throw  e
                        }
                    }
                    get("/testpost"){req,res->
                        res.type("application.json")
                        HttpSenderToHis().testPostToHis(args)
                    }
                }
                path("/") {
                    post("jsonmsg"){req,res ->

                        if(!req.body()?.isEmpty()!!) {

                            val data = req.body()
                            val msgDto :Hl7OrmDto  = gson.fromJson(data, Hl7OrmDto::class.java)

                            try {
                                when (msgDto.messageCode) {
                                    "ADT_A04" -> {
                                        res.type("application/json")
                                        JsonReceiver().createAdtMsg(msgDto, args)
                                    }

                                    "ORM_O01" -> {
                                        res.type("application/json")
                                        JsonReceiver().createOrmMsg(msgDto, args)
                                    }
                                    else ->"Sorry, Option not available yet"
                                }
                            }catch (e:Exception){
                               throw IllegalArgumentException(e)
   //                             res.status(500)
  //                              res.body(e.message)

                                //println(e.message)
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
                HL7ServiceListener().startLisenter(args)
                SmbNotifier().notify(args)

            }

 }





