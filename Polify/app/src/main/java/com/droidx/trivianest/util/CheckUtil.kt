package com.droidx.trivianest.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import splitties.systemservices.telephonyManager
import java.util.*


fun Context.isValidPhoneNumber(number: String, countryCode: String): Boolean {
    val util = PhoneNumberUtil.createInstance(this)
    val regionCode = util.getRegionCodeForCountryCode(countryCode.toInt())

    Log.d("numLog", "number=$number, code=$countryCode, region=$regionCode")

    return try {
        val phone = util.parse(number, regionCode)
        Log.d("numLog", "parse done. valid = ${util.isValidNumberForRegion(phone, regionCode)}")
        util.isValidNumberForRegion(phone, regionCode)
    } catch (e: NumberParseException) {
        Log.d("numLog", "could not parse")
        e.printStackTrace()
        false
    }
}


fun isValidUserName(userName: String) =
        userName.isNotBlank()

fun List<String>.toFullPhoneNumbers(context: Context): List<String> {
    val simIso = telephonyManager.simCountryIso.toUpperCase(Locale.US).trim()
    val util = PhoneNumberUtil.createInstance(context)
    val countryCode = util.getCountryCodeForRegion(simIso).toString()
    val dummy = util.format(util.getExampleNumber(simIso), PhoneNumberUtil.PhoneNumberFormat.E164)

    Log.d("contactLog", "dummy = $dummy")
    Log.d("contactLog", "country code = $countryCode")

    return this.map { number ->
        Log.d("contactLog", "number = $number")
        try {
            val num = util.format(util.parse(number, simIso), PhoneNumberUtil.PhoneNumberFormat.E164)
            Log.d("contactLog", "parsed success = $num")
            num
        } catch (e: NumberParseException) {
            if (number[0] != '+') {
                if (number.length == dummy.length - 1) {
                    Log.d("contactLog", "only plus missing: $number")
                    "+$number"
                } else {
                    Log.d("contactLog", "country code missing: $number")
                    "+$countryCode$number"
                }
            } else {
                Log.d("contactLog", "number starts with +: $number")
                number
            }
        }
    }
}

fun Context.launchSettingsIntent() {
    val uri = Uri.fromParts("package", packageName, null)

    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
    startActivity(intent)
}