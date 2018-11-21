package com.hisd3.utils.hl7service

import java.time.ZoneId
import HL7DateTimeParser
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class HL7DateTime
constructor(dateTime: String) {

    val localDateTime: LocalDateTime
    val precision: String
    val formatter: DateTimeFormatter
    private val hasTimeComponent: Boolean?

    init {
        localDateTime = HL7DateTimeParser().parse(dateTime)!!
        precision = HL7DateTimeParser().getPattern(dateTime)
        formatter = DateTimeFormatter.ofPattern(precision)
        hasTimeComponent = dateTime.length > 8
    }

    fun asDate(): Date {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }

    fun hasTimeComponent(): Boolean {
        return hasTimeComponent!!
    }

    override fun toString(): String {
        return localDateTime.toString()
    }
}