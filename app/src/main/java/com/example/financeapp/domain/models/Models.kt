package com.example.financeapp.domain.models

import kotlinx.serialization.Serializable
import java.time.LocalDate

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val date: LocalDate,
    val note: String?
)

data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isCustom: Boolean = false
)

data class BudgetSuggestion(
    val suggestedTotal: Double,
    val categoryBudgets: Map<String, Double>,
    val tips: List<String>
)

@Serializable
data class SummaryData(
    val monthlySpending: List<Double>,
    val categoryBreakdown: Map<String, Double>,
    val trend: String
)

@Serializable
data class SuggestionDto(
    val suggestedBudget: Double,
    val tips: List<String>,
    val categoryBudgets: Map<String, Double> = emptyMap()
)
