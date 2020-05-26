package com.droidx.trivianest.model

data class Contact(var id: String, var name: String) {
    private val _numbers: MutableList<ContactPhone> = mutableListOf()

    val numbers: List<ContactPhone>
        get() = _numbers

    fun addNumber(number: String?, type: String?) {
        if (type != null && number != null)
            _numbers.add(ContactPhone(number, type))
    }
}