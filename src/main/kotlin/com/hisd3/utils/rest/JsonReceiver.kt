package com.hisd3.utils.rest

import ca.uhn.hl7v2.DefaultHapiContext
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.v25.message.ORM_O01
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.util.idgenerator.InMemoryIDGenerator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.hisd3.utils.Dto.Hl7OrmDto
import org.json.JSONObject
import org.omg.CORBA.Object
import spark.Spark
import spark.Spark.post
import java.io.IOException

class JsonReceiver {
    fun jsonParse(data: String): String? {
        var gson = Gson()

            val msgDto = gson.fromJson(data, Hl7OrmDto::class.java)
        println(msgDto)

            val useTls = false // Should we use TLS/SSL?
            var context = DefaultHapiContext()
            var mcf = CanonicalModelClassFactory("2.5")
            context.setModelClassFactory(mcf)

            var orm = ORM_O01()
            orm.initQuickstart(msgDto.msh.messageCode, msgDto.msh.messageTriggerEvent, "D")
            var parser = context.getPipeParser()
            parser.getParserConfiguration().setIdGenerator(InMemoryIDGenerator())
//            val adt = p.parse(msg)
            var connection = context.newClient(msgDto.recievingFacility.ipAddress, 22223, useTls)

        // Populate the MSH Segment
        var msh = orm.getMSH()
        msh.getSendingApplication().getNamespaceID().setValue(msgDto.msh.hospitalName)
        msh.getSendingFacility().getNamespaceID().setValue(msgDto.msh.sendingFacility)
        msh.dateTimeOfMessage.time

        // Populate the PID Segment
        var pid = orm.getPATIENT().getPID()
        pid.getPatientName(0).getFamilyName().getSurname().setValue(msgDto?.pid?.pidLastName)
        pid.getPatientName(0).getGivenName().setValue(msgDto?.pid?.pidFirstName)
        pid.getPatientName(0).getSuffixEgJRorIII().setValue(msgDto?.pid?.pidExtName?:"")
        pid.getDateTimeOfBirth().time.value = msgDto.pid?.pidDob?.toString("yyyyMMddHHmm")
        pid.getPatientAddress(0).getCity().setValue(msgDto?.pid?.pidCity)
        pid.getPatientAddress(0).getCountry().setValue(msgDto?.pid?.pidCountry)
        pid.getPatientAddress(0).streetAddress.streetName.value=msgDto?.pid?.pidAddress
        pid.getPatientAddress(0).stateOrProvince.value=msgDto?.pid?.pidProvince
        pid.getPatientAddress(0).zipOrPostalCode.value=msgDto?.pid?.pidZip
        pid.patientID.idNumber.value=msgDto?.pid?.pidPatientId
        pid.getPatientIdentifierList(0).idNumber.value=msgDto?.pid?.pidPatientNo
        pid.administrativeSex.value=msgDto?.pid?.pidGender
        pid.getCitizenship(0).identifier.value=msgDto?.pid?.pidCitizenship

        var pv1 = orm.getPATIENT().getPATIENT_VISIT().getPV1()
        pv1.getVisitNumber().getIDNumber().setValue(msgDto.pv1.pv1VisitNumer)
        pv1.visitNumber.idNumber.value=msgDto.pv1.pv1VisitNumer
        pv1.getAttendingDoctor(0).givenName.value=msgDto.pv1.pv1RequestingDrFname
        pv1.getAttendingDoctor(0).familyName.surname.value=msgDto.pv1.pv1RequestingDrLname
        pv1.getAttendingDoctor(0).idNumber.value=msgDto.pv1.pv1RequestingDrId

        var orc = orm.getORDER(0).getORC()
        orc.orc1_OrderControl.value="NW"
        orc.getPlacerOrderNumber().universalID.value=msgDto.orc.orcPlacerOrderNumber


        var obr = orm.getORDER(0).getORDER_DETAIL().getOBR()
        obr.setIDOBR.value=msgDto.obr.obrFileOrderNumber
        obr.placerOrderNumber.universalID.value=msgDto.obr.obrPlaceOrderNumber
        obr.obr4_UniversalServiceIdentifier.ce1_Identifier.value=msgDto.obr.obrServiceIdentifier
        obr.obr4_UniversalServiceIdentifier.ce2_Text.value=msgDto.obr.obrServiceName
        obr.obr6_RequestedDateTime.time.value=msgDto.obr.obrRequestDate?.toString("yyyyMMddHHmm")
        obr.obr7_ObservationDateTime.time.value=msgDto.obr.obrObservationDate?.toString("yyyyMMddHHmm")

        /*
         * In other situation, more segments and fields would be populated
         */
        // Now, let's encode the message and look at the output

            try {

                var initiator = connection.initiator
                var response = initiator.sendAndReceive(orm)

                //connection.close()
                //System.out.println("Received response:\n" + responseString)
                var gson = Gson()
                return gson.toJson(response)

            } catch (e: IOException) {
                throw IllegalArgumentException(e.message)
                throw HL7Exception(e)
            } finally {
                connection.close()
            }

        }

}