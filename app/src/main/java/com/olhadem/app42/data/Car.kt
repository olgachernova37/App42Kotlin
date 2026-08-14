package com.olhadem.app42.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Car(
    val id: Int,
    @StringRes val titleRes: Int,
    @DrawableRes val imageRes: Int
)
