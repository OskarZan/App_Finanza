package com.example.financeszan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeszan.data.entities.Transaction
import com.example.financeszan.data.entities.TransactionCategory
import com.example.financeszan.data.entities.TransactionType
import com.example.financeszan.ui.viewmodel.FinancesViewModel
import com.example.financeszan.ui.viewmodel.SettingsViewModel
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddTransactionClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    viewModel: FinancesViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = SettingsViewModel.getInstance(LocalContext.current.applicationContext as android.app.Application)
) {
    val transactions by viewModel.transactions.collectAsState()
    val monthlyBalance by viewModel.monthlyBalance.collectAsState()
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    
    // Configurar el formato de moneda según la configuración
    val currencyFormat = when (settingsViewModel.currencyType) {
        com.example.financeszan.ui.screens.CurrencyType.EURO -> NumberFormat.getCurrencyInstance(Locale("es", "ES"))
        com.example.financeszan.ui.screens.CurrencyType.DOLLAR -> NumberFormat.getCurrencyInstance(Locale.US)
        com.example.financeszan.ui.screens.CurrencyType.YEN -> NumberFormat.getCurrencyInstance(Locale.JAPAN)
        else -> NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Reiniciar Balance") },
            text = { Text("¿Estás seguro de que deseas eliminar todas las transacciones? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllTransactions()
                        showConfirmDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "FinanzasZan",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
                Divider()
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") }
                )
                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = {
                        scope.launch { 
                            drawerState.close() 
                            onNavigateToProfile()
                        }
                    },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Perfil") }
                )
                NavigationDrawerItem(
                    label = { Text("Calendario") },
                    selected = false,
                    onClick = {
                        scope.launch { 
                            drawerState.close() 
                            onNavigateToCalendar()
                        }
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendario") }
                )
                NavigationDrawerItem(
                    label = { Text("Configuración") },
                    selected = false,
                    onClick = {
                        scope.launch { 
                            drawerState.close()
                            onNavigateToSettings()
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") }
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Reiniciar Balance") },
                    selected = false,
                    onClick = {
                        scope.launch { 
                            drawerState.close()
                            showConfirmDialog = true
                        }
                    },
                    icon = { Icon(Icons.Default.Delete, contentDescription = "Reiniciar Balance") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FinanzasZan") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = onAddTransactionClick) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar transacción")
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
                // Balance mensual
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Balance Mensual",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = currencyFormat.format(monthlyBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (monthlyBalance >= 0) {
                                if (isDarkMode) {
                                    com.example.financeszan.ui.theme.IncomeColorDark
                                } else {
                                    com.example.financeszan.ui.theme.IncomeColor
                                }
                            } else {
                                if (isDarkMode) {
                                    com.example.financeszan.ui.theme.ExpenseColorDark
                                } else {
                                    com.example.financeszan.ui.theme.ExpenseColor
                                }
                            }
                        )
                    }
                }

                // Resumen por categorías
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Gastos por Categoría",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TransactionCategory.values().forEach { category ->
                            if (category != TransactionCategory.SALARY) {
                                val total by viewModel.getCategoryTotal(category).collectAsState()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(category.displayName)
                                    Text(
                                        text = currencyFormat.format(total),
                                        color = if (isDarkMode) {
                                            com.example.financeszan.ui.theme.ExpenseColorDark
                                        } else {
                                            com.example.financeszan.ui.theme.ExpenseColor
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Lista de transacciones
                Text(
                    text = "Últimas Transacciones",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(transactions) { transaction ->
                        TransactionItem(transaction = transaction, settingsViewModel = settingsViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    settingsViewModel: SettingsViewModel = SettingsViewModel.getInstance(LocalContext.current.applicationContext as android.app.Application)
) {
    val currencyFormat = when (settingsViewModel.currencyType) {
        com.example.financeszan.ui.screens.CurrencyType.EURO -> NumberFormat.getCurrencyInstance(Locale("es", "ES"))
        com.example.financeszan.ui.screens.CurrencyType.DOLLAR -> NumberFormat.getCurrencyInstance(Locale.US)
        com.example.financeszan.ui.screens.CurrencyType.YEN -> NumberFormat.getCurrencyInstance(Locale.JAPAN)
        else -> NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    }
    
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    
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
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    if (isDarkMode) {
                        com.example.financeszan.ui.theme.IncomeColorDark
                    } else {
                        com.example.financeszan.ui.theme.IncomeColor
                    }
                } else {
                    if (isDarkMode) {
                        com.example.financeszan.ui.theme.ExpenseColorDark
                    } else {
                        com.example.financeszan.ui.theme.ExpenseColor
                    }
                }
            )
        }
    }
} 