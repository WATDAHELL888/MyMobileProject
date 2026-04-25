package com.example.mymobileproject.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val dayMonthTh = DateTimeFormatter.ofPattern("d MMM", Locale("th", "TH"))
    private val dayMonthEn = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
    private val fullDateTh = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("th", "TH"))
    private val fullDateEn = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    private val monthYearTh = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("th", "TH"))
    private val monthYearEn = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun formatDayMonth(date: LocalDate, isThai: Boolean = true): String =
        date.format(if (isThai) dayMonthTh else dayMonthEn)

    fun formatFullDate(date: LocalDate, isThai: Boolean = true): String =
        date.format(if (isThai) fullDateTh else fullDateEn)

    fun formatMonthYear(yearMonth: YearMonth, isThai: Boolean = true): String =
        yearMonth.atDay(1).format(if (isThai) monthYearTh else monthYearEn)

    fun formatTime(dateTime: LocalDateTime): String = dateTime.format(timeFmt)

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun isYesterday(date: LocalDate): Boolean = date == LocalDate.now().minusDays(1)

    fun getStartOfMonth(): LocalDate = LocalDate.now().withDayOfMonth(1)

    fun getStartOfPreviousMonth(): LocalDate =
        LocalDate.now().minusMonths(1).withDayOfMonth(1)

    fun getEndOfPreviousMonth(): LocalDate =
        LocalDate.now().withDayOfMonth(1).minusDays(1)
}
