package com.example.financeapp.network

import com.example.financeapp.domain.models.BudgetSuggestion
import com.example.financeapp.domain.models.SuggestionDto
import com.example.financeapp.domain.models.SummaryData
import retrofit2.http.Body
import retrofit2.http.POST

interface SuggestionApi {
    @POST("budget/suggest")
    suspend fun suggest(@Body summary: SummaryData): SuggestionDto
}

class RetrofitLLMService(
    private val api: SuggestionApi
) : LLMService {
    override suspend fun getBudgetSuggestion(summary: SummaryData): BudgetSuggestion {
        val response = api.suggest(summary)
        return BudgetSuggestion(response.suggestedBudget, response.categoryBudgets, response.tips)
    }
}
