package com.example.financeapp.domain.usecases

import com.example.financeapp.domain.models.SummaryData
import com.example.financeapp.domain.models.Transaction
import javax.inject.Inject

class EstimateBudgetUseCase @Inject constructor() {
    fun run(monthlyTransactions: List<List<Transaction>>, categoryNames: Map<Long, String>): SummaryData {
        val monthlySpending = monthlyTransactions.map { month -> month.sumOf { it.amount } }
        val trend = when {
            monthlySpending.size < 2 -> "stable"
            monthlySpending.last() > monthlySpending.first() -> "increasing"
            monthlySpending.last() < monthlySpending.first() -> "decreasing"
            else -> "stable"
        }

        val latest = monthlyTransactions.lastOrNull().orEmpty()
        val categoryBreakdown = latest.groupBy { categoryNames[it.categoryId] ?: "Unknown" }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

        return SummaryData(monthlySpending, categoryBreakdown, trend)
    }
}
