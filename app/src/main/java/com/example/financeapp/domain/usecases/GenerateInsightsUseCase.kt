package com.example.financeapp.domain.usecases

import com.example.financeapp.domain.models.Category
import com.example.financeapp.domain.models.Transaction
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

class GenerateInsightsUseCase @Inject constructor() {
    fun run(transactions: List<Transaction>, categories: List<Category>, today: LocalDate = LocalDate.now()): List<String> {
        if (transactions.isEmpty()) return listOf("Start tracking expenses to unlock insights.")

        val currentWeek = transactions.filter { it.date >= today.minusDays(6) }
        val previousWeek = transactions.filter { it.date in today.minusDays(13)..today.minusDays(7) }
        val currentWeekTotal = currentWeek.sumOf { it.amount }
        val previousWeekTotal = previousWeek.sumOf { it.amount }

        val categoryTotals = transactions.groupBy { it.categoryId }.mapValues { (_, txns) -> txns.sumOf { it.amount } }
        val topCategory = categoryTotals.maxByOrNull { it.value }?.key
        val topCategoryName = categories.find { it.id == topCategory }?.name ?: "Unknown"

        val weekTrend = if (previousWeekTotal > 0) ((currentWeekTotal - previousWeekTotal) / previousWeekTotal) * 100 else 0.0

        return buildList {
            if (abs(weekTrend) >= 10) {
                add("You spent ${"%.0f".format(abs(weekTrend))}% ${if (weekTrend > 0) "more" else "less"} this week.")
            }
            add("Your highest expense category is $topCategoryName.")
            add(if (weekTrend > 0) "Spending is trending up compared to last week." else "Spending is stable or decreasing this week.")
        }
    }
}
