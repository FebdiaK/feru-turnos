package com.catedra.feruturnos.data.model

import org.osmdroid.util.GeoPoint

data class Court(
    val id: String,
    val name: String,
    val address: String,
    val sport: String,
    val pricePerHour: Int,
    val rating: Float,
    val location: GeoPoint,
    val hasLights: Boolean = true,
    val surface: String = "Césped sintético"
)

//canchas harcodeadas, hay que pasarlas a firestore
val hardcodedCourts = listOf(
    Court(
        id = "1",
        name = "Los Manzanos",
        address = "Av. Mitre 1230, Bernal",
        sport = "Fútbol 5",
        pricePerHour = 8000,
        rating = 4.5f,
        location = GeoPoint(-34.7065, -58.2792)
    ),
    Court(
        id = "2",
        name = "Club Atlético Quilmes",
        address = "Rivadavia 456, Quilmes",
        sport = "Fútbol 5",
        pricePerHour = 7500,
        rating = 4.2f,
        location = GeoPoint(-34.7205, -58.2528)
    ),
    Court(
        id = "3",
        name = "Complejo El Estadio",
        address = "Av. Calchaquí 890, Florencio Varela",
        sport = "Fútbol 7",
        pricePerHour = 9000,
        rating = 4.8f,
        location = GeoPoint(-34.8012, -58.2756)
    ),
    Court(
        id = "4",
        name = "Sport Center Berazategui",
        address = "Calle 14 Nro 2100, Berazategui",
        sport = "Fútbol 5",
        pricePerHour = 6500,
        rating = 3.9f,
        location = GeoPoint(-34.7631, -58.2105)
    ),
    Court(
        id = "5",
        name = "La Cancha de Bera",
        address = "Av. Montevideo 3400, Berazategui",
        sport = "Fútbol 11",
        pricePerHour = 12000,
        rating = 4.6f,
        location = GeoPoint(-34.7580, -58.2190)
    )
)