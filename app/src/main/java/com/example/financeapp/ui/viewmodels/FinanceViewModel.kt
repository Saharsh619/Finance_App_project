package com.example.financeapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.domain.models.BudgetSuggestion
import com.example.financeapp.domain.models.Category
import com.example.financeapp.domain.models.Transaction
import com.example.financeapp.domain.usecases.EstimateBudgetUseCase
import com.example.financeapp.domain.usecases.GenerateInsightsUseCase
import com.example.financeapp.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repo: FinanceRepository,
    private val insightsUseCase: GenerateInsightsUseCase,
    private val estimateBudgetUseCase: EstimateBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeDashboard().collect { (txns, categories) ->
                _uiState.update {
                    it.copy(
                        transactions = txns,
                        categories = categories,
                        insights = insightsUseCase.run(txns, categories)
                    )
                }
            }
        }
    }

    fun addTransaction(amount: Double, categoryId: Long, note: String?) = viewModelScope.launch {
        repo.upsertTransaction(
            Transaction(
                amount = amount,
                categoryId = categoryId,
                date = LocalDate.now(),
                note = note
            )
        )
    }

    // ✅ KEEP THIS (important feature)
    fun addExpenseWithCategory(amount: Double, categoryName: String, note: String?) =
        viewModelScope.launch {

            if (amount <= 0 || categoryName.isBlank()) return@launch

            val existing = uiState.value.categories.firstOrNull {
                it.name.equals(categoryName.trim(), ignoreCase = true)
            }

            val categoryId = if (existing != null) {
                existing.id
            } else {
                repo.addCategory(
                    Category(
                        name = categoryName.trim(),
                        colorHex = randomCategoryColor(categoryName),
                        isCustom = true
                    )
                )
            }

            repo.upsertTransaction(
                Transaction(
                    amount = amount,
                    categoryId = categoryId,
                    date = LocalDate.now(),
                    note = note
                )
            )
        }

    fun addCategory(name: String, colorHex: String) = viewModelScope.launch {
        repo.addCategory(Category(name = name, colorHex = colorHex, isCustom = true))
    }

    fun fetchSmartSuggestion() = viewModelScope.launch {
        val categoryNames = uiState.value.categories.associate { it.id to it.name }
        val summary = estimateBudgetUseCase.run(
            repo.getTransactionsForLastMonths(3),
            categoryNames
        )

        val result = repo.getSmartSuggestion(summary)

        _uiState.update {
            it.copy(
                suggestion = result.getOrElse {
                    fallbackSuggestion(summary.monthlySpending.average())
                }
            )
        }
    }

    private fun fallbackSuggestion(avg: Double) = BudgetSuggestion(
        suggestedTotal = avg,
        categoryBudgets = emptyMap(),
        tips = listOf("Offline mode: using local 3-month average as your suggested budget.")
    )
}

data class FinanceUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = defaultCategories,
    val insights: List<String> = emptyList(),
    val suggestion: BudgetSuggestion? = null
)

private val defaultCategories = listOf(
    Category(1, "Food", "#FF9800"),
    Category(2, "Travel", "#2196F3"),
    Category(3, "Bills", "#4CAF50"),
    Category(4, "Shopping", "#9C27B0")
)

// ✅ KEEP THIS (used in addExpenseWithCategory)
private fun randomCategoryColor(seed: String): String {
    val palette = listOf(
        "#3F51B5", "#F44336", "#FF9800",
        "#4CAF50", "#9C27B0", "#00BCD4"
    )
    return palette[(seed.lowercase().hashCode().absoluteValue) % palette.size]
}