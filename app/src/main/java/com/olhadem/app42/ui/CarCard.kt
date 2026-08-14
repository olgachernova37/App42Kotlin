package com.olhadem.app42.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olhadem.app42.R
import com.olhadem.app42.data.Car
import com.olhadem.app42.data.CarStatus
import com.olhadem.app42.data.ServiceCondition
import com.olhadem.app42.data.calculateServiceCondition
import com.olhadem.app42.ui.theme.appColors

/**
 * One row in the main car list, split into two equal halves:
 * the left half is the car photo (full photo, no crop, no background),
 * the right half is a card with the name, mileage/lock state and
 * condition badge.
 *
 * Everything text-based (name, lock state, condition) comes from
 * strings.xml, so switching the app language from the top-bar menu
 * updates this card too - nothing here is hardcoded in one language.
 * Colors come from appColors(), so the card also follows light/dark theme.
 *
 * The photo uses [SharedTransitionScope.sharedElement] with the SAME key as the
 * photo in [CarDetailScreen], so when you tap a card, Compose animates that exact
 * photo growing and moving into place on the detail screen, instead of just
 * cross-fading two unrelated screens.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CarCard(
    car: Car,
    status: CarStatus,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val condition = calculateServiceCondition(status)
    val title = stringResource(car.titleRes)
    val colors = appColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ліва половина - фото машини цілком, без обрізання і без фону.
        // sharedElement "прив'язує" це фото до такого самого фото на екрані
        // деталей (той самий key), тому Compose плавно анімує його рух/розмір
        // між списком і деталями, замість простого проявлення нового екрана.
        with(sharedTransitionScope) {
            Image(
                painter = painterResource(id = car.imageRes),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .sharedElement(
                        rememberSharedContentState(key = "car-photo-${car.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )
        }

        // Права половина - картка з назвою, пробігом/замком і статусом.
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colors.cardBackground,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )

                    // Пробіг - підписаний словом (не смайликом), щоб було
                    // видно, що це саме за число, навіть без ознайомлення з
                    // рештою застосунку. Слово береться з strings.xml, тому
                    // воно теж змінюється разом з мовою застосунку.
                    Text(
                        text = "${stringResource(R.string.label_mileage)}: ${"%,d km".format(status.mileage)}",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )

                    ConditionBadge(condition = condition)
                }

                // Тонкий натяк, що картку можна натиснути - стрілочка праворуч.
                Text(
                    text = "›",
                    color = colors.textFaint,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        }
    }
}

@Composable
fun ConditionBadge(condition: ServiceCondition) {
    val (label, color) = when (condition) {
        ServiceCondition.EXCELLENT ->
            stringResource(R.string.service_condition_excellent) to Color(0xFF0E8F5A)
        ServiceCondition.GOOD ->
            stringResource(R.string.service_condition_good) to Color(0xFFE0A100)
        ServiceCondition.POOR ->
            stringResource(R.string.service_condition_poor) to Color(0xFFD32F2F)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color
        ) {}

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            // maxLines = 2 (не 1) - раніше довгі слова англійською/німецькою
            // ("Needs attention", "Ausgezeichneter Zustand") обрізались
            // трьома крапками. Тепер їм є куди перенестись на другий рядок,
            // а maxLines=148dp висота картки + більший вертикальний відступ
            // дають знизу достатньо місця, щоб літери з "хвостиком" (y, g)
            // не обрізались.
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun CarCardPreview() {
    MaterialTheme {
        SharedTransitionLayout {
            androidx.compose.animation.AnimatedContent(targetState = true, label = "preview") {
                CarCard(
                    car = Car(1, R.string.car_name_skoda_superb, R.drawable.car_skoda_new),
                    status = CarStatus(
                        isLocked = true,
                        engineOk = true,
                        oilLevel = 90,
                        tirePressure = listOf(2.3, 2.3, 2.3, 2.3),
                        fuelRange = 600,
                        mileage = 40_000,
                        nextServiceKm = 10_000
                    ),
                    onClick = {},
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            }
        }
    }
}
