package com.pfortbe22bgrupo2.architectapp.listeners

interface AuthResultListener {

    fun onAuthSuccess()
    fun onAuthFailure(errorMessage: String)
}