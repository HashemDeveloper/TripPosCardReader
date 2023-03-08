package com.limosys.test.tripostestapp.repo

interface ISharedPref {
    fun setIdentifier(identifier: String)
    fun getIdentifier(): String
}