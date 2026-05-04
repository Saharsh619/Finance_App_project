package com.example.financeapp.repository

import com.example.financeapp.data.local.CategoryEntity
import com.example.financeapp.data.local.FinanceDao
import com.example.financeapp.data.local.TransactionEntity
import com.example.financeapp.domain.models.BudgetSuggestion
import com.example.financeapp.domain.models.Category
import com.example.financeapp.domain.models.SummaryData
import com.example.financeapp.domain.models.Transaction
import com.example.financeapp.network.LLMService
import com.example.financeapp.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class FinanceRepository @Inject constructor(
    private val dao: FinanceDao,
    private val llmService: LLMService
) {

    fun observeTransactions(): Flow<List<Transaction>> =
        dao.observeTransactions().map { list ->
            list.map {
                Transaction(
                    it.id,
                    it.amount,
                    it.categoryId,
                    LocalDate.ofEpochDay(it.epochDay),
                    it.note
                )
            }
        }

    fun observeCategories(): Flow<List<Category>> =
        dao.observeCategories().map { list ->
            list.map {
                Category(it.id, it.name, it.colorHex, it.isCustom)
            }
        }

    fun observeDashboard() =
        combine(observeTransactions(), observeCategories()) { txns, cats ->
            txns to cats
        }

    suspend fun upsertTransaction(txn: Transaction) {
        dao.upsertTransaction(
            TransactionEntity(
                txn.id,
                txn.amount,
                txn.categoryId,
                txn.date.toEpochDay(),
                txn.note
            )
        )
    }

    // ✅ KEEP THIS VERSION
    suspend fun addCategory(category: Category): Long {
        return dao.upsertCategory(
            CategoryEntity(
                category.id,
                category.name,
                category.colorHex,
                category.isCustom
            )
        )
    }

    suspend fun getSmartSuggestion(summary: SummaryData): Result<BudgetSuggestion> =
        runCatching {
            llmService.getBudgetSuggestion(summary)
        }

    suspend fun getTransactionsForLastMonths(months: Int): List<List<Transaction>> {
        val ranges = DateUtils.lastNMonthRanges(months)

        return ranges.map { (start, end) ->
            dao.transactionsInRange(start.toEpochDay(), end.toEpochDay()).map {
                Transaction(
                    it.id,
                    it.amount,
                    it.categoryId,
                    LocalDate.ofEpochDay(it.epochDay),
                    it.note
                )
            }
        }
    }
}