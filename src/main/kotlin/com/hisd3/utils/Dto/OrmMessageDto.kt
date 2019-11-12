package com.hisd3.utils.Dto

import com.hisd3.utils.customtypes.IntegratedFacilities


class OrmMessageDto {
    var msg  = MshSegment()
    var pid  = PidSegment()
    var pv1 = Pv1Segment ()
    var orc = OrcSegment()
    var obr = ObrSegment ()
    var addons = AddOns ()
}

class MshSegment {
    var messageCode = ""
    var messageTriggerEvent = ""
    var hospitalName = ""
    var sendingFacility = ""
    var messageControlId = ""
    var sendingApplication = ""
    var recievingApplication = ""
}

class PidSegment{
    var pidLastName = ""
    var pidFirstName = ""
    var pidMiddleName = ""
    var pidExtName = ""
    var pidGender = ""
    var pidDob = ""
    var pidCity  = ""
    var pidCountry = ""
    var pidAddress =""
    var pidProvince =""
    var pidZip = ""
    var pidPatientId = ""
    var pidPatientNo = ""
    var pidCitizenship = ""
    var pidAlternatePid = ""
    var pidFullname = ""
}

class Pv1Segment{
    var pv1VisitNumer = ""
    var pv1CaseNumer = ""
    var pv1PatientClass = ""
    var pv1ReferringDoctorID = ""
    var pv1RequestingDrFname = ""
    var pv1RequestingDrLname = ""
    var pv1RequestingDrMname = ""
    var bed = ""
    var pv1Room = ""
}

class OrcSegment{
    var orcOrderControl = ""
    var orcPlacerOrderNumber = ""
    var orcRequestingDrId = ""
    var orcRequestingDrFname = ""
    var orcRequestingDrLname = ""
    var orcRequestingDrMname = ""
}

class ObrSegment {
    var obrPlaceOrderNumber = ""
    var obrFileOrderNumber = ""
    var obrServiceIdentifier = ""
    var serviceCategory = ""
    var obrServiceName = ""
    var obrPriority = false
    var obrRequestDate = ""
    var obrObservationDate = ""
    var modalityType = ""
    var diagnosticSev = ""
    var obrArray = ArrayList<Obritem>()

}

class Obritem{
    var identifier = ""
    var nameservice = ""
    var observationDate = ""
    var orcRequestingDrId = ""
    var orcRequestingDrFname = ""
    var obrPriority = "ROUTINE"
    var obrFileOrderNumber = ""
    var orcPlacerOrderNumber = ""
}

class AddOns{
    var tcp = false
    var ipAddress = ""
    var port = 0
    var smbUrl = ""
    var integratedFacilities = IntegratedFacilities.LIS
    var userLogin = ""
    var passLogin = ""
}