package com.example.simmanager.data

data class EsimProfile(
    val name: String,
    val activationCode: String, // format: "LPA:1$SMDP.ADDRESS$ACTIVATION_CODE"
    val carrier: String,
    val notes: String
)
