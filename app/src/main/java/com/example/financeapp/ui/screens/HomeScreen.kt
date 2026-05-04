package com.example.financeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.ui.components.PieSlice
import com.example.financeapp.ui.components.SimpleBarChart
import com.example.financeapp.ui.components.SimpleLineChart
import com.example.financeapp.ui.components.SimplePieChart
import com.example.financeapp.ui.viewmodels.FinanceViewModel

@Composable
fun HomeScreen(vm: FinanceViewModel = hiltViewModel()) {

    val state by vm.uiState.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember {
        mutableLongStateOf(state.categories.firstOrNull()?.id ?: 1L)
    }

    val monthlyTotal = state.transactions.sumOf { it.amount }

    val categoriesById = state.categories.associateBy { it.id }

    val categoryTotals = state.transactions
        .groupBy { it.categoryId }
        .map { (categoryId, txns) ->
            val category = categoriesById[categoryId]
            PieSlice(
                label = category?.name ?: "Unknown",
                amount = txns.sumOf { it.amount },
                color = colorFromHex(category?.colorHex)
            )
        }
        .sortedByDescending { it.amount }

    val recent = state.transactions.sortedBy { it.date }.takeLast(10)

    val monthGroups = state.transactions
        .groupBy { "${it.date.year}-${it.date.monthValue.toString().padStart(2, '0')}" }
        .toSortedMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            "This Month: ₹${"%.2f".format(monthlyTotal)}",
            style = MaterialTheme.typography.titleLarge
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.weight(1f)
            )
        }

        // Category selection
        Text("Category", style = MaterialTheme.typography.labelLarge)

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            state.categories.take(5).forEach { category ->
                AssistChip(
                    onClick = { selectedCategoryId = category.id },
                    label = { Text(category.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor =
                            if (selectedCategoryId == category.id)
                                colorFromHex(category.colorHex).copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Button(
            onClick = {
                val parsedAmount = parseAmount(amount)
                if (parsedAmount > 0) {
                    vm.addTransaction(parsedAmount, selectedCategoryId, note.ifBlank { null })
                    amount = ""
                    note = ""
                }
            }
        ) {
            Text("Add Expense")
        }

        Button(onClick = vm::fetchSmartSuggestion) {
            Text("Get Smart Suggestion")
        }

        // Charts
        SimplePieChart(
            entries = categoryTotals,
            title = "Category Distribution"
        )

        SimpleLineChart(
            values = recent.map { it.amount },
            xLabels = recent.map { "${it.date.monthValue}/${it.date.dayOfMonth}" },
            title = "Daily Spending Trend"
        )

        SimpleBarChart(
            values = monthGroups.values.map { it.sumOf { txn -> txn.amount } },
            xLabels = monthGroups.keys.map { it.takeLast(2) },
            title = "Monthly Comparison"
        )

        // Insights
        Text("Insights", style = MaterialTheme.typography.titleMedium)

        state.insights.forEach {
            Text("• $it")
        }

        state.suggestion?.let { s ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp)) {
                    Text("Suggested Budget: ₹${"%.2f".format(s.suggestedTotal)}")
                    s.tips.forEach { Text("- $it") }
                }
            }
        }

        // Transactions list
        LazyColumn {
            items(state.transactions) { txn ->
                Text("₹${"%.2f".format(txn.amount)} • ${txn.date} • ${txn.note ?: "No note"}")
            }
        }
    }
}

// Helpers
private fun parseAmount(raw: String): Double =
    raw.replace(",", "").trim().toDoubleOrNull() ?: 0.0

private fun colorFromHex(hex: String?): Color = try {
    if (hex.isNullOrBlank()) Color(0xFF9E9E9E)
    else Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    Color(0xFF9E9E9E)
}