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
import ca.uhn.hl7v2.model.v25.datatype.CE
import ca.uhn.hl7v2.util.Terser
import com.hisd3.utils.hl7service.HL7DateTime
import org.joda.time.DateTime

class JsonReceiver {


    fun createOrmMsg(msgDto: Hl7OrmDto, args:ArgDto):String? {
        var gson = Gson()
//
//            val msgDto = gson.fromJson(data, Hl7OrmDto::class.java)
 //       println(msgDto)



        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

            var orm = ORM_O01()

            orm.initQuickstart("ORM","O01","P")
            //orm.initQuickstart(msgDto.messageCode, msgDto.messageTriggerEvent, "D")
            var parser = context.getPipeParser()
            parser.getParserConfiguration().setIdGenerator(InMemoryIDGenerator())

//          val adt = p.parse(msg)


        // Populate the MSH Segment
        var msh = orm.getMSH()
        msh.messageControlID.value = msgDto?.messageControlId?:"11111"
        msh.getSendingApplication().getNamespaceID().value = msgDto?.sendingApplication
        msh.sendingFacility.namespaceID.value =msgDto?.sendingFacility

        val i = DateTime.now().toString("yyyyMMddHHmmss")

        var date = Date()
        var ts = Timestamp(date.getTime())
        //msh.dateTimeOfMessage.timeOfAnEvent.value  = i
        msh.dateTimeOfMessage.time.value =i

        // Populate the PID Segment
        var pid = orm.getPATIENT().getPID()

        pid.getPatientName(0).getFamilyName().surname.value =msgDto?.pidLastName
        //pid.getPatientName(0).familyName.value  =msgDto?.pidLastName
        pid.getPatientName(0).getGivenName().value =msgDto?.pidFirstName
        pid.getPatientName(0).getSuffixEgJRorIII().setValue(msgDto?.pidExtName?:"")
       // pid.dateTimeOfBirth.time.value = msgDto?.pidDob
        var dobs =  HL7DateTime(msgDto?.pidDob!!)
       // pid.pid7_DateOfBirth.timeOfAnEvent.value = msgDto?.pidDob
        pid.dateTimeOfBirth.time.value =msgDto?.pidDob
        pid.getPatientAddress(0).getCity().setValue(msgDto.pidCity)
        pid.getPatientAddress(0).country.value=msgDto?.pidCountry
        pid.getPatientAddress(0).streetAddress.streetName.value =msgDto.pidAddress
       // pid.getPatientAddress(0).streetAddress.value = msgDto.pidAddress
        pid.getPatientAddress(0).stateOrProvince.value=msgDto?.pidProvince
        pid.getPatientAddress(0).zipOrPostalCode.value=msgDto?.pidZip
        //pid.patientID.idNumber.value = msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).idNumber.value=msgDto?.pidPatientNo
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
        pv1.getAttendingDoctor(0).givenName.value=msgDto.pv1RequestingDrFname
       // pv1.getAttendingDoctor(0).familyName.value=msgDto.pv1RequestingDrLname
        pv1.getAdmittingDoctor(0).familyName.surname.value = msgDto.pv1RequestingDrLname
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1RequestingDrId

        var order = orm.getORDER(0)

        var orc = orm.getORDER(0).getORC()
        orc.orc1_OrderControl.value="NW"
        orc.orderStatus.value="SC"
        orc.dateTimeOfTransaction.time.value=i
        orc.getEnteredBy(0).idNumber.value=msgDto.orcRequestingDrId
        orc.getEnteredBy(0).familyName.surname.value =msgDto.orcRequestingDrFname
        orc.enteringOrganization.identifier.value= msgDto.hospitalName
        orc.placerOrderNumber.entityIdentifier.value = msgDto?.obrFileOrderNumber
        orc.fillerOrderNumber.entityIdentifier.value = msgDto?.obrFileOrderNumber


        var obr = orm.getORDER(0).getORDER_DETAIL().getOBR()

        if (msgDto?.obrArray !=null){
            val terser = Terser(orm)
            obr.setIDOBR.value = "1"
            obr.placerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
            //obr.fillerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
            obr.requestedDateTime.time.value=msgDto?.obrRequestDate
            var priority:String?
            if(msgDto?.obrPriority == true){
                priority  = "STAT"
            }else{
                priority = "ROUTINE"
            }
            obr.priorityOBR.value = priority
            obr

            var x  = 0
            for(obritem in msgDto?.obrArray!!)
                {
                    // Populate the OBR Segment

                   // obr.setIDOBR.value = x.toString()
                   // obr.universalServiceIdentifier.identifier.value = obritem.identifier
                  //  obr.universalServiceIdentifier.text.value = obritem.nameservice
                   // obr.placerOrderNumber.entityIdentifier.value = obritem.obrFileOrderNumber
                  //  obr.getPlacerOrderNumber(0).entityIdentifier.value = obritem.obrFileOrderNumber
                  //  obr.getFillerOrderNumber().entityIdentifier.value = obritem.obrFileOrderNumber
//                    obr.requestedDateTime.time.value = obritem.observationDate
//                    obr.observationDateTime.time.value  = obritem.observationDate
//                    obr.scheduledDateTime.time.value = obritem.observationDate

                    //obr.priority.value = priority
                    terser.set("/.OBR-4("+x+")-1", obritem.identifier)
                    terser.set("/.OBR-4("+x+")-2", obritem.nameservice)

                    x++
                }
        }else {
            obr.setIDOBR.value = "1"
            obr.placerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
           // obr.fillerOrderNumber.entityIdentifier.value=msgDto?.obrPlaceOrderNumber
            obr.requestedDateTime.time.value = msgDto.obrRequestDate
            obr.observationDateTime.time.value = msgDto.obrObservationDate
            var priority: String?
            if (msgDto.obrPriority == true) {
                priority = "STAT"
            } else {
                priority = "ROUTINE"
            }
            obr.priorityOBR.value = priority
            obr.getOrderingProvider(0).idNumber
            obr.scheduledDateTime.time.value = msgDto.obrRequestDate

            obr.universalServiceIdentifier.identifier.value = msgDto.obrServiceIdentifier
            obr.universalServiceIdentifier.text.value = msgDto.obrServiceName
            obr.obr19_PlacerField2.value =msgDto.modalityType
            obr.diagnosticServSectID.value = msgDto.diagnosticSev
        }

        /*
         * In other situation, more segments and fields would be populated
         */
        // Now, let's encode the message and look at the output
        var  encodedMessage = parser.encode(orm)
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
                return  dirWritter(msgDto, args, encodedMessage)
            }

        }

    fun createAdtMsg(msgDto: Hl7OrmDto,args: ArgDto):String? {

        val formatter2 = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var adt = ADT_A01()

        adt.initQuickstart("ADT","A04","P")

        var parser = context.getPipeParser()
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
        pid.getPatientName(0).getFamilyName().surname.value =msgDto?.pidLastName
        pid.getPatientName(0).getGivenName().value =msgDto?.pidFirstName
        pid.getPatientName(0).getSuffixEgJRorIII().setValue(msgDto?.pidExtName?:"")
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
        pv1.assignedPatientLocation.bed.value=msgDto.bed
        pv1.getAttendingDoctor(0).givenName.value=msgDto.pv1RequestingDrFname
        pv1.getAttendingDoctor(0).familyName.surname.value=msgDto.pv1RequestingDrLname
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1RequestingDrId

        var  encodedMessage = parser.encode(adt)
        val useTls = false // Should we use TLS/SSL?

        if (msgDto.recievingApplication == "RIS"){
                return   httpSender(args,adt,args.adtRisPort?.toInt())
        }

        else {
            return    dirWritter(msgDto,args, encodedMessage)
        }

    }

    fun httpSender(args:ArgDto,rawmsg:Message,port:Int?= null): String? {

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        val useTls = false // Should we use TLS/SSL?
//        var connection = context.newClient(args.risHost, args.risPort!!.toInt(), useTls)
        var connection = context.newClient(args.risHost, 22223, useTls)
            try {
//              var connection = context.newClient(msgDto.recievingFacility.ipAddress, 22223, useTls)

                var initiator = connection.initiator
                var response = initiator.sendAndReceive(rawmsg)

               // connection.close()
                return response.encode()


            } catch (e: IOException) {
               //throw IllegalArgumentException(e.message)
                throw HL7Exception(e)
            }
            finally {
                connection.close()
            }
         }
        }


    fun dirWritter(msgDto: Hl7OrmDto,args:ArgDto,encodedMessage: String): String? {

        var gson = Gson()
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

                val path = args.smbUrl+"/Order/"+msgDto.messageControlId+".hl7"
                val sFile = SmbFile(path, ntlmPasswordAuthentication)
                var sfos =  SmbFileOutputStream(sFile)
                sfos.write(encodedMessage.toByteArray())
                sfos.close()
            println("Written file" + msgDto.messageControlId.toString())
            /*** writing files in local shared folder***/
//                var file = Paths.get("//localhost/Shared/Outbox/"+msgDto.msh.messageControlId+".hl7")
//                Files.write(file,encodedMessage.toByteArray())

        }catch(e: IOException) {
            throw IllegalArgumentException(e.message)
            e.printStackTrace()
        }
        return gson.toJson("ok")
    }

