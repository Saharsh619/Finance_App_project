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
    val categoryTotals = state.transactions.groupBy { it.categoryId }.mapValues { it.value.sumOf { t -> t.amount } }

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

        val recentTransactions = state.transactions.take(10).reversed()
        SimplePieChart(categoryTotals.values.toList())
        SimpleLineChart(
            values = recentTransactions.map { it.amount },
            xLabels = recentTransactions.map { it.date.dayOfMonth.toString() }
        )
        val monthGroups = state.transactions.groupBy { it.date.monthValue }.toSortedMap()
        SimpleBarChart(
            values = monthGroups.values.map { list -> list.sumOf { it.amount } },
            xLabels = monthGroups.keys.map { it.toString() }
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
