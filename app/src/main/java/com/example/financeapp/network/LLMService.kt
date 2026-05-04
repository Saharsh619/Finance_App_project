package com.example.financeapp.network

import com.example.financeapp.domain.models.BudgetSuggestion
import com.example.financeapp.domain.models.SummaryData

interface LLMService {
    suspend fun getBudgetSuggestion(summary: SummaryData): BudgetSuggestion
}
