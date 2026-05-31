package com.catedra.feruturnos.ui.search

import org.osmdroid.util.GeoPoint

data class EnclosureItem(
    val id: String = "",
    val name: String = "",
    val location: GeoPoint
)