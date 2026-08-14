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
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.olhadem.app42.data.Car
import com.olhadem.app42.data.CarRepository
import com.olhadem.app42.ui.CarCard
import com.olhadem.app42.ui.CarDetailScreen
import com.olhadem.app42.ui.MoreOptionsButton
import com.olhadem.app42.ui.theme.LocalDarkTheme
import com.olhadem.app42.ui.theme.ThemeMode
import com.olhadem.app42.ui.theme.ThemePreferences
import com.olhadem.app42.ui.theme.appColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
           // Тема тепер НЕ залежить від системних налаштувань телефону -
           // тільки від явного вибору користувача (⋮ → Тема), який ми
           // зберігаємо через ThemePreferences (SharedPreferences), щоб він
           // не скидався після повного закриття застосунку.
           var themeMode by remember { mutableStateOf(ThemePreferences.load(this@MainActivity)) }
           val isDark = themeMode == ThemeMode.DARK
           val colorScheme = if (isDark) darkColorScheme() else lightColorScheme()

           // CompositionLocalProvider "розсилає" isDark усьому дереву екранів
           // одразу - тому appColors() у CarCard/CarDetailScreen/MainScreen
           // підхоплює нову тему миттєво, без перезапуску екрана.
           CompositionLocalProvider(LocalDarkTheme provides isDark) {
               MaterialTheme(colorScheme = colorScheme) {
                   Surface(
                       modifier = Modifier.fillMaxSize(),
                       color = MaterialTheme.colorScheme.background
                   ) {
                       MainScreen(
                           themeMode = themeMode,
                           onThemeModeChange = { mode ->
                               themeMode = mode
                               ThemePreferences.save(this@MainActivity, mode)
                           }
                       )
                   }
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
fun MainScreen(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
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

    // Стан пошуку: чи активне поле пошуку в шапці, і що в нього набрано.
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Коли відкриваємо деталі машини - ховаємо пошук, щоб він не лишався
    // "непомітно активним" на екрані, де його все одно не видно.
    LaunchedEffect(selectedCar) {
        if (selectedCar != null) {
            isSearchActive = false
            searchQuery = ""
        }
    }

    // Системна кнопка/жест "назад" (апаратна чи на екрані) - коли відкриті
    // деталі машини, вона має повертати на список, а не одразу закривати
    // застосунок. enabled = false на списку - там "назад" працює як завжди.
    BackHandler(enabled = selectedCar != null) {
        selectedCar = null
    }

    // Список машин, відфільтрований за текстом пошуку (за назвою машини,
    // без урахування регістру). Якщо рядок пошуку порожній - показуємо всіх.
    val filteredCars = if (searchQuery.isBlank()) {
        cars
    } else {
        cars.filter { car -> stringResource(car.titleRes).contains(searchQuery, ignoreCase = true) }
    }

    Column(
       modifier = Modifier
           .fillMaxSize()
           .background(appColors().screenBackground)
    ) {
       GreenTopBar(
           showBack = selectedCar != null,
           onBackClick = { selectedCar = null },
           isSearchActive = isSearchActive,
           searchQuery = searchQuery,
           onSearchQueryChange = { searchQuery = it },
           onSearchToggle = { active -> isSearchActive = active },
           themeMode = themeMode,
           onThemeModeChange = onThemeModeChange
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
                   if (filteredCars.isEmpty()) {
                       Box(
                           modifier = Modifier.fillMaxSize(),
                           contentAlignment = Alignment.Center
                       ) {
                           Text(
                               text = stringResource(R.string.no_search_results),
                               color = appColors().textTertiary,
                               fontSize = 16.sp
                           )
                       }
                   } else {
                       LazyColumn(
                           modifier = Modifier.fillMaxSize(),
                           contentPadding = PaddingValues(16.dp),
                           verticalArrangement = Arrangement.spacedBy(12.dp)
                       ) {
                           items(filteredCars, key = { it.id }) { car ->
                               CarCard(
                                   car = car,
                                   status = carStatuses.getValue(car.id),
                                   onClick = { selectedCar = car },
                                   sharedTransitionScope = this@SharedTransitionLayout,
                                   animatedVisibilityScope = this@AnimatedContent
                               )
                           }
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
    onBackClick: () -> Unit,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAuthorDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Пошук доступний лише на головному списку - на екрані деталей однієї
    // машини (showBack == true) шукати нема серед чого.
    val showSearchUi = isSearchActive && !showBack

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
           if (showSearchUi) {
               // Режим пошуку: замість заголовка - текстове поле, яке
               // одразу отримує фокус (і відкриває клавіатуру).
               Row(
                   modifier = Modifier.fillMaxSize(),
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   Box(
                       modifier = Modifier.size(48.dp),
                       contentAlignment = Alignment.Center
                   ) {
                       Text(
                           text = "✕",
                           color = Color.White,
                           fontSize = 26.sp,
                           fontWeight = FontWeight.Bold,
                           modifier = Modifier.clickable {
                               onSearchQueryChange("")
                               onSearchToggle(false)
                           }
                       )
                   }

                   Box(
                       modifier = Modifier
                           .weight(1f)
                           .padding(horizontal = 4.dp)
                   ) {
                       if (searchQuery.isEmpty()) {
                           Text(
                               text = stringResource(R.string.search_hint),
                               color = Color.White.copy(alpha = 0.75f),
                               fontSize = 18.sp
                           )
                       }
                       BasicTextField(
                           value = searchQuery,
                           onValueChange = onSearchQueryChange,
                           singleLine = true,
                           textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                           cursorBrush = SolidColor(Color.White),
                           modifier = Modifier
                               .fillMaxWidth()
                               .focusRequester(focusRequester)
                       )
                   }

                   if (searchQuery.isNotEmpty()) {
                       Box(
                           modifier = Modifier
                               .size(48.dp)
                               .clickable { onSearchQueryChange("") },
                           contentAlignment = Alignment.Center
                       ) {
                           Text(text = "×", color = Color.White, fontSize = 28.sp)
                       }
                   }
               }

               // LaunchedEffect(Unit) тут виконується щоразу, коли ця гілка
               // Box'а заново потрапляє в композицію (тобто щоразу, коли
               // showSearchUi стає true) - це і потрібно, щоб фокус
               // (і клавіатура) з'являлись одразу після натискання лупи.
               LaunchedEffect(Unit) {
                   focusRequester.requestFocus()
               }
           } else {
               if (showBack) {
                   Text(
                       text = "↩",
                       color = Color.White,
                       fontSize = 34.sp,
                       fontWeight = FontWeight.Bold,
                       modifier = Modifier
                           .align(Alignment.CenterStart)
                           .offset(y = 10.dp)
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

               // spacedBy(0.dp) - лупа і кнопка меню стоять впритул, без
               // проміжку між ними (обидві - однакові 48dp-квадрати).
               Row(
                   modifier = Modifier
                       .align(Alignment.CenterEnd)
                       .offset(y = 10.dp),
                   verticalAlignment = Alignment.CenterVertically,
                   horizontalArrangement = Arrangement.spacedBy(0.dp)
               ) {
                   if (!showBack) {
                       Box(
                           modifier = Modifier
                               .size(48.dp)
                               .clickable { onSearchToggle(true) },
                           contentAlignment = Alignment.Center
                       ) {
                           Text(
                               text = "⌕",
                               color = Color.White,
                               fontSize = 34.sp,
                               fontWeight = FontWeight.Normal
                           )
                       }
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
                               text = { Text(stringResource(R.string.menu_theme)) },
                               onClick = {
                                   menuExpanded = false
                                   showThemeDialog = true
                               }
                           )
                           DropdownMenuItem(
                               text = { Text(stringResource(R.string.menu_about)) },
                               onClick = {
                                   menuExpanded = false
                                   showAboutDialog = true
                               }
                           )
                           DropdownMenuItem(
                               text = { Text(stringResource(R.string.menu_author)) },
                               onClick = {
                                   menuExpanded = false
                                   showAuthorDialog = true
                               }
                           )
                       }
                   }
               }
           }
       }
    }

    if (showLanguageDialog) {
       LanguagePickerDialog(onDismiss = { showLanguageDialog = false })
    }

    if (showThemeDialog) {
       ThemePickerDialog(
           currentMode = themeMode,
           onModeSelected = { mode ->
               onThemeModeChange(mode)
               showThemeDialog = false
           },
           onDismiss = { showThemeDialog = false }
       )
    }

    if (showAboutDialog) {
       AboutAppDialog(onDismiss = { showAboutDialog = false })
    }

    if (showAuthorDialog) {
       AboutAuthorDialog(onDismiss = { showAuthorDialog = false })
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

/**
 * Вибір теми: Світла / Темна. На відміну від мови, тут НЕ треба
 * перезапускати активність (recreate()) - зміна кольорів проходить одразу
 * через LocalDarkTheme і звичайну recomposition.
 */
@Composable
private fun ThemePickerDialog(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        ThemeMode.LIGHT to stringResource(R.string.theme_light),
        ThemeMode.DARK to stringResource(R.string.theme_dark)
    )

    AlertDialog(
       onDismissRequest = onDismiss,
       title = { Text(stringResource(R.string.menu_theme)) },
       text = {
           Column {
               options.forEach { (mode, label) ->
                   Row(
                       modifier = Modifier
                           .fillMaxWidth()
                           .clickable { onModeSelected(mode) }
                           .padding(vertical = 12.dp),
                       verticalAlignment = Alignment.CenterVertically
                   ) {
                       Text(
                           text = label,
                           fontSize = 17.sp,
                           modifier = Modifier.weight(1f)
                       )
                       if (mode == currentMode) {
                           Text(
                               text = "✓",
                               fontSize = 17.sp,
                               color = Color(0xFF0E8F5A),
                               fontWeight = FontWeight.Bold
                           )
                       }
                   }
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

/**
 * "Про автора" - текст про Олю, а під ним фото. Фото - drawable-nodpi
 * (фон уже прибраний, обрізаний впритул), висота обмежена 130dp, щоб увесь
 * діалог (заголовок + текст + фото + кнопка) поміщався на екрані одразу,
 * без прокрутки.
 */
@Composable
private fun AboutAuthorDialog(onDismiss: () -> Unit) {
    AlertDialog(
       onDismissRequest = onDismiss,
       title = { Text(stringResource(R.string.menu_author)) },
       text = {
           Column {
               // textAlign = Justify - текст рівний з обох боків, як у
               // книжці (Compose сам розтягує проміжки між словами, крім
               // останнього рядка кожного речення - його воно не чіпає).
               // Перше речення в самому рядку (strings.xml) починається з
               // 4 нерозривних пробілів "\u00A0" - це і є "абзацний відступ",
               // решта речень просто йдуть з нового рядка (\n) без відступу.
               Text(
                   text = stringResource(R.string.about_author_bio),
                   fontSize = 14.sp,
                   textAlign = TextAlign.Justify
               )

               Spacer(modifier = Modifier.height(10.dp))

               Image(
                   painter = painterResource(id = R.drawable.author_photo),
                   contentDescription = stringResource(R.string.menu_author),
                   contentScale = ContentScale.Fit,
                   modifier = Modifier
                       .fillMaxWidth()
                       .height(100.dp)
                       .clip(RoundedCornerShape(12.dp))
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
