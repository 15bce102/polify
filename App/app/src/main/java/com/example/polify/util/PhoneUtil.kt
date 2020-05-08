package com.example.polify.util

fun isValidPhoneNumber(fullPhoneNumber: String) =
        (fullPhoneNumber.length == 13) && fullPhoneNumber[0] == '+'