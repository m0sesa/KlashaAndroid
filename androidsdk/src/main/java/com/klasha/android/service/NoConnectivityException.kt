package com.klasha.android.service

import java.io.IOException

class NoConnectivityException: IOException() {
    override val message: String
        get() = "No Internet Connection"
}