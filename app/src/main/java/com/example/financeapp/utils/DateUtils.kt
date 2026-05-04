package com.example.financeapp.utils

import java.time.LocalDate
import java.time.YearMonth

object DateUtils {
    fun lastNMonthRanges(n: Int): List<Pair<LocalDate, LocalDate>> {
        val now = YearMonth.now()
        return (n - 1 downTo 0).map { offset ->
            val ym = now.minusMonths(offset.toLong())
            ym.atDay(1) to ym.atEndOfMonth()
        }
    }
}
