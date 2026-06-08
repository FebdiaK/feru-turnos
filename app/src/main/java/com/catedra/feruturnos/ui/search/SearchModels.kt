package com.catedra.feruturnos.ui.search

import org.osmdroid.util.GeoPoint

data class EnclosureItem(
    val id: String = "",
    val name: String = "",
    val location: GeoPoint,
    val address: String = "",
    val phone: Long = 0,
    val amenities: List<String> = emptyList(),
    val fields: List<FieldItem> = emptyList()
    )

data class FieldItem(
    val id: String = "",
    val fieldName: String = "",
    val type: String = "",
    val price: Long = 0,
    val days: List<String> = emptyList(),
    val timeTable: List<String> = emptyList(),
    val description: String = ""
)