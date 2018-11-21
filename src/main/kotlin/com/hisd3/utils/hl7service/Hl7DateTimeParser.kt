import java.time.LocalDateTime
import ca.uhn.hl7v2.HL7Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class HL7DateTimeParser {

     private val YYYY = 4
     private val YYYYMM = 6
     private val YYYYMMDD = 8
     private val YYYYMMDDHHMM = 12
     private val YYYYMMDDHHMMSS = 14
     private val YYYYMMDDHHMMSS_T = 16
     private val YYYYMMDDHHMMSS_TT = 17
     private val YYYYMMDDHHMMSS_TTT = 18
     private val YYYYMMDDHHMMSS_TTTT = 19

    @Throws(HL7Exception::class)
    fun parse(dateTime: String?): LocalDateTime? {
        Objects.requireNonNull(dateTime)

        var cleanDateTime = dateTime?.trim { it <= ' ' }

        if ("" == cleanDateTime) {
            return null
        }

        if (!isValidTs(cleanDateTime))
             HL7Exception.APPLICATION_INTERNAL_ERROR


        if (cleanDateTime!!.length < 8) {
            cleanDateTime = handleShortDates(cleanDateTime)
        }

        val pattern = getPattern(cleanDateTime)
        val formatter = DateTimeFormatter.ofPattern(pattern)

        return if (cleanDateTime.length == 8) {
            LocalDate.parse(cleanDateTime, formatter).atStartOfDay()
        } else LocalDateTime.parse(cleanDateTime, formatter)

    }

     fun handleShortDates(dateTime: String): String {
        if (dateTime.length == 4) {
            return dateTime + "0101"
        }

        return if (dateTime.length == 6) {
            dateTime + "01"
        } else dateTime

    }

    @Throws(HL7Exception::class)
    fun getPattern(dateTime: String): String {
        when (dateTime.length) {
            YYYY -> return "yyyy"
            YYYYMM -> return "yyyy-MM"
            YYYYMMDD -> return "yyyyMMdd"
            YYYYMMDDHHMM -> return "yyyyMMddHHmm"
            YYYYMMDDHHMMSS -> return "yyyyMMddHHmmss"
            YYYYMMDDHHMMSS_T -> return "yyyyMMddHHmmss.S"
            YYYYMMDDHHMMSS_TT -> return "yyyyMMddHHmmss.SS"
            YYYYMMDDHHMMSS_TTT -> return "yyyyMMddHHmmss.SSS"
            YYYYMMDDHHMMSS_TTTT -> return "yyyyMMddHHmmss.SSSS"
            else -> return HL7Exception.APPLICATION_INTERNAL_ERROR.toString()
        }
    }

    fun isValidTs(dateTime:String?):Boolean {
        if (dateTime == null) {
            return false
        }
        var  regex  = "([12]\\d{3}" + "((0[1-9]|1[0-2])" + "((0[1-9]|[12]\\d|3[01])" + "(([01]\\d|2[0-3])" + "([0-5]\\d" + "([0-5]\\d" + "(\\.\\d\\d?\\d?\\d?)?)?)?)?)?)?"+"((\\+|\\-)([01]\\d|2[0-3])[0-5]\\d)?)?"
        return dateTime.matches(Regex.fromLiteral(regex))
    }
 }