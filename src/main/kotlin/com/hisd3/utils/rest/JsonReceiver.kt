package com.hisd3.utils.rest

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.v27.segment.MSH
import ca.uhn.hl7v2.model.v25.message.ORM_O01
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.util.idgenerator.InMemoryIDGenerator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hisd3.utils.Dto.Hl7OrmDto
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileOutputStream
import org.json.JSONObject
import org.omg.CORBA.Object
import spark.Spark
import spark.Spark.post
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class JsonReceiver {

    fun createOrmMsg(data: String): String? {
        var gson = Gson()

            val msgDto = gson.fromJson(data, Hl7OrmDto::class.java)
        println(msgDto)

        var context = DefaultHapiContext()
        var mcf = CanonicalModelClassFactory("2.5")
        context.setModelClassFactory(mcf)

            var orm = ORM_O01()
            orm.initQuickstart("ORM","O01","P")
           // orm.initQuickstart(msgDto.msh.messageCode, msgDto.msh.messageTriggerEvent, "D")
            var parser = context.getPipeParser()
            parser.getParserConfiguration().setIdGenerator(InMemoryIDGenerator())
//            val adt = p.parse(msg)


        // Populate the MSH Segment
        var msh = orm.getMSH()
        msh.getSendingApplication().getNamespaceID().value = msgDto.msh.hospitalName
        msh.getSendingFacility().getNamespaceID().setValue(msgDto.msh.sendingFacility)
        msh.dateTimeOfMessage

        // Populate the PID Segment
        var pid = orm.getPATIENT().getPID()
        pid.getPatientName(0).getFamilyName().surname.value =msgDto?.pid?.pidLastName
        pid.getPatientName(0).getGivenName().value =msgDto?.pid?.pidFirstName
        pid.getPatientName(0).getSuffixEgJRorIII().setValue(msgDto?.pid?.pidExtName?:"")
       // pid.dateTimeOfBirth.degreeOfPrecision.value = msgDto.pid?.pidDob?.toString("yyyyMMddHHmm")
        pid.getPatientAddress(0).getCity().setValue(msgDto.pid.pidCity)
        pid.getPatientAddress(0).getCountry().setValue(msgDto.pid.pidCountry)
        pid.getPatientAddress(0).streetAddress.streetName.value =msgDto.pid.pidAddress
        pid.getPatientAddress(0).stateOrProvince.value=msgDto?.pid?.pidProvince
        pid.getPatientAddress(0).zipOrPostalCode.value=msgDto?.pid?.pidZip
        pid.patientID.idNumber.value = msgDto?.pid?.pidPatientNo

        pid.administrativeSex.value =msgDto?.pid?.pidGender


        // Populate the PV1 Segment
        var pv1 = orm.getPATIENT().getPATIENT_VISIT().getPV1()
        pv1.getPatientClass().setValue(msgDto.pv1.pv1PatientClass)
        pv1.visitNumber.idNumber.value =msgDto.pv1.pv1VisitNumer

        pv1.getAttendingDoctor(0).givenName.value=msgDto.pv1.pv1RequestingDrFname
        pv1.getAttendingDoctor(0).familyName.surname.value=msgDto.pv1.pv1RequestingDrLname
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1.pv1RequestingDrId

        var orc = orm.getORDER(0).getORC()
        orc.orc1_OrderControl.value="NW"
        orc.placerOrderNumber.universalID.value =msgDto.orc.orcPlacerOrderNumber


        // Populate the OBR Segment
        var obr = orm.getORDER(0).getORDER_DETAIL().getOBR()

        obr.placerOrderNumber.universalID.value  = msgDto.obr.obrFileOrderNumber
        obr.getFillerOrderNumber().universalIDType.value =  msgDto.obr.obrFileOrderNumber
        obr.obr4_UniversalServiceIdentifier.ce1_Identifier.value=msgDto.obr.obrServiceIdentifier
        obr.obr4_UniversalServiceIdentifier.ce2_Text.value=msgDto.obr.obrServiceName
//        obr.requestedDateTime.degreeOfPrecision.value=msgDto.obr.obrRequestDate?.toString("yyyyMMddHHmm")
//        obr.observationEndDateTime.degreeOfPrecision.value = msgDto.obr.obrObservationDate?.toString("yyyyMMddHHmm")
        var priority:String?
        if(msgDto.obr.obrPriority == true){
            priority  = "STAT"
        }else{
            priority = "ROUTINE"
        }
        obr.priorityOBR.value = priority

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
        if (msgDto.recievingFacility.tcp == true) {

            try {
                var connection = context.newClient("127.0.0.1", 22223, useTls)
                var initiator = connection.initiator
                var response = initiator.sendAndReceive(orm)

                connection.close()
                //System.out.println("Received response:\n" + responseString)
                var gson = Gson()
                return gson.toJson(response)

            } catch (e: IOException) {
                throw IllegalArgumentException(e.message)
                throw HL7Exception(e)
            }
        }
        else{

            try {
                /** writting files to shared folder in a network wiht credentials**/
                val ntlmPasswordAuthentication = NtlmPasswordAuthentication(msgDto.recievingFacility.ipAddress, msgDto.facilityCredentials.userLogin, msgDto.facilityCredentials.passLogin)
                val user = msgDto.facilityCredentials.userLogin+":"+msgDto.facilityCredentials.passLogin
                val auth = NtlmPasswordAuthentication(user)

                val smbUrl = "smb://"+msgDto.recievingFacility.ipAddress+"/"+msgDto.recievingFacility.smbUrl+"/New"
                val directory = SmbFile(smbUrl,ntlmPasswordAuthentication)

                try{
                    if (! directory.exists()) {
                        directory.mkdir()
                    }
                }catch(e: IOException) {
                    throw IllegalArgumentException(e.message)
                    e.printStackTrace()
                }

                val path = "smb://"+msgDto.recievingFacility.ipAddress+"/"+msgDto.recievingFacility.smbUrl+"/New"+msgDto.pv1.pv1VisitNumer+".hl7"
                val sFile = SmbFile(path, ntlmPasswordAuthentication)
                var sfos =  SmbFileOutputStream(sFile)
                sfos.write(encodedMessage.toByteArray())
                sfos.close()

                /*** writting files in local shared folder***/
                var file = Paths.get("//localhost/Shared/Outbox/"+msgDto.pv1.pv1VisitNumer+".hl7")
                Files.write(file,encodedMessage.toByteArray())

                return gson.toJson("ok")

            }catch(e: IOException) {
                throw IllegalArgumentException(e.message)
                e.printStackTrace()
            }
        }

        }

}