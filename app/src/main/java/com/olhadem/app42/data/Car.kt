package com.olhadem.app42.data

import androidx.annotation.DrawableRes

data class Car(
    val id: Int,
    val title: String,
    @DrawableRes val imageRes: Int
)
