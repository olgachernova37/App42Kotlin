package com.olhadem.app42.data

import com.olhadem.app42.R
import kotlin.random.Random

class CarRepository {

    fun getCars(): List<Car> {
        return listOf(
            Car(1, R.string.car_name_skoda_superb, R.drawable.car_skoda_new),
            Car(2, R.string.car_name_audi_a6, R.drawable.car_audi),
            Car(3, R.string.car_name_skoda_felicia, R.drawable.car_skoda_old)
        )
    }

    /**
     * Generates a random status, but each car uses its own realistic range so the
     * randomness stays "in character": the newer Skoda tends to read Excellent,
     * the Audi Good, and the old Felicia Poor - while every launch still varies.
     */
    fun getRandomStatus(carId: Int): CarStatus {
        return when (carId) {
            1 -> randomStatus( // Skoda Superb 2018 - newer car -> tends to Excellent
                oilRange = 75..100,
                engineOkChance = 0.98,
                tirePressureRange = 2.2..2.4,
                fuelRangeKm = 450..700,
                mileageRange = 20_000..60_000,
                nextServiceRange = 6_000..15_000
            )
            2 -> randomStatus( // Audi A6 2018 -> tends to Good
                oilRange = 50..80,
                engineOkChance = 0.90,
                tirePressureRange = 2.0..2.5,
                fuelRangeKm = 250..500,
                mileageRange = 60_000..120_000,
                nextServiceRange = 2_000..8_000
            )
            else -> randomStatus( // Skoda Felicia - old car -> tends to Poor
                oilRange = 10..45,
                engineOkChance = 0.65,
                tirePressureRange = 1.7..2.6,
                fuelRangeKm = 20..200,
                mileageRange = 150_000..260_000,
                nextServiceRange = 0..1_500
            )
        }
    }

    private fun randomStatus(
        oilRange: IntRange,
        engineOkChance: Double,
        tirePressureRange: ClosedFloatingPointRange<Double>,
        fuelRangeKm: IntRange,
        mileageRange: IntRange,
        nextServiceRange: IntRange
    ): CarStatus {
        return CarStatus(
            isLocked = Random.nextBoolean(),
            engineOk = Random.nextDouble() < engineOkChance,
            oilLevel = Random.nextInt(oilRange.first, oilRange.last + 1),
            tirePressure = List(4) {
                Random.nextDouble(tirePressureRange.start, tirePressureRange.endInclusive)
            },
            fuelRange = Random.nextInt(fuelRangeKm.first, fuelRangeKm.last + 1),
            mileage = Random.nextInt(mileageRange.first, mileageRange.last + 1),
            nextServiceKm = Random.nextInt(nextServiceRange.first, nextServiceRange.last + 1)
        )
    }
}
