package com.hisd3.utils

import com.google.gson.Gson
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.Dto.Hl7OrmDto
import com.hisd3.utils.hl7service.HL7ServiceListener
import com.hisd3.utils.hl7service.HL7Test
import com.hisd3.utils.hl7service.Hl7DirectoryWatcher
import com.hisd3.utils.rest.JsonReceiver
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import spark.Spark.*

class Application

    fun bootstrap() {
        get("/bootup") { req, res -> "Test" }
    }

     @Throws(ParseException::class)
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

            var args = ArgDto()
                args.hisd3host =cmd.getOptionValue("hisd3host")?:"127.0.0.1"
                args.hisd3Port =cmd.getOptionValue("hisd3Port")?:"8080"
                args.risHost = cmd.getOptionValue("rishost") ?: "127.0.0.1"
                args.risPort = cmd.getOptionValue("risport") ?: "22223"
                args.smbHost = cmd.getOptionValue("smbhost") ?: "127.0.0.1"
                args.smbUrl = cmd.getOptionValue("smburl") ?: "smb://172.0.0.1/shared"
                args.smbUser = cmd.getOptionValue("user") ?: "user"
                args.smbPass = cmd.getOptionValue("password") ?: "password"


            var gson = Gson()

            if (cmd.hasOption("start")) {

                HL7ServiceListener().startLisenter(args)
                Hl7DirectoryWatcher().startDirWatching(args)

                path( "/tests"){
                    var argumentsData = gson.toJson(args)
                    get("/showvars") { req, res -> argumentsData
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



