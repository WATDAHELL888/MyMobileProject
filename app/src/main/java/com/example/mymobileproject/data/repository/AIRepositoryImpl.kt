package com.example.mymobileproject.data.repository

import com.example.mymobileproject.core.util.CurrencyUtils
import com.example.mymobileproject.domain.model.AIMessage
import com.example.mymobileproject.domain.model.SpendingSummary
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.domain.repository.AIRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor() : AIRepository {

    override suspend fun chat(
        userMessage: String,
        history: List<AIMessage>,
        spendingSummary: SpendingSummary?
    ): Result<String> = runCatching {
        kotlinx.coroutines.delay(800)
        val lower = userMessage.lowercase()
        val s = spendingSummary
        when {
            lower.containsAny("เท่าไหร่","how much","ใช้เงิน","spent") -> {
                if (s != null) "เดือนนี้รายจ่าย ${CurrencyUtils.formatBaht(s.totalExpense)} รายรับ ${CurrencyUtils.formatBaht(s.totalIncome)}\nคงเหลือ ${CurrencyUtils.formatBaht(s.balance)} 💰"
                else "ยังไม่มีข้อมูลรายจ่ายเดือนนี้ ลองเพิ่มรายการก่อนนะครับ 📝"
            }
            lower.containsAny("ประหยัด","save","ลด","reduce") -> {
                if (s != null) { val top = s.categoryBreakdown.maxByOrNull { it.value }; "💡 หมวด${catTh(top?.key)}ใช้สูงสุด ${CurrencyUtils.formatBaht(top?.value ?: 0.0)}\nลด 20% ประหยัด ${CurrencyUtils.formatBaht((top?.value ?: 0.0) * 0.2)}/เดือน\n\nลองทำอาหารเอง ใช้ขนส่งสาธารณะ ตั้งเป้าออม 20% 🎯" }
                else "เพิ่มข้อมูลรายจ่ายก่อนนะครับ!"
            }
            lower.containsAny("วิเคราะห์","analyze") -> analyzeSpending(s ?: SpendingSummary()).getOrElse { "ไม่สามารถวิเคราะห์ได้" }
            lower.containsAny("budget","งบ","แนะนำ") -> {
                if (s != null) "📊 แนะนำ Budget:\n• งบรวม: ${CurrencyUtils.formatBaht(s.totalExpense * 0.9)}\nลดลง 10% จากเดือนนี้ 💪"
                else "เพิ่มข้อมูลก่อนนะครับ!"
            }
            lower.containsAny("สวัสดี","hello","hi") -> "สวัสดีครับ! 👋 ถามเรื่องการเงินได้เลย"
            else -> "ผมช่วยวิเคราะห์การใช้จ่าย แนะนำวิธีประหยัด แนะนำ budget ได้ครับ! 🤔"
        }
    }

    override suspend fun analyzeSpending(summary: SpendingSummary): Result<String> = runCatching {
        if (summary.totalExpense == 0.0) return@runCatching "ยังไม่มีข้อมูลการใช้จ่าย"
        val t = summary.totalExpense
        buildString {
            append("📈 วิเคราะห์การใช้จ่าย:\n\n")
            summary.categoryBreakdown.entries.sortedByDescending { it.value }.forEach { (c, a) ->
                append("${catEmoji(c)} ${catTh(c)}: ${CurrencyUtils.formatBaht(a)} (${(a/t*100).toInt()}%)\n")
            }
            val top = summary.categoryBreakdown.maxByOrNull { it.value }
            if (top != null && top.value / t > 0.5) append("\n⚠️ ${catTh(top.key)}สูงผิดปกติ ลองตั้ง budget ดู")
        }
    }

    override suspend fun getInsight(summary: SpendingSummary): Result<String> = runCatching {
        if (summary.totalExpense == 0.0) return@runCatching "เริ่มบันทึกรายจ่ายเพื่อรับ AI Insight! 📊"
        val top = summary.categoryBreakdown.maxByOrNull { it.value }
        val pct = if (top != null) (top.value / summary.totalExpense * 100).toInt() else 0
        "คุณใช้เงินกับ${catTh(top?.key)} ${pct}% ${catEmoji(top?.key)} | ยอดรวม ${CurrencyUtils.formatBaht(summary.totalExpense)}"
    }

    private fun catTh(c: TransactionCategory?) = when(c) {
        TransactionCategory.FOOD->"อาหาร"; TransactionCategory.TRANSPORT->"เดินทาง"
        TransactionCategory.SHOPPING->"ช้อปปิ้ง"; TransactionCategory.ENTERTAINMENT->"บันเทิง"
        TransactionCategory.BILLS->"บิล"; TransactionCategory.HEALTH->"สุขภาพ"
        TransactionCategory.EDUCATION->"การศึกษา"; else->"อื่นๆ"
    }
    private fun catEmoji(c: TransactionCategory?) = when(c) {
        TransactionCategory.FOOD->"🍔"; TransactionCategory.TRANSPORT->"🚗"
        TransactionCategory.SHOPPING->"🛍️"; TransactionCategory.ENTERTAINMENT->"🎬"
        TransactionCategory.BILLS->"📄"; TransactionCategory.HEALTH->"💊"
        TransactionCategory.EDUCATION->"📚"; else->"📦"
    }
    private fun String.containsAny(vararg kw: String) = kw.any { contains(it) }
}
