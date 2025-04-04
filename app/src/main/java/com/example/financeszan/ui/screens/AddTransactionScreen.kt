package com.example.financeszan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeszan.data.entities.Transaction
import com.example.financeszan.data.entities.TransactionCategory
import com.example.financeszan.data.entities.TransactionType
import com.example.financeszan.ui.theme.ExpenseColor
import com.example.financeszan.ui.theme.ExpenseColorDark
import com.example.financeszan.ui.theme.IncomeColor
import com.example.financeszan.ui.theme.IncomeColorDark
import com.example.financeszan.ui.viewmodel.FinancesViewModel
import com.example.financeszan.ui.viewmodel.SettingsViewModel
import java.util.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancesViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = SettingsViewModel.getInstance(LocalContext.current.applicationContext as android.app.Application)
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.DAILY_EXPENSE) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

    // Colores para los tipos de transacción
    val incomeColor = if (isDarkMode) IncomeColorDark else IncomeColor
    val expenseColor = if (isDarkMode) ExpenseColorDark else ExpenseColor

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Transacción") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de tipo con botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedType = TransactionType.INCOME },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.INCOME) incomeColor else Color.Gray.copy(alpha = 0.3f),
                        contentColor = if (selectedType == TransactionType.INCOME) Color.White else Color.DarkGray
                    )
                ) {
                    Text("Ingreso")
                }
                
                Button(
                    onClick = { selectedType = TransactionType.EXPENSE },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.EXPENSE) expenseColor else Color.Gray.copy(alpha = 0.3f),
                        contentColor = if (selectedType == TransactionType.EXPENSE) Color.White else Color.DarkGray
                    )
                ) {
                    Text("Gasto")
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    // Filtrar solo números y un punto decimal
                    val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                    amount = filteredValue
                },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (selectedType == TransactionType.INCOME) incomeColor else expenseColor,
                    focusedLabelColor = if (selectedType == TransactionType.INCOME) incomeColor else expenseColor
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (selectedType == TransactionType.INCOME) incomeColor else expenseColor,
                    focusedLabelColor = if (selectedType == TransactionType.INCOME) incomeColor else expenseColor
                )
            )

            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = !categoryMenuExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = if (selectedType == TransactionType.INCOME) incomeColor else expenseColor,
                        focusedLabelColor = if (selectedType == TransactionType.INCOME) incomeColor else expenseColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    TransactionCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = { 
                                selectedCategory = category
                                categoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val transaction = Transaction(
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        description = description,
                        date = Date(),
                        type = selectedType,
                        category = selectedCategory
                    )
                    viewModel.addTransaction(transaction)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) incomeColor else expenseColor
                )
            ) {
                Text("Guardar")
            }
        }
    }
} 