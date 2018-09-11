package com.hisd3.utils.Dto

import javafx.scene.input.DataFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class Hl7OrmDto {
//    var msh = Msh()
//    var recievingFacility = ReceivingFacility()
//    var facilityCredentials = FacilityCredentials()
//    var pid = Pid()
//    var pv1 = Pv1()
//    var orc = Orc()
//    var obr = Obr()


    var messageCode: String?=null
    var messageTriggerEvent:String?=null
    var hospitalName:String?=null
    var sendingFacility: String?=null
    var messageControlId:String?=null


    var pidLastName:String?=null
    var pidFirstName:String?=null
    var pidExtName:String?=null
    var pidGender:String?=null
    var pidDob:String?= ""
    var pidCity:String?=null
    var pidCountry:String?=null
    var pidAddress:String?=null
    var pidProvince:String?=null
    var pidZip:String?=null
    var pidPatientId:String?=null
    var pidPatientNo:String?=null
    var pidCitizenship:String?=null

    var pv1VisitNumer:String?=null
    var pv1CaseNumer:String?=null
    var pv1PatientClass:String?=null
    var pv1RequestingDrId:String?=null
    var pv1RequestingDrFname:String?=null
    var pv1RequestingDrLname:String?=null
    var pv1RequestingDrMname:String?=null
    var bed:String?=null
    var pv1Room:String?=null

    var orcOrderControl:String?=null
    var orcPlacerOrderNumber:String?=null
    var orcRequestingDrId:String?=null
    var orcRequestingDrFname:String?=null
    var orcRequestingDrLname:String?=null
    var orcRequestingDrMname:String?=null

    var obrPlaceOrderNumber:String?=null
    var obrFileOrderNumber:String?=null
    var obrServiceIdentifier:String?=null
    var obrServiceName:String?=null
    var obrPriority:Boolean?=false
    var obrRequestDate:String?=""
    var obrObservationDate:String?=""


    var tcp: Boolean?=false
    var ipAddress:String?=null
    var port:Int?=null
    var smbUrl:String?=null

    var userLogin:String?=null
    var passLogin:String?=null
}