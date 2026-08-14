package com.olhadem.app42.data

data class CarStatus(
    val isLocked: Boolean,
    val engineOk: Boolean,
    val oilLevel: Int, // 0 to 100
    val tirePressure: List<Double>, // Pressure for 4 wheels
    val fuelRange: Int, // in km
    val mileage: Int, // total odometer reading, in km
    val nextServiceKm: Int // km remaining until the next scheduled service
)
