package com.hisd3.utils.Dto

import javafx.scene.input.DataFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class Hl7OrmDto {
    var msh = Msh()
    var recievingFacility = ReceivingFacility()
    var facilityCredentials = FacilityCredentials()
    var pid = Pid()
    var pv1 = Pv1()
    var orc = Orc()
    var obr = Obr()
}

class Msh(
        var messageCode: String?="",
        var messageTriggerEvent:String?="",
        var hospitalName:String?="",
        var sendingFacility: String?=""
)
class Pid(
        var pidLastName:String?="",
        var pidFirstName:String?="",
        var pidExtName:String?="",
        var pidGender:String?="",
        var pidDob:DateTime?=DateTime.now(),
        var pidCity:String?="",
        var pidCountry:String?="",
        var pidAddress:String?="",
        var pidProvince:String?="",
        var pidZip:String?="",
        var pidPatientId:String?="",
        var pidPatientNo:String?="",
        var pidCitizenship:String?=""
)
class Pv1(
        var pv1VisitNumer:String?="",
        var pv1CaseNumer:String?="",
        var pv1PatientClass:String?="",
        var pv1RequestingDrId:String?="",
        var pv1RequestingDrFname:String?="",
        var pv1RequestingDrLname:String?="",
        var pv1RequestingDrMname:String?=""
)
class Orc(
        var orcOrderControl:String?="",
        var orcPlacerOrderNumber:String?="",
        var orcRequestingDrId:String?="",
        var orcRequestingDrFname:String?="",
        var orcRequestingDrLname:String?="",
        var orcRequestingDrMname:String?=""
)
class Obr(
        var obrPlaceOrderNumber:String?="",
        var obrFileOrderNumber:String?="",
        var obrServiceIdentifier:String?="",
        var obrServiceName:String?="",
        var obrPriority:String?="",
        var obrRequestDate:DateTime?=DateTime.now(),
        var obrObservationDate:DateTime?=DateTime.now()
)

class ReceivingFacility(
        var tcp: Boolean?=false,
        var ipAddress:String?="",
        var port:String?="",
        var smbUrl:String?=""
)
class FacilityCredentials(
        var userLogin:String?="",
        var passLogin:String?=""
)