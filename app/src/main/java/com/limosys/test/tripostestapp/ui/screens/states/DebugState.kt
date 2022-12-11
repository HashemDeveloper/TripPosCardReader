package com.limosys.test.tripostestapp.ui.screens.states

sealed class DebugState {
    object OnDebug: DebugState()
    object None : DebugState()
    class DebugList(val list: MutableList<String>): DebugState()
}
