package com.hisd3.utils

import com.hisd3.utils.rest.JsonReceiverV2

class ServiceRunner {

    fun run() {
        initControllers()
    }

    private fun initControllers() {
        JsonReceiverV2()
    }
}