package com.limosys.test.tripostestapp.objects

enum class TriPOSTransactionType(var isShow: Boolean, val type: String) {
    NONE(isShow = false, "none"),
    SALE(isShow = true, "sale"),
    REFUND(isShow = true, "refund"),
    REVERSAL(isShow = true, "reversal"),
    RETURN(isShow = true, "return")
}