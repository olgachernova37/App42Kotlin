package com.olhadem.app42

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.olhadem.app42.data.Car
import com.olhadem.app42.data.CarRepository
import com.olhadem.app42.ui.CarCard
import com.olhadem.app42.ui.CarDetailScreen
import com.olhadem.app42.ui.MoreOptionsButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       enableEdgeToEdge()
       setContent {
           MaterialTheme {
               Surface(
                   modifier = Modifier.fillMaxSize(),
                   color = MaterialTheme.colorScheme.background
               ) {
                   MainScreen()
               }
           }
       }
    }

    companion object {
       init {
           System.loadLibrary("app42")
       }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen() {
    // remember{} тримає ці значення, поки composable живий, і не перестворює їх
    // при кожному перемальовуванні (recomposition).
    val repository = remember { CarRepository() }
    val cars = remember { repository.getCars() }

    // Статус кожної машини генерується один раз при відкритті екрана,
    // а не при кожному кліку - інакше цифри "стрибали б" щоразу.
    val carStatuses = remember {
        cars.associate { car -> car.id to repository.getRandomStatus(car.id) }
    }

    // Яка машина зараз обрана. null = показуємо список усіх машин.
    var selectedCar by remember { mutableStateOf<Car?>(null) }

    // Системна кнопка/жест "назад" (апаратна чи на екрані) - коли відкриті
    // деталі машини, вона має повертати на список, а не одразу закривати
    // застосунок. enabled = false на списку - там "назад" працює як завжди.
    BackHandler(enabled = selectedCar != null) {
        selectedCar = null
    }

    Column(
       modifier = Modifier
           .fillMaxSize()
           .background(Color(0xFFE8E8E8))
    ) {
       GreenTopBar(
           showBack = selectedCar != null,
           onBackClick = { selectedCar = null }
       )

       // SharedTransitionLayout дає можливість "прив'язати" одне і те саме фото
       // машини в списку і на екрані деталей (той самий sharedElement key в
       // CarCard і CarDetailScreen) - тоді Compose плавно анімує САМЕ ЦЕ фото
       // між двома місцями (переїзд + збільшення), а не просто показує новий
       // екран поверх старого.
       SharedTransitionLayout {
           AnimatedContent(
               targetState = selectedCar,
               label = "carListToDetail",
               transitionSpec = {
                   fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
               },
               modifier = Modifier.fillMaxSize()
           ) { selected ->
               if (selected == null) {
                   LazyColumn(
                       modifier = Modifier.fillMaxSize(),
                       contentPadding = PaddingValues(16.dp),
                       verticalArrangement = Arrangement.spacedBy(12.dp)
                   ) {
                       items(cars, key = { it.id }) { car ->
                           CarCard(
                               car = car,
                               status = carStatuses.getValue(car.id),
                               onClick = { selectedCar = car },
                               sharedTransitionScope = this@SharedTransitionLayout,
                               animatedVisibilityScope = this@AnimatedContent
                           )
                       }
                   }
               } else {
                   CarDetailScreen(
                       car = selected,
                       status = carStatuses.getValue(selected.id),
                       sharedTransitionScope = this@SharedTransitionLayout,
                       animatedVisibilityScope = this@AnimatedContent
                   )
               }
           }
       }
    }
}

@Composable
private fun GreenTopBar(
    showBack: Boolean,
    onBackClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
       modifier = Modifier
           .fillMaxWidth()
           .background(Color(0xFF0E8F5A))
    ) {
       // Box замість Row зі SpaceBetween - так назва застосунку завжди рівно
       // по центру шапки, незалежно від того, скільки місця займають іконки
       // по краях (стрілка "назад" зліва, пошук/меню справа).
       Box(
           modifier = Modifier
               .fillMaxWidth()
               .height(88.dp)
               .padding(horizontal = 16.dp)
       ) {
           if (showBack) {
               Text(
                   text = "↩",
                   color = Color.White,
                   fontSize = 34.sp,
                   fontWeight = FontWeight.Bold,
                   modifier = Modifier
                       .align(Alignment.CenterStart)
                       .clickable(
                           onClickLabel = stringResource(R.string.back),
                           onClick = onBackClick
                       )
               )
           }

           Text(
               text = "MyŠkoda",
               color = Color.White,
               fontSize = 34.sp,
               fontWeight = FontWeight.Medium,
               modifier = Modifier
                   .align(Alignment.Center)
                   .offset(y = 10.dp)
           )

           Row(
               modifier = Modifier.align(Alignment.CenterEnd),
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.spacedBy(14.dp)
           ) {
               Box(
                   modifier = Modifier.size(48.dp),
                   contentAlignment = Alignment.Center
               ) {
                   Text(
                       text = "⌕",
                       color = Color.White,
                       fontSize = 34.sp,
                       fontWeight = FontWeight.Normal
                   )
               }

               Box {
                   MoreOptionsButton(onClick = { menuExpanded = true })

                   DropdownMenu(
                       expanded = menuExpanded,
                       onDismissRequest = { menuExpanded = false }
                   ) {
                       DropdownMenuItem(
                           text = { Text(stringResource(R.string.menu_language)) },
                           onClick = {
                               menuExpanded = false
                               showLanguageDialog = true
                           }
                       )
                       DropdownMenuItem(
                           text = { Text(stringResource(R.string.menu_about)) },
                           onClick = {
                               menuExpanded = false
                               showAboutDialog = true
                           }
                       )
                   }
               }
           }
       }
    }

    if (showLanguageDialog) {
       LanguagePickerDialog(onDismiss = { showLanguageDialog = false })
    }

    if (showAboutDialog) {
       AboutAppDialog(onDismiss = { showAboutDialog = false })
    }
}

/**
 * Список з 3 мов застосунку (en/cs/de). Назви мов навмисно НЕ перекладені -
 * "English", "Čeština", "Deutsch" завжди пишуться своєю рідною мовою,
 * так прийнято в усіх застосунках, щоб людина впізнала свою мову.
 */
@Composable
private fun LanguagePickerDialog(onDismiss: () -> Unit) {
    val languages = listOf(
       "English" to "en",
       "Čeština" to "cs",
       "Deutsch" to "de"
    )
    val context = LocalContext.current

    AlertDialog(
       onDismissRequest = onDismiss,
       title = { Text(stringResource(R.string.menu_language)) },
       text = {
           Column {
               languages.forEach { (label, tag) ->
                   Text(
                       text = label,
                       fontSize = 17.sp,
                       modifier = Modifier
                           .fillMaxWidth()
                           .clickable {
                               // Ставимо мову застосунку і перезапускаємо екран,
                               // щоб усі stringResource() підхопили нову мову.
                               AppCompatDelegate.setApplicationLocales(
                                   LocaleListCompat.forLanguageTags(tag)
                               )
                               onDismiss()
                               (context as? Activity)?.recreate()
                           }
                           .padding(vertical = 12.dp)
                   )
               }
           }
       },
       confirmButton = {
           TextButton(onClick = onDismiss) {
               Text(stringResource(R.string.action_close))
           }
       }
    )
}

@Composable
private fun AboutAppDialog(onDismiss: () -> Unit) {
    AlertDialog(
       onDismissRequest = onDismiss,
       title = { Text(stringResource(R.string.app_name)) },
       text = {
           Column {
               Text(
                   text = stringResource(R.string.about_app_description),
                   fontSize = 15.sp
               )
               Spacer(modifier = Modifier.height(10.dp))
               Text(
                   text = stringResource(R.string.about_app_version),
                   fontSize = 13.sp,
                   color = Color.Gray
               )
           }
       },
       confirmButton = {
           TextButton(onClick = onDismiss) {
               Text(stringResource(R.string.action_close))
           }
       }
    )
}

@Composable
private fun BatteryIndicator() {
    Row(
       verticalAlignment = Alignment.CenterVertically,
       horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
       Box(
           modifier = Modifier
               .width(28.dp)
               .height(14.dp)
               .border(2.dp, Color.White, RoundedCornerShape(2.dp))
       )

       Box(
           modifier = Modifier
               .width(3.dp)
               .height(8.dp)
               .background(Color.White, RoundedCornerShape(2.dp))
       )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MaterialTheme {
       MainScreen()
    }
}
