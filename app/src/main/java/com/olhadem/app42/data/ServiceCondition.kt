package com.olhadem.app42.data

/**
 * A simple 3-level summary of how the car is doing, similar to the
 * "health" indicators used in real car companion apps (e.g. MyŠkoda, MyAudi).
 */
enum class ServiceCondition {
    EXCELLENT,
    GOOD,
    POOR
}

/**
 * Turns the raw telemetry numbers from [CarStatus] into one [ServiceCondition] label.
 * This keeps the label driven by the same random numbers instead of being hardcoded.
 */
fun calculateServiceCondition(status: CarStatus): ServiceCondition {
    val oilScore = status.oilLevel.toDouble()
    val engineScore = if (status.engineOk) 100.0 else 20.0

    val idealPressure = 2.3
    val avgDeviation = status.tirePressure.map { kotlin.math.abs(it - idealPressure) }.average()
    val tireScore = (100.0 - (avgDeviation * 150)).coerceIn(0.0, 100.0)

    val averageScore = (oilScore + engineScore + tireScore) / 3.0

    return when {
        averageScore >= 75 -> ServiceCondition.EXCELLENT
        averageScore >= 45 -> ServiceCondition.GOOD
        else -> ServiceCondition.POOR
    }
}
