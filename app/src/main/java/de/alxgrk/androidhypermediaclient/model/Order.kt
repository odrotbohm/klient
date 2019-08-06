package de.alxgrk.androidhypermediaclient.model

import java.util.*

interface Order {
    fun getId(): String
    fun getStatus(): String
    fun getOrderedDate(): Date
}