package com.limosys.test.tripostestapp.utils

sealed class MessageState {
    class INFO(val message: String): MessageState()
    class WARNING(val message: String): MessageState()
    class ALERT(val message: String): MessageState()
    class ERROR(val message: String): MessageState()
}