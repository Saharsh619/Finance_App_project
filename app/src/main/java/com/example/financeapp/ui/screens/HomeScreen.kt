package com.example.financeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.ui.components.SimpleBarChart
import com.example.financeapp.ui.components.SimpleLineChart
import com.example.financeapp.ui.components.SimplePieChart
import com.example.financeapp.ui.viewmodels.FinanceViewModel

@Composable
fun HomeScreen(vm: FinanceViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val monthlyTotal = state.transactions.sumOf { it.amount }
    val categoryNameMap = state.categories.associate { it.id to it.name }
    val categoryTotals = state.transactions.groupBy { categoryNameMap[it.categoryId] ?: "Unknown" }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("This Month: $${"%.2f".format(monthlyTotal)}", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.weight(1f))
        }

        Button(onClick = { vm.addTransaction(amount.toDoubleOrNull() ?: 0.0, 1, note.ifBlank { null }); amount = ""; note = "" }) {
            Text("Add Expense")
        }
        Button(onClick = vm::fetchSmartSuggestion) { Text("Get Smart Suggestion") }

        val recent = state.transactions.sortedBy { it.date }.takeLast(10)
        val monthGroups = state.transactions.groupBy { "${it.date.year}-${it.date.monthValue.toString().padStart(2, '0')}" }
            .toSortedMap()

        SimplePieChart(entries = categoryTotals, title = "Category Distribution")
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

        Text("Insights", style = MaterialTheme.typography.titleMedium)
        state.insights.forEach { Text("• $it") }

        state.suggestion?.let { s ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp)) {
                    Text("Suggested Budget: $${"%.2f".format(s.suggestedTotal)}")
                    s.tips.forEach { Text("- $it") }
                }
            }
        }

        LazyColumn {
            items(state.transactions) { txn ->
                Text("$${txn.amount} • ${txn.date} • ${txn.note ?: "No note"}")
            }
        }
    }
}
