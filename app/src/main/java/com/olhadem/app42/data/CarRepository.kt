package com.olhadem.app42.data

import com.olhadem.app42.R
import kotlin.random.Random

class CarRepository {

    fun getCars(): List<Car> {
        return listOf(
            Car(1, "Skoda Superb 2018", R.drawable.car_skoda_new),
            Car(2, "Audi A6 2018 Front", R.drawable.car_audi),
            Car(3, "Škoda Felicia", R.drawable.car_skoda_old)
        )
    }

    fun getRandomStatus(carId: Int): CarStatus {
        return CarStatus(
            isLocked = Random.nextBoolean(),
            engineOk = Random.nextDouble() < 0.9, // 90% chance OK
            oilLevel = Random.nextInt(10, 101), // 10% to 100%
            tirePressure = List(4) { Random.nextDouble(2.0, 2.6) }, // Typical pressure
            fuelRange = Random.nextInt(20, 701) // 20km to 700km
        )
    }
}
