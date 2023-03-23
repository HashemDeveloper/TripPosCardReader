package com.limosys.test.tripostestapp.objects

enum class TransactionDialogType(var isShow: Boolean, val type: String) {
    NONE(isShow = false, "none"),
    REFUND(isShow = true, "refund"),
    REVERSAL(isShow = true, "reversal")
}