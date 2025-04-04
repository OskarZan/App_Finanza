package com.example.financeszan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeszan.data.entities.Transaction
import com.example.financeszan.data.entities.TransactionType
import com.example.financeszan.data.entities.TransactionCategory
import com.example.financeszan.ui.theme.ExpenseColor
import com.example.financeszan.ui.theme.ExpenseColorDark
import com.example.financeszan.ui.theme.IncomeColor
import com.example.financeszan.ui.theme.IncomeColorDark
import com.example.financeszan.ui.viewmodel.CalendarViewModel
import com.example.financeszan.ui.viewmodel.SettingsViewModel
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = CalendarViewModel.getInstance(LocalContext.current.applicationContext as android.app.Application),
    settingsViewModel: SettingsViewModel = SettingsViewModel.getInstance(LocalContext.current.applicationContext as android.app.Application)
) {
    val scope = rememberCoroutineScope()
    var errorState by remember { mutableStateOf<String?>(null) }
    
    // Manejar posibles errores de inicialización
    DisposableEffect(Unit) {
        try {
            // Intenta acceder a las propiedades del viewModel para detectar errores temprano
            viewModel.selectedDate
        } catch (e: Exception) {
            e.printStackTrace()
            errorState = "Error al inicializar el calendario: ${e.message}"
        }
        onDispose { }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (errorState != null) {
                // Mostrar error y botón para volver
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Error al cargar el calendario",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorState ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Volver")
                        }
                    }
                }
            } else {
                SafeCalendarContent(viewModel, settingsViewModel)
            }
        }
    }
}

@Composable
private fun SafeCalendarContent(
    viewModel: CalendarViewModel,
    settingsViewModel: SettingsViewModel
) {
    val selectedDate = remember { mutableStateOf(LocalDate.now()) }
    val transactionsForSelectedDate = remember { mutableStateOf<List<Transaction>>(emptyList()) }
    val dailyTransactionTotals = remember { mutableStateOf<Map<LocalDate, Double>>(emptyMap()) }
    
    // Observar datos de forma segura
    LaunchedEffect(viewModel) {
        try {
            viewModel.selectedDate.collect { selectedDate.value = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    LaunchedEffect(viewModel) {
        try {
            viewModel.transactionsForSelectedDate.collect { transactionsForSelectedDate.value = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    LaunchedEffect(viewModel) {
        try {
            viewModel.dailyTransactionTotals.collect { dailyTransactionTotals.value = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    
    // Configurar el formato de moneda según la configuración
    val currencyFormat = when (settingsViewModel.currencyType) {
        com.example.financeszan.ui.screens.CurrencyType.EURO -> NumberFormat.getCurrencyInstance(Locale("es", "ES"))
        com.example.financeszan.ui.screens.CurrencyType.DOLLAR -> NumberFormat.getCurrencyInstance(Locale.US)
        com.example.financeszan.ui.screens.CurrencyType.YEN -> NumberFormat.getCurrencyInstance(Locale.JAPAN)
        else -> NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    }
    
    // Encabezado del calendario con mes y año y flechas para navegar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            currentMonth = currentMonth.minusMonths(1)
        }) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior")
        }
        
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))),
            style = MaterialTheme.typography.titleLarge
        )
        
        IconButton(onClick = {
            currentMonth = currentMonth.plusMonths(1)
        }) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mes siguiente")
        }
    }
    
    // Días de la semana
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val daysOfWeek = listOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        )
        
        daysOfWeek.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale("es", "ES")),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Rejilla del calendario
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        // Días previos del mes para alinear
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstWeekDayOfMonth = firstDayOfMonth.dayOfWeek.value
        val offset = if (firstWeekDayOfMonth == 7) 0 else firstWeekDayOfMonth
        
        items(offset) {
            Box(modifier = Modifier.aspectRatio(1f))
        }
        
        // Días del mes actual
        val daysInMonth = currentMonth.lengthOfMonth()
        items(daysInMonth) { day ->
            val date = currentMonth.atDay(day + 1)
            val isSelected = date == selectedDate.value
            val hasTransactions = dailyTransactionTotals.value.containsKey(date)
            val amountForDay = dailyTransactionTotals.value[date] ?: 0.0
            
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            hasTransactions -> {
                                if (amountForDay >= 0) {
                                    if (isDarkMode) IncomeColorDark.copy(alpha = 0.3f) else IncomeColor.copy(alpha = 0.3f)
                                } else {
                                    if (isDarkMode) ExpenseColorDark.copy(alpha = 0.3f) else ExpenseColor.copy(alpha = 0.3f)
                                }
                            }
                            else -> Color.Transparent
                        }
                    )
                    .clickable {
                        try {
                            viewModel.selectDate(date)
                        } catch (e: Exception) {
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = (day + 1).toString(),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> LocalContentColor.current
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (hasTransactions) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (amountForDay >= 0) {
                                        if (isDarkMode) IncomeColorDark else IncomeColor
                                    } else {
                                        if (isDarkMode) ExpenseColorDark else ExpenseColor
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // Detalles del día seleccionado
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = selectedDate.value.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val totalForDay = dailyTransactionTotals.value[selectedDate.value] ?: 0.0
            Text(
                text = "Balance del día: ${currencyFormat.format(totalForDay)}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (totalForDay >= 0) {
                    if (isDarkMode) IncomeColorDark else IncomeColor
                } else {
                    if (isDarkMode) ExpenseColorDark else ExpenseColor
                }
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Lista de transacciones del día
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Transacciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (transactionsForSelectedDate.value.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay transacciones para este día",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(transactionsForSelectedDate.value) { transaction ->
                        TransactionItemCalendar(
                            transaction = transaction,
                            currencyFormat = currencyFormat,
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCalendar(
    transaction: Transaction,
    currencyFormat: NumberFormat,
    isDarkMode: Boolean
) {
    val emoji = when (transaction.category) {
        TransactionCategory.SUBSCRIPTION -> "🎬"
        TransactionCategory.DAILY_EXPENSE -> "💲"
        TransactionCategory.LEISURE -> "🪇"
        TransactionCategory.MONTHLY_EXPENSE -> "🏡"
        else -> ""
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (transaction.type == TransactionType.INCOME) {
                if (isDarkMode) IncomeColorDark else IncomeColor
            } else {
                if (isDarkMode) ExpenseColorDark else ExpenseColor
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$emoji ${transaction.description}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = transaction.category.displayName,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = currencyFormat.format(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = if (transaction.type == TransactionType.INCOME) {
                    if (isDarkMode) IncomeColorDark else IncomeColor
                } else {
                    if (isDarkMode) ExpenseColorDark else ExpenseColor
                }
            )
        }
    }
} 