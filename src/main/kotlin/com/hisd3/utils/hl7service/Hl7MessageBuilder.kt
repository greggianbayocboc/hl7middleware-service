package com.hisd3.utils.hl7service

import ca.uhn.hl7v2.model.DataTypeException
import java.io.IOException
import ca.uhn.hl7v2.HL7Exception
import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.model.v25.message.ADT_A01
import ca.uhn.hl7v2.model.v25.message.ORM_O01
import ca.uhn.hl7v2.model.v25.segment.MSH
import ca.uhn.hl7v2.model.v25.segment.PID
import ca.uhn.hl7v2.model.v25.segment.PV1
import java.sql.Timestamp
import java.util.*
import sun.rmi.registry.RegistryImpl.getID




class Hl7MessageBuilder {

    @Throws(HL7Exception::class, IOException::class)
    fun Build() {

        val currentDateTimeString = Timestamp(Date().getTime())

        var orm = ORM_O01()

        orm.initQuickstart("ORM","O01","P")
        //_adtMessage = ADT_A01()
        //you can use the context class's newMessage method to instantiate a message if you want
       // _adtMessage.initQuickstart("ADT", "A01", "P")
        createMshSegment(orm.getMSH())
       // createEvnSegment(currentDateTimeString)
        createPidSegment(orm.getPATIENT().getPID())
        createPv1Segment(orm.getPATIENT().getPATIENT_VISIT().getPV1())
        return
    }

    @Throws(DataTypeException::class)

    fun createMshSegment(mshSegment : MSH) : MSH {

        val currentDateTimeString = Timestamp(Date().getTime())
        mshSegment.getFieldSeparator().setValue("|")
        mshSegment.getEncodingCharacters().setValue("^~\\&")
        mshSegment.getSendingApplication().getNamespaceID().setValue("Our System")
        mshSegment.getSendingFacility().getNamespaceID().setValue("Our Facility")
        mshSegment.getReceivingApplication().getNamespaceID().setValue("Their Remote System")
        mshSegment.getReceivingFacility().getNamespaceID().setValue("Their Remote Facility")
        mshSegment.getDateTimeOfMessage().getTime().setValue(currentDateTimeString)
        mshSegment.getMessageControlID().setValue("111111")
        mshSegment.getVersionID().getVersionID().setValue("2.4")

        return mshSegment

    }

    @Throws(DataTypeException::class)
    fun createPidSegment(pid : PID) : PID {

        val patientName = pid.getPatientName(0)
        patientName.getFamilyName().getSurname().setValue("Mouse")
        patientName.getGivenName().setValue("Mickey")
        pid.getPatientIdentifierList(0).getIDNumber().setValue("378785433211")
        val patientAddress = pid.getPatientAddress(0)
        patientAddress.getStreetAddress().getStreetOrMailingAddress().setValue("123 Main Street")
        patientAddress.getCity().setValue("Lake Buena Vista")
        patientAddress.getStateOrProvince().setValue("FL")
        patientAddress.getCountry().setValue("USA")

        return  pid
    }

    @Throws(DataTypeException::class)
    fun  createPv1Segment (pv1 : PV1):PV1 {

        pv1.getPatientClass().setValue("O") // to represent an 'Outpatient'
        val assignedPatientLocation = pv1.getAssignedPatientLocation()
        assignedPatientLocation.getFacility().getNamespaceID().setValue("Some Treatment Facility Name")
        assignedPatientLocation.getPointOfCare().setValue("Some Point of Care")
        pv1.getAdmissionType().setValue("ALERT")
        val referringDoctor = pv1.getReferringDoctor(0)
        referringDoctor.getIDNumber().setValue("99999999")
        referringDoctor.getFamilyName().getSurname().setValue("Smith")
        referringDoctor.getGivenName().setValue("Jack")
        referringDoctor.getIdentifierTypeCode().setValue("456789")
        //pv1.getAdmitDateTime().getTimeOfAnEvent().setValue(getCurrentTimeStamp())

        return pv1
    }
}