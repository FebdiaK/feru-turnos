package com.catedra.feruturnos.ui.search

import org.osmdroid.util.GeoPoint
import kotlin.math.*

fun calculateDistance(p1: GeoPoint, p2: GeoPoint): Double {
    val r = 6371
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)

    val a =
        sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) *
                cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return r * c
}