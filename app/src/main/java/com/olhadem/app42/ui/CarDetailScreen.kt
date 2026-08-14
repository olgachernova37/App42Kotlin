package com.olhadem.app42.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olhadem.app42.R
import com.olhadem.app42.data.Car
import com.olhadem.app42.data.CarStatus
import com.olhadem.app42.data.calculateServiceCondition
import com.olhadem.app42.ui.theme.appColors

/**
 * Detail screen for one car. Layout, top to bottom:
 *  1. big photo (shared element with the matching CarCard photo)
 *  2. name + overall condition badge
 *  3. a 2x2 grid of "stat tiles" - the 4 numbers you'd glance at first
 *  4. a tire-pressure card with all 4 wheels
 *  5. a status card - lock state and engine state
 *
 * Everything text-based comes from strings.xml, so it follows the app's
 * language setting like the rest of the screens. Colors come from
 * appColors(), so it follows light/dark theme too.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CarDetailScreen(
    car: Car,
    status: CarStatus,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val title = stringResource(car.titleRes)
    val colors = appColors()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        with(sharedTransitionScope) {
            Image(
                painter = painterResource(id = car.imageRes),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "car-photo-${car.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        ConditionBadge(condition = calculateServiceCondition(status))

        Spacer(modifier = Modifier.height(22.dp))

        SectionLabel(text = stringResource(R.string.section_overview))
        Spacer(modifier = Modifier.height(8.dp))

        // 2x2 сітка з найважливішими цифрами - те, що зазвичай дивляться
        // "одним поглядом", без заглиблення в деталі.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                icon = "💧",
                value = "${status.oilLevel}%",
                label = stringResource(R.string.label_oil),
                valueColor = oilLevelColor(status.oilLevel),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = "⛽",
                value = "${status.fuelRange} km",
                label = stringResource(R.string.label_range),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                icon = "🧭",
                value = "%,d km".format(status.mileage),
                label = stringResource(R.string.label_mileage),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                icon = "🔧",
                value = "%,d km".format(status.nextServiceKm),
                label = stringResource(R.string.label_next_service),
                valueColor = if (status.nextServiceKm < 1_500) Color(0xFFD32F2F) else colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionLabel(text = stringResource(R.string.tire_pressure))
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val wheelLabels = listOf(
                    stringResource(R.string.wheel_fl),
                    stringResource(R.string.wheel_fr),
                    stringResource(R.string.wheel_rl),
                    stringResource(R.string.wheel_rr)
                )
                status.tirePressure.forEachIndexed { index, pressure ->
                    TireChip(label = wheelLabels[index], bar = pressure)
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionLabel(text = stringResource(R.string.section_status))
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                StatusRow(
                    icon = if (status.isLocked) "🔒" else "🔓",
                    text = stringResource(
                        if (status.isLocked) R.string.car_status_locked else R.string.car_status_unlocked
                    ),
                    color = if (status.isLocked) Color(0xFF0E8F5A) else Color(0xFFE0A100)
                )
                StatusRow(
                    icon = if (status.engineOk) "✅" else "⚠️",
                    text = stringResource(
                        if (status.engineOk) R.string.engine_status_ok else R.string.engine_status_check
                    ),
                    color = if (status.engineOk) Color(0xFF0E8F5A) else Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = appColors().textTertiary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun StatTile(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = appColors().textPrimary
) {
    val colors = appColors()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.cardBackground,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = colors.textTertiary
            )
        }
    }
}

@Composable
private fun TireChip(label: String, bar: Double) {
    val colors = appColors()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.textTertiary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "%.1f".format(bar),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Text(
            text = "bar",
            fontSize = 11.sp,
            color = colors.textFaint
        )
    }
}

@Composable
private fun StatusRow(icon: String, text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

private fun oilLevelColor(level: Int): Color = when {
    level >= 60 -> Color(0xFF0E8F5A)
    level >= 30 -> Color(0xFFE0A100)
    else -> Color(0xFFD32F2F)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun CarDetailScreenPreview() {
    MaterialTheme {
        SharedTransitionLayout {
            AnimatedContent(targetState = true, label = "preview") {
                CarDetailScreen(
                    car = Car(1, R.string.car_name_skoda_superb, R.drawable.car_skoda_new),
                    status = CarStatus(
                        isLocked = true,
                        engineOk = true,
                        oilLevel = 90,
                        tirePressure = listOf(2.3, 2.3, 2.2, 2.4),
                        fuelRange = 600,
                        mileage = 40_000,
                        nextServiceKm = 10_000
                    ),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            }
        }
    }
}
