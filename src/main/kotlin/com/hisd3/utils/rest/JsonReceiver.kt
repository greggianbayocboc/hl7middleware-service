package com.hisd3.utils.rest

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.message.ADT_A01
import ca.uhn.hl7v2.model.v25.message.ORM_O01
//import ca.uhn.hl7v2.model.v23.message.ORM_O01
import ca.uhn.hl7v2.model.v25.segment.OBR
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.util.idgenerator.InMemoryIDGenerator
import com.google.gson.Gson
import com.hisd3.utils.Dto.ArgDto
import com.hisd3.utils.Dto.Hl7OrmDto
import com.hisd3.utils.customtypes.IntegratedFacilities
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileOutputStream
import spark.Response
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import jdk.nashorn.internal.objects.NativeDate.getTime
import java.sql.Timestamp
import java.util.*
import HL7DateTimeParser
import ca.uhn.hl7v2.hoh.llp.Hl7OverHttpLowerLayerProtocol
import ca.uhn.hl7v2.hoh.util.ServerRoleEnum
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol
import ca.uhn.hl7v2.llp.MllpConstants
import ca.uhn.hl7v2.model.v25.datatype.CE
import ca.uhn.hl7v2.model.v25.message.ACK
import ca.uhn.hl7v2.model.v25.segment.MSH
import ca.uhn.hl7v2.model.v25.segment.PID
import ca.uhn.hl7v2.util.Terser
import com.hisd3.utils.hl7service.HL7DateTime
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

class JsonReceiver {


    fun createOrmMsg(msgDto: Hl7OrmDto, args:ArgDto): String? {
        var gson = Gson()
//
//            val msgDto = gson.fromJson(data, Hl7OrmDto::class.java)
//        println(gson.toJson(msgDto))



        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var  llp = Hl7OverHttpLowerLayerProtocol(ServerRoleEnum.CLIENT)
        context.setLowerLayerProtocol(llp)
        //val mllp = MinLowerLayerProtocol()
        llp.setCharset("UTF-8")
        context.setLowerLayerProtocol(llp)

            var orm = ORM_O01()

            orm.initQuickstart("ORM","O01","P")
            //orm.initQuickstart(msgDto.messageCode, msgDto.messageTriggerEvent, "D")
            var parser = context.getPipeParser()

            parser.getParserConfiguration().setIdGenerator(InMemoryIDGenerator())
            parser.defaultEncoding


//          val adt = p.parse(msg)


        // Populate the MSH Segment
        var msh = orm.getMSH()
        msh.messageControlID.value = msgDto?.messageControlId?:"11111"
        msh.getSendingApplication().getNamespaceID().value = msgDto?.sendingApplication
        msh.sendingFacility.namespaceID.value =msgDto?.sendingFacility
        //msh.getCharacterSet(0).value= "UNICODE UTF-8"
        val i = DateTime.now().toString("yyyyMMddHHmmss")

        var date = Date()
        var ts = Timestamp(date.getTime())
        //msh.dateTimeOfMessage.timeOfAnEvent.value  = i
        msh.dateTimeOfMessage.time.value =i

        // Populate the PID Segment
        var pid = orm.getPATIENT().getPID()

       // pid.pid5_PatientName[0].familyName.ownSurnamePrefix.value = msgDto?.pidMiddleName
        pid.getPatientName(0).getFamilyName().surname.value =msgDto?.pidLastName?.replace("Ñ","N")
        pid.getPatientName(0).getSecondAndFurtherGivenNamesOrInitialsThereof().value =msgDto?.pidMiddleName?.replace("Ñ","N")
        pid.getPatientName(0).getGivenName().value =msgDto?.pidFirstName?.replace("Ñ","N")
      //  pid.getPatientName(0).getSuffixEgJRorIII().value=msgDto?.pidExtName
       // pid.getPatientName(0).getPrefixEgDR().value= "ENGR"

       // pid.dateTimeOfBirth.time.value = msgDto?.pidDob
        var dobs =  HL7DateTime(msgDto?.pidDob!!)
       // pid.pid7_DateOfBirth.timeOfAnEvent.value = msgDto?.pidDob
        pid.dateTimeOfBirth.time.value =msgDto?.pidDob
        pid.getPatientAddress(0).city.value =msgDto.pidCity
        pid.getPatientAddress(0).country.value=msgDto?.pidCountry
        pid.getPatientAddress(0).streetAddress.streetName.value = StringUtils.trim(msgDto.pidAddress)
       // pid.getPatientAddress(0).streetAddress.value = msgDto.pidAddress
        pid.getPatientAddress(0).stateOrProvince.value=msgDto?.pidProvince
        pid.getPatientAddress(0).zipOrPostalCode.value=msgDto?.pidZip
        //pid.patientID.idNumber.value = msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).idNumber.value=msgDto?.pidPatientNo
        pid.getAlternatePatientIDPID(0).idNumber.value =msgDto?.pidAlternatePid
        pid.administrativeSex.value =msgDto?.pidGender
       // pid.patientID.checkDigit.value= msgDto?.obrFileOrderNumber
       // pid.getPatientIDInternalID(0).id.value  = msgDto?.pidPatientNo

      //  pid.pid8_Sex.value=msgDto?.pidGender


        // Populate the PV1 Segment
        var pv1 = orm.getPATIENT().getPATIENT_VISIT().getPV1()

        pv1.patientClass.value = msgDto.pv1PatientClass
        pv1.visitNumber.idNumber.value =msgDto.pv1VisitNumer
        //pv1.visitNumber.id.value = msgDto.pv1VisitNumer
        pv1.patientType.value=msgDto.pv1PatientClass
        pv1.assignedPatientLocation.bed.value=msgDto.bed

        pv1.getAdmittingDoctor(0).familyName.surname.value = msgDto.pv1RequestingDrFname?.replace("Ñ","N")
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1RequestingDrId?.replace("Ñ","N")
        pv1.getReferringDoctor(0).familyName.surname.value=msgDto.pv1RequestingDrFname?.replace("Ñ","N")
        //pv1.getReferringDoctor(0).givenName.value=msgDto.pv1RequestingDrFname
        var order = orm.getORDER(0)

        var orc = orm.getORDER(0).getORC()
        orc.orc1_OrderControl.value="NW"
        orc.orderStatus.value="SC"
        orc.dateTimeOfTransaction.time.value=i
        orc.getEnteredBy(0).idNumber.value=msgDto.pv1RequestingDrId
        orc.getEnteredBy(0).familyName.surname.value =msgDto.pv1RequestingDrLname
        orc.enteringOrganization.identifier.value= msgDto.hospitalName
        orc.placerOrderNumber.entityIdentifier.value = msgDto?.obrFileOrderNumber
        orc.fillerOrderNumber.entityIdentifier.value = msgDto?.obrFileOrderNumber


        var obr = orm.getORDER(0).getORDER_DETAIL().getOBR()

        if (msgDto?.obrArray !=null){
            val terser = Terser(orm)
            obr.setIDOBR.value = "1"
            obr.placerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
            obr.fillerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
            obr.requestedDateTime.time.value=msgDto?.obrRequestDate
            obr.observationDateTime.time.value = i
            obr.scheduledDateTime.time.value=i
            var priority:String?
            if(msgDto?.obrPriority == true){
                priority  = "STAT"
            }else{
                priority = "ROUTINE"
            }
            obr.priorityOBR.value = priority
            var x  = 0
            for(obritem in msgDto?.obrArray!!)
                {
                    // Populate the OBR Segment

                    terser.set("/.OBR-4("+x+")-1", obritem.identifier)
                    terser.set("/.OBR-4("+x+")-2", obritem.nameservice)

                    x++
                }
        }else {
            obr.setIDOBR.value = "1"
            obr.placerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
            obr.fillerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
            obr.requestedDateTime.time.value = msgDto.obrRequestDate
            obr.observationDateTime.time.value = msgDto.obrObservationDate
            var priority: String?
            if (msgDto.obrPriority == true) {
                priority = "STAT"
            } else {
                priority = "ROUTINE"
            }
            obr.priorityOBR.value = priority
            obr.scheduledDateTime.time.value = i
            obr.observationDateTime.time.value = i
            obr.requestedDateTime.time.value=msgDto?.obrRequestDate
            obr.getOrderingProvider(0).idNumber.value = msgDto.pv1RequestingDrId
            obr.getOrderingProvider(0).familyName.surname.value =msgDto.pv1RequestingDrFname
            obr.universalServiceIdentifier.identifier.value = msgDto.obrServiceIdentifier
            obr.universalServiceIdentifier.text.value = msgDto.serviceCategory +"-" + msgDto.obrServiceName
            obr.obr19_PlacerField2.value =msgDto.modalityType
            obr.obr21_FillerField2.value=msgDto.modalityType
            obr.obr24_DiagnosticServSectID.value="RX"
            obr.diagnosticServSectID.value = msgDto.diagnosticSev
        }

        /*
         * In other situation, more segments and fields would be populated
         */
        // Now, let's encode the message and look at the output

       // println(encodedMessage)
        val useTls = false // Should we use TLS/SSL?
//            try {
//                var connection = context.newClient(msgDto.recievingFacility.ipAddress, 22223, useTls)
//                var initiator = connection.initiator
//                var response = initiator.sendAndReceive(orm)
//
//                connection.close()
//                //System.out.println("Received response:\n" + responseString)
//                var gson = Gson()
//                return gson.toJson(response)
//
//            } catch (e: IOException) {
//                throw IllegalArgumentException(e.message)
//                throw HL7Exception(e)
//            }


            if (msgDto.recievingApplication == "RIS") {

                    return   httpSender(args,orm,args?.ormRisPort?.toInt())

            }

            else {
                val terser = Terser(orm)
                terser.set("/.PID-5-1","")
                terser.set("/.PID-5-3","")
                terser.set("/.PID-5-2", msgDto?.pidFullname)
                terser.set("/.MSH-18","UNICODE UTF-8")
                var  encodedMessage = parser.encode(orm)
                return  dirWritter(msgDto, args, orm)
            }

        }

    fun createAdtMsg(msgDto: Hl7OrmDto,args: ArgDto): String? {

        val formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var adt = ADT_A01()

        adt.initQuickstart("ADT","A04","P")

        var parser = context.getPipeParser()
        //var parser = context.getGenericParser()
        parser.getParserConfiguration().setIdGenerator(InMemoryIDGenerator())

        // Populate the MSH Segment
        var msh = adt.getMSH()
        msh.messageControlID.value = msgDto?.messageControlId
        msh.getSendingApplication().getNamespaceID().value = msgDto?.sendingApplication
        msh.sendingFacility.namespaceID.value = msgDto.sendingFacility
        msh.receivingApplication.namespaceID.value =msgDto?.recievingApplication
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)
        msh.dateTimeOfMessage.time.value=formatted

        // Populate the PID Segment
        var pid = adt.getPID()
        pid.getPatientName(0).getFamilyName().surname.value =msgDto?.pidLastName?.replace("Ñ","N")
        pid.getPatientName(0).getSecondAndFurtherGivenNamesOrInitialsThereof().value =msgDto?.pidMiddleName?.replace("Ñ","N")
        pid.getPatientName(0).getGivenName().value =msgDto?.pidFirstName?.replace("Ñ","N")
        pid.getPatientName(0).getSuffixEgJRorIII().value=msgDto?.pidExtName?:""
        pid.dateTimeOfBirth.time.value = msgDto?.pidDob
        pid.getPatientAddress(0).getCity().setValue(msgDto.pidCity)
        pid.getPatientAddress(0).country.value=msgDto?.pidCountry
        pid.getPatientAddress(0).stateOrProvince.value=msgDto?.pidProvince
        pid.getPatientAddress(0).zipOrPostalCode.value=msgDto?.pidZip
        pid.patientID.idNumber.value = msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).idNumber.value=msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).checkDigit.value=""

        pid.administrativeSex.value = msgDto?.pidGender

        // Populate the PV1 Segment
        var pv1 = adt.getPV1()
        var pclass :String? = null
        pv1.patientClass.value = msgDto.pv1PatientClass
        pv1.visitNumber.idNumber.value =msgDto.pv1VisitNumer
        pv1.patientType.value=msgDto.pv1PatientClass
        pv1.assignedPatientLocation.room.value = msgDto.pv1Room?:"DEMO Room"
        pv1.assignedPatientLocation.bed.value= msgDto.bed
        pv1.getAttendingDoctor(0).givenName.value=msgDto.pv1RequestingDrFname?.replace("Ñ","N")
        pv1.getAttendingDoctor(0).familyName.surname.value= msgDto.pv1RequestingDrLname?.replace("Ñ","N")
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1RequestingDrId
        var  encodedMessage = parser.encode(adt)
        val useTls = false // Should we use TLS/SSL?

        if (msgDto.recievingApplication == "RIS"){
                return   httpSender(args,adt,args.adtRisPort?.toInt())
        }

        else {
            return    dirWritter(msgDto,args, adt)
        }

    }

    fun httpSender(args:ArgDto,rawmsg:Message,port:Int?= null): String? {

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)
 //       val parser = context.genericParser
        val useTls = false // Should we use TLS/SSL?
//        var connection = context.newClient(args.risHost, args.risPort!!.toInt(), useTls)
        var connection = context.newClient(args.risHost, port!!, useTls)

            try {
//              var connection = context.newClient(msgDto.recievingFacility.ipAddress, 22223, useTls)

                var initiator = connection.initiator
                var res = initiator.sendAndReceive(rawmsg)
                println("middleware: response from RIS : " + res)

                var msa :String? = null
                try{

                    msa = StringUtils.substring(
                            res.toString(),
                            StringUtils.indexOf(res.toString(),"MSA"),
                            res.toString().length
                    )
                }catch (e: Exception){
                    throw e
                }

              return StringUtils.trim(msa)


            } catch (e: IOException) {
               //throw IllegalArgumentException(e.message)
                throw HL7Exception(e)
            }
            finally {
                connection.close()
            }
         }


    fun dirWritter(msgDto: Hl7OrmDto,args:ArgDto,encodedMessage: Message): String? {

        try {
            /** writing files to shared folder in a network with credentials**/

            val ntlmPasswordAuthentication = NtlmPasswordAuthentication(args.smbHost,args.smbUser, args.smbPass)

            val shared = args.smbUrl+"/Order/"
            val directory = SmbFile(shared,ntlmPasswordAuthentication)

            try{
                if (! directory.exists()) {
                    directory.mkdir()
                }
            }catch(e: IOException) {
                throw IllegalArgumentException(e.message)
                e.printStackTrace()
            }

            var context = DefaultHapiContext()
            var mcf = CanonicalModelClassFactory("2.5")
            context.setModelClassFactory(mcf)

//            var  llp = Hl7OverHttpLowerLayerProtocol(ServerRoleEnum.CLIENT)
//            context.setLowerLayerProtocol(llp)
            //val mllp = MinLowerLayerProtocol()
            //llp.setCharset("UNICODE UTF-8")
            //context.setLowerLayerProtocol(llp)

            var parser3 =context.getPipeParser()
           // System.setProperty(MllpConstants.CHARSET_KEY, "UNICODE UTF-8")

            val path = args.smbUrl+"/Order/"+msgDto.messageControlId+".hl7"
            val sFile = SmbFile(path, ntlmPasswordAuthentication)
            var sfos =  SmbFileOutputStream(sFile)
            sfos.write(parser3.encode(encodedMessage).toByteArray())
            sfos.flush()
            sfos.close()
            println("Written file : " + msgDto.pidPatientId.toString())


            /*** writing files in local shared folder***/

//            var file = File("//localhost/Shared/Order/"+msgDto.messageControlId+".hl7")
//
//            if (!file.exists()) {
//                file.createNewFile()
//            }
//
//            System.out.println("Serializing message to file...")
//
//            var outputStream = FileOutputStream(file)
//            outputStream.write(parser3.encode(encodedMessage).toByteArray())
//            outputStream.flush()
//
//            outputStream?.close()
        }catch(e: IOException) {
            e.printStackTrace()
            throw IllegalArgumentException(e.message)
        }

        return  "AA"
    }


}


