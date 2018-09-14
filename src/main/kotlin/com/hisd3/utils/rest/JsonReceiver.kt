package com.hisd3.utils.rest

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.message.ADT_A01
import ca.uhn.hl7v2.model.v25.message.ORM_O01
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.util.idgenerator.InMemoryIDGenerator
import com.google.gson.Gson
import com.hisd3.utils.Dto.Hl7OrmDto
import com.hisd3.utils.customtypes.IntegratedFacilities
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileOutputStream
import spark.Response
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



class JsonReceiver {


    fun createOrmMsg(msgDto: Hl7OrmDto, risHost: String?, risPort:String?,smbUrl:String?,smbUser:String?,smbPass:String?,smbHost:String?):String? {
        var gson = Gson()
//
//            val msgDto = gson.fromJson(data, Hl7OrmDto::class.java)
        //println(msgDto)

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
        msh.messageControlID.value = msgDto?.messageControlId
        msh.getSendingApplication().getNamespaceID().value = "HISD3"
        msh.sendingFacility.namespaceID.value = msgDto.hospitalName

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)
        msh.dateTimeOfMessage.time.value=formatted

        // Populate the PID Segment
        var pid = orm.getPATIENT().getPID()
        pid.getPatientName(0).getFamilyName().surname.value =msgDto?.pidLastName
        pid.getPatientName(0).getGivenName().value =msgDto?.pidFirstName
        pid.getPatientName(0).getSuffixEgJRorIII().setValue(msgDto?.pidExtName?:"")
        pid.dateTimeOfBirth.time.value = msgDto?.pidDob
        pid.getPatientAddress(0).getCity().setValue(msgDto.pidCity)
        pid.getPatientAddress(0).getCountry().setValue(msgDto.pidCountry)
        pid.getPatientAddress(0).streetAddress.streetName.value =msgDto.pidAddress
        pid.getPatientAddress(0).stateOrProvince.value=msgDto?.pidProvince
        pid.getPatientAddress(0).zipOrPostalCode.value=msgDto?.pidZip
        pid.patientID.idNumber.value = msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).idNumber.value=msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).checkDigit.value=""
        pid.administrativeSex.value =msgDto?.pidGender


        // Populate the PV1 Segment
        var pv1 = orm.getPATIENT().getPATIENT_VISIT().getPV1()
        pv1.getPatientClass().setValue(msgDto.pv1PatientClass)
        pv1.visitNumber.idNumber.value =msgDto.pv1VisitNumer
        pv1.patientType.value=msgDto.pv1PatientClass
        pv1.assignedPatientLocation.bed.value=msgDto.bed
        pv1.getAttendingDoctor(0).givenName.value=msgDto.pv1RequestingDrFname
        pv1.getAttendingDoctor(0).familyName.surname.value=msgDto.pv1RequestingDrLname
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1RequestingDrId

        var orc = orm.getORDER(0).getORC()
        orc.orc1_OrderControl.value="NW"
        orc.placerOrderNumber.universalID.value =msgDto.orcPlacerOrderNumber
        orc.orderStatus.value="SC"
        orc.getEnteredBy(0).idNumber.value=msgDto.pv1RequestingDrId
        orc.getEnteredBy(0).familyName.surname.value =msgDto.pv1RequestingDrMname
        orc.enteringOrganization.identifier.value= msgDto.hospitalName
        orc.placerOrderNumber.entityIdentifier.value = msgDto?.obrFileOrderNumber
        orc.fillerOrderNumber.entityIdentifier.value = msgDto?.obrFileOrderNumber
        // Populate the OBR Segment
        var obr = orm.getORDER(0).getORDER_DETAIL().getOBR()

        obr.placerOrderNumber.entityIdentifier.value  = msgDto.obrFileOrderNumber
        obr.getFillerOrderNumber().entityIdentifier.value =  msgDto.obrFileOrderNumber
        obr.setIDOBR.value = msgDto.obrFileOrderNumber
        obr.universalServiceIdentifier.identifier.value = msgDto.obrServiceIdentifier
        obr.universalServiceIdentifier.text.value = msgDto.obrServiceName
        obr.requestedDateTime.time.value =msgDto.obrRequestDate
        obr.observationDateTime.time.value  = msgDto.obrObservationDate
        var priority:String?
        if(msgDto.obrPriority == true){
            priority  = "STAT"
        }else{
            priority = "ROUTINE"
        }
        obr.priorityOBR.value = priority
        obr.getOrderingProvider(0).idNumber
        obr.scheduledDateTime.time.value=msgDto.obrRequestDate

        /*
         * In other situation, more segments and fields would be populated
         */
        // Now, let's encode the message and look at the output
        var  encodedMessage = parser.encode(orm)
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


            if (msgDto.integratedFacilities == IntegratedFacilities.RIS) {
                return   httpSender(risHost,risPort,orm)
            }

            else {
                return  dirWritter(msgDto, smbHost, smbUser, smbPass, smbUrl, encodedMessage)
            }

        }

    fun createAdtMsg(msgDto: Hl7OrmDto, risHost: String?, risPort:String?,smbUrl:String?,smbUser:String?,smbPass:String?,smbHost:String?):String? {


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
        msh.getSendingApplication().getNamespaceID().value = "HISD3"
        msh.sendingFacility.namespaceID.value = msgDto.hospitalName

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
        pid.getPatientAddress(0).getCountry().setValue(msgDto.pidCountry)
        pid.getPatientAddress(0).stateOrProvince.value=msgDto?.pidProvince
        pid.getPatientAddress(0).zipOrPostalCode.value=msgDto?.pidZip
        pid.patientID.idNumber.value = msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).idNumber.value=msgDto?.pidPatientNo
        pid.getPatientIdentifierList(0).checkDigit.value=""
        pid.administrativeSex.value =msgDto?.pidGender

        // Populate the PV1 Segment
        var pv1 = adt.getPV1()
        pv1.getPatientClass().setValue(msgDto.pv1PatientClass)
        pv1.visitNumber.idNumber.value =msgDto.pv1VisitNumer
        pv1.patientType.value=msgDto.pv1PatientClass
        pv1.assignedPatientLocation.bed.value=msgDto.bed
        pv1.getAttendingDoctor(0).givenName.value=msgDto.pv1RequestingDrFname
        pv1.getAttendingDoctor(0).familyName.surname.value=msgDto.pv1RequestingDrLname
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1RequestingDrId

        var  encodedMessage = parser.encode(adt)
        val useTls = false // Should we use TLS/SSL?

        if (msgDto.integratedFacilities == IntegratedFacilities.RIS) return   httpSender(risHost,risPort,adt)

        else {
            return    dirWritter(msgDto, smbHost, smbUser, smbPass, smbUrl, encodedMessage)
        }

    }

    fun httpSender(risHost: String?,risPort: String?,rawmsg:Message): String? {

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

        var gson = Gson()
        val useTls = false // Should we use TLS/SSL?
        var connection = context.newClient(risHost, risPort!!.toInt(), useTls)
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


    fun dirWritter(msgDto: Hl7OrmDto,smbHost: String?,smbUser: String?,smbPass: String?,smbUrl: String?,encodedMessage: String): String? {

        var gson = Gson()
        try {
            /** writting files to shared folder in a network wiht credentials**/

            val ntlmPasswordAuthentication = NtlmPasswordAuthentication(smbHost,smbUser, smbPass)

            val shared = smbUrl+"/Order"
            val directory = SmbFile(shared,ntlmPasswordAuthentication)

                try{
                    if (! directory.exists()) {
                        directory.mkdir()
                    }
                }catch(e: IOException) {
                    throw IllegalArgumentException(e.message)
                    e.printStackTrace()
                }

                val path = smbUrl+"/Order/"+msgDto.messageControlId+".hl7"
                val sFile = SmbFile(path, ntlmPasswordAuthentication)
                var sfos =  SmbFileOutputStream(sFile)
                sfos.write(encodedMessage.toByteArray())
                sfos.close()

            /*** writting files in local shared folder***/
//                var file = Paths.get("//localhost/Shared/Outbox/"+msgDto.msh.messageControlId+".hl7")
//                Files.write(file,encodedMessage.toByteArray())

        }catch(e: IOException) {
            throw IllegalArgumentException(e.message)
            e.printStackTrace()
        }
        return gson.toJson("ok")
    }

