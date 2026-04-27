package com.example.mymobileproject.data.repository

import com.example.mymobileproject.core.util.CurrencyUtils
import com.example.mymobileproject.domain.model.*
import com.example.mymobileproject.domain.repository.AIRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor() : AIRepository {

    // ── Smart Chat with financial context ──
    override suspend fun chat(
        userMessage: String,
        history: List<AIMessage>,
        spendingSummary: SpendingSummary?,
        recentTransactions: List<Transaction>
    ): Result<String> = runCatching {
        kotlinx.coroutines.delay(600)
        val lower = userMessage.lowercase()
        val s = spendingSummary

        when {
            // เท่าไหร่ / how much
            lower.containsAny("เท่าไหร่", "how much", "ใช้เงิน", "spent", "ยอด") -> {
                if (s != null && s.totalExpense > 0) {
                    buildString {
                        append("📊 สรุปเดือนนี้:\n\n")
                        append("💰 รายรับ: ${CurrencyUtils.formatBaht(s.totalIncome)}\n")
                        append("💸 รายจ่าย: ${CurrencyUtils.formatBaht(s.totalExpense)}\n")
                        append("📌 คงเหลือ: ${CurrencyUtils.formatBaht(s.balance)}\n\n")
                        if (s.balance < 0) append("⚠️ คุณใช้เงินเกินรายรับแล้ว! ควรระวังการใช้จ่าย\n")
                        else append("✅ ยังอยู่ในเกณฑ์ดี เก็บเงินได้ ${(s.balance / s.totalIncome * 100).toInt()}%\n")
                        append("\nถามว่า \"วิเคราะห์\" เพื่อดูรายละเอียดหมวดหมู่ 📂")
                    }
                } else "ยังไม่มีข้อมูลรายจ่ายเดือนนี้ ลองเพิ่มรายการก่อนนะครับ 📝"
            }

            // ประหยัด / save
            lower.containsAny("ประหยัด", "save", "ลด", "reduce", "ออม") -> {
                if (s != null && s.totalExpense > 0) {
                    val tips = generateSavingTips(s)
                    buildString {
                        append("💡 คำแนะนำการประหยัด:\n\n")
                        tips.forEachIndexed { i, tip -> append("${i + 1}. $tip\n") }
                        val saveable = s.totalExpense * 0.15
                        append("\n🎯 ถ้าทำตาม ประหยัดได้ ~${CurrencyUtils.formatBaht(saveable)}/เดือน!")
                    }
                } else "เพิ่มข้อมูลรายจ่ายก่อนนะครับ!"
            }

            // วิเคราะห์ / analyze
            lower.containsAny("วิเคราะห์", "analyze", "สรุป", "breakdown") -> {
                val baseAnalysis = analyzeSpending(s ?: SpendingSummary()).getOrElse { "ไม่สามารถวิเคราะห์ได้" }
                if (recentTransactions.isNotEmpty()) {
                    val merchantAnalysis = extractMerchantInsights(recentTransactions)
                    "$baseAnalysis\n\n$merchantAnalysis"
                } else baseAnalysis
            }
            
            // ร้านค้าเฉพาะเจาะจง
            lower.containsAny("ร้านไหน", "ซื้อที่ไหน", "merchant", "ร้าน") -> {
                if (recentTransactions.isNotEmpty()) {
                    "🛒 สรุปการซื้อรายร้านค้า:\n\n${extractMerchantInsights(recentTransactions)}"
                } else "ยังไม่มีข้อมูลรายชื่อร้านค้าในรายการรายจ่ายของคุณครับ"
            }

            // budget / งบ
            lower.containsAny("budget", "งบ", "แนะนำ", "recommend") -> {
                if (s != null && s.totalExpense > 0) {
                    val rec = getBudgetRecommendation(s).getOrNull()
                    if (rec != null) {
                        buildString {
                            append("📊 แนะนำ Budget เดือนหน้า:\n\n")
                            append("💰 งบรวม: ${CurrencyUtils.formatBaht(rec.suggestedBudget)}\n\n")
                            rec.categoryBudgets.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                                append("${catEmoji(cat)} ${catTh(cat)}: ${CurrencyUtils.formatBaht(amt)}\n")
                            }
                            if (rec.tips.isNotEmpty()) {
                                append("\n💡 Tips:\n")
                                rec.tips.forEach { append("• $it\n") }
                            }
                            append("\n🎯 ศักยภาพเก็บเงิน: ${CurrencyUtils.formatBaht(rec.savingsPotential)}/เดือน")
                        }
                    } else "ไม่สามารถแนะนำได้ ลองอีกครั้ง"
                } else "เพิ่มข้อมูลก่อนนะครับ!"
            }

            // ผิดปกติ / anomaly
            lower.containsAny("ผิดปกติ", "anomal", "แปลก", "เยอะ", "สูง") ->
                detectAnomalies(s ?: SpendingSummary(), null).getOrElse { "ไม่พบข้อมูลเพียงพอ" }

            // กลุ่ม / group
            lower.containsAny("กลุ่ม", "group", "หาร") ->
                "💡 ไปที่ Tab 'Groups' เพื่อดูการวิเคราะห์ค่าใช้จ่ายกลุ่ม\nหรือถามว่า \"วิเคราะห์\" เพื่อดูรายจ่ายส่วนตัว 📊"

            // สวัสดี
            lower.containsAny("สวัสดี", "hello", "hi", "ดี") -> "สวัสดีครับ! 👋 ถามเรื่องการเงินได้เลย เช่น:\n• \"เดือนนี้ใช้เท่าไหร่?\"\n• \"แนะนำ budget\"\n• \"ช่วยวิเคราะห์การใช้จ่าย\"\n• \"มีอะไรผิดปกติไหม?\""
            lower.containsAny("ขอบคุณ", "thanks") -> "ยินดีครับ! 😊 ถ้ามีอะไรถามเพิ่มได้ตลอด 💰"
            else -> "ผมช่วยได้หลายอย่างครับ:\n🔍 วิเคราะห์การใช้จ่าย\n💡 แนะนำวิธีประหยัด\n📊 แนะนำ budget\n⚠️ ตรวจจับความผิดปกติ\n\nลองถามดูนะครับ! 😊"
        }
    }

    // ── Deep Spending Analysis ──
    override suspend fun analyzeSpending(summary: SpendingSummary): Result<String> = runCatching {
        if (summary.totalExpense == 0.0) return@runCatching "ยังไม่มีข้อมูลการใช้จ่าย"
        val t = summary.totalExpense
        buildString {
            append("📈 วิเคราะห์พฤติกรรมการเงิน:\n\n")
            append("💸 รายจ่ายรวม: ${CurrencyUtils.formatBaht(t)}\n\n")

            // Category breakdown sorted
            append("📂 แยกตามหมวด:\n")
            summary.categoryBreakdown.entries.sortedByDescending { it.value }.forEach { (c, a) ->
                val pct = (a / t * 100).toInt()
                val bar = "█".repeat((pct / 5).coerceIn(1, 20))
                append("${catEmoji(c)} ${catTh(c)}: ${CurrencyUtils.formatBaht(a)} ($pct%) $bar\n")
            }

            // Top spending category warning
            val top = summary.categoryBreakdown.maxByOrNull { it.value }
            if (top != null) {
                val topPct = (top.value / t * 100).toInt()
                append("\n")
                if (topPct > 50) {
                    append("⚠️ ${catTh(top.key)}สูงผิดปกติ (${topPct}%)! ควรลดลงอย่างน้อย 20%\n")
                    append("💡 ลด 20% = ประหยัด ${CurrencyUtils.formatBaht(top.value * 0.2)}/เดือน\n")
                } else if (topPct > 35) {
                    append("📌 ${catTh(top.key)}ค่อนข้างสูง (${topPct}%) ควรระวัง\n")
                } else {
                    append("✅ การใช้จ่ายกระจายตัวดี!\n")
                }
            }

            // Daily spending pattern
            if (summary.dailySpending.isNotEmpty()) {
                val avgDaily = t / summary.dailySpending.size
                val maxDay = summary.dailySpending.maxByOrNull { it.value }
                append("\n📅 ค่าเฉลี่ย/วัน: ${CurrencyUtils.formatBaht(avgDaily)}")
                if (maxDay != null && maxDay.value > avgDaily * 2) {
                    append("\n⚡ วันที่ ${maxDay.key} ใช้เงินสูงสุด ${CurrencyUtils.formatBaht(maxDay.value)}")
                }
            }
        }
    }

    // ── Quick Insight ──
    override suspend fun getInsight(summary: SpendingSummary): Result<String> = runCatching {
        if (summary.totalExpense == 0.0) return@runCatching "เริ่มบันทึกรายจ่ายเพื่อรับ AI Insight! 📊"
        val top = summary.categoryBreakdown.maxByOrNull { it.value }
        val pct = if (top != null) (top.value / summary.totalExpense * 100).toInt() else 0
        val saveable = summary.totalExpense * 0.15
        "${catEmoji(top?.key)} ${catTh(top?.key)} ${pct}% | รวม ${CurrencyUtils.formatBaht(summary.totalExpense)} | ประหยัดได้ ~${CurrencyUtils.formatBaht(saveable)}"
    }

    // ── Budget Recommendation ──
    override suspend fun getBudgetRecommendation(summary: SpendingSummary): Result<BudgetRecommendation> = runCatching {
        val target = summary.totalExpense * 0.85 // 15% reduction
        val catBudgets = summary.categoryBreakdown.mapValues { (cat, amount) ->
            when {
                cat == TransactionCategory.FOOD -> amount * 0.85
                cat == TransactionCategory.ENTERTAINMENT -> amount * 0.7
                cat == TransactionCategory.SHOPPING -> amount * 0.75
                else -> amount * 0.9
            }
        }
        val tips = mutableListOf<String>()
        summary.categoryBreakdown.entries.sortedByDescending { it.value }.take(3).forEach { (cat, amt) ->
            val pct = (amt / summary.totalExpense * 100).toInt()
            when (cat) {
                TransactionCategory.FOOD -> {
                    tips.add("🍔 ทำอาหารเอง 2-3 วัน/สัปดาห์ ประหยัดได้ ~${CurrencyUtils.formatBaht(amt * 0.2)}")
                    if (pct > 40) tips.add("☕ ลดกาแฟร้านเหลือ 3 แก้ว/สัปดาห์")
                }
                TransactionCategory.TRANSPORT -> {
                    tips.add("🚌 ใช้ขนส่งสาธารณะแทน ประหยัดได้ ~${CurrencyUtils.formatBaht(amt * 0.3)}")
                }
                TransactionCategory.SHOPPING -> {
                    tips.add("🛍️ ตั้งกฎ \"รอ 24 ชม.\" ก่อนซื้อของที่ไม่จำเป็น")
                }
                TransactionCategory.ENTERTAINMENT -> {
                    tips.add("🎬 หากิจกรรมฟรีทดแทน เช่น สวนสาธารณะ, YouTube")
                }
                else -> tips.add("💡 ลด${catTh(cat)}ลง 10% ประหยัด ${CurrencyUtils.formatBaht(amt * 0.1)}")
            }
        }
        tips.add("📌 ตั้งเป้าออม ${CurrencyUtils.formatBaht(summary.totalIncome * 0.2)}/เดือน (20% ของรายรับ)")

        BudgetRecommendation(
            suggestedBudget = target,
            categoryBudgets = catBudgets,
            tips = tips,
            savingsPotential = summary.totalExpense - target
        )
    }

    // ── Anomaly Detection ──
    override suspend fun detectAnomalies(
        summary: SpendingSummary,
        lastMonthSummary: SpendingSummary?
    ): Result<String> = runCatching {
        if (summary.totalExpense == 0.0) return@runCatching "ยังไม่มีข้อมูลเพียงพอ"
        buildString {
            append("🔍 ตรวจจับความผิดปกติ:\n\n")
            var found = false

            // Check single-day spikes
            if (summary.dailySpending.isNotEmpty()) {
                val avg = summary.totalExpense / summary.dailySpending.size.coerceAtLeast(1)
                summary.dailySpending.filter { it.value > avg * 2.5 }.forEach { (day, amt) ->
                    append("⚡ วันที่ $day: ${CurrencyUtils.formatBaht(amt)} (สูงกว่าปกติ ${(amt / avg).toInt()}x)\n")
                    found = true
                }
            }

            // Category anomalies
            summary.categoryBreakdown.forEach { (cat, amt) ->
                val pct = amt / summary.totalExpense * 100
                if (pct > 50) {
                    append("⚠️ ${catEmoji(cat)} ${catTh(cat)}ใช้ถึง ${pct.toInt()}% ของรายจ่ายทั้งหมด!\n")
                    append("   ↳ ลองตั้งเพดานไม่เกิน ${CurrencyUtils.formatBaht(summary.totalExpense * 0.35)}\n")
                    found = true
                }
            }

            // Income vs expense warning
            if (summary.balance < 0) {
                append("🚨 รายจ่ายเกินรายรับ ${CurrencyUtils.formatBaht(-summary.balance)}!\n")
                append("   ↳ ต้องลดรายจ่ายหรือเพิ่มรายรับด่วน\n")
                found = true
            } else if (summary.totalIncome > 0 && summary.balance / summary.totalIncome < 0.1) {
                append("⚠️ เงินเหลือแค่ ${(summary.balance / summary.totalIncome * 100).toInt()}% ของรายรับ\n")
                append("   ↳ ควรเหลืออย่างน้อย 20%\n")
                found = true
            }

            if (!found) append("✅ ไม่พบความผิดปกติ การใช้จ่ายอยู่ในเกณฑ์ปกติ 👍")
        }
    }

    // ── Group Spending Analysis ──
    override suspend fun analyzeGroupSpending(
        groupName: String,
        expenses: List<GroupExpense>,
        memberNames: Map<String, String>
    ): Result<String> = runCatching {
        if (expenses.isEmpty()) return@runCatching "กลุ่ม \"$groupName\" ยังไม่มีรายการ"
        val total = expenses.sumOf { it.amount }
        val memberSpend = expenses.groupBy { it.paidBy }.mapValues { (_, v) -> v.sumOf { it.amount } }
        val catSpend = expenses.groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amount } }
        val memberCount = memberNames.size
        val avgPerPerson = total / memberCount

        buildString {
            append("👥 วิเคราะห์กลุ่ม \"$groupName\":\n\n")
            append("💰 ค่าใช้จ่ายรวม: ${CurrencyUtils.formatBaht(total)}\n")
            append("👤 เฉลี่ย/คน: ${CurrencyUtils.formatBaht(avgPerPerson)}\n\n")

            // Who paid most
            append("💳 ใครจ่ายเยอะสุด:\n")
            memberSpend.entries.sortedByDescending { it.value }.forEach { (uid, amt) ->
                val name = memberNames[uid] ?: uid
                val pct = (amt / total * 100).toInt()
                append("  $name: ${CurrencyUtils.formatBaht(amt)} ($pct%)\n")
            }

            // Category breakdown
            append("\n📂 แยกตามหมวด:\n")
            catSpend.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                append("  ${catEmoji(cat)} ${catTh(cat)}: ${CurrencyUtils.formatBaht(amt)}\n")
            }

            // Budget recommendation per person
            append("\n📊 แนะนำงบครั้งหน้า:\n")
            append("  งบรวม: ${CurrencyUtils.formatBaht(total * 0.9)} (ลด 10%)\n")
            append("  งบ/คน: ${CurrencyUtils.formatBaht(total * 0.9 / memberCount)}\n")

            // Tips
            val topCat = catSpend.maxByOrNull { it.value }
            if (topCat != null) {
                val topPct = (topCat.value / total * 100).toInt()
                append("\n💡 Tips: ${catTh(topCat.key)}ใช้เยอะสุด ($topPct%) ")
                when (topCat.key) {
                    TransactionCategory.FOOD -> append("ลองทำอาหารเองบ้าง!")
                    TransactionCategory.TRANSPORT -> append("ลองใช้รถร่วมกัน!")
                    TransactionCategory.ENTERTAINMENT -> append("หากิจกรรมฟรีๆ ทดแทน!")
                    else -> append("ลองหาทางลดดู!")
                }
            }
        }
    }

    // ── Helpers ──
    private fun generateSavingTips(s: SpendingSummary): List<String> {
        val tips = mutableListOf<String>()
        s.categoryBreakdown.entries.sortedByDescending { it.value }.take(3).forEach { (cat, amt) ->
            when (cat) {
                TransactionCategory.FOOD -> {
                    tips.add("🍔 ลดทานข้างนอก ทำอาหารเอง → ประหยัด ~${CurrencyUtils.formatBaht(amt * 0.25)}/เดือน")
                    tips.add("☕ ลดกาแฟเหลือ 3 แก้ว/สัปดาห์ → ประหยัด ~฿900/เดือน")
                }
                TransactionCategory.TRANSPORT -> tips.add("🚌 ใช้ขนส่งสาธารณะ → ประหยัด ~${CurrencyUtils.formatBaht(amt * 0.3)}/เดือน")
                TransactionCategory.SHOPPING -> tips.add("🛍️ ใช้กฎ 24 ชม. ก่อนซื้อของ → ลดการใช้จ่ายไม่จำเป็น")
                TransactionCategory.ENTERTAINMENT -> tips.add("🎬 หากิจกรรมฟรี → ประหยัด ~${CurrencyUtils.formatBaht(amt * 0.3)}/เดือน")
                TransactionCategory.BILLS -> tips.add("📄 เปรียบเทียบแพ็กเกจ/เปลี่ยน plan → ลดค่าใช้จ่ายคงที่")
                else -> {}
            }
        }
        if (s.totalIncome > 0) tips.add("🎯 ตั้งเป้าออม 20% = ${CurrencyUtils.formatBaht(s.totalIncome * 0.2)}/เดือน")
        return tips
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

    private fun extractMerchantInsights(transactions: List<Transaction>): String {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE && it.note.isNotBlank() }
        if (expenses.isEmpty()) return ""
        
        // Group by note (assuming note contains merchant name like "Starbucks", "7-11")
        val merchantSpending = expenses.groupBy { it.note.trim() }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
            
        return buildString {
            if (merchantSpending.isNotEmpty()) {
                val top = merchantSpending.first()
                if (top.value > 1000 || merchantSpending.size >= 1) {
                    append("🛍️ Insight ร้านค้า:\n")
                    append("• คุณจ่ายให้ ${top.key} ไปถึง ${CurrencyUtils.formatBaht(top.value)} ")
                    if (expenses.count { it.note.trim() == top.key } > 3) append("(ซื้อบ่อยมาก!)\n") else append("\n")
                }
                
                // Show top 3
                merchantSpending.take(3).forEachIndexed { i, (merchant, amount) ->
                    if (i > 0) append("• ${merchant}: ${CurrencyUtils.formatBaht(amount)}\n")
                }
                
                // Suggestion based on brand
                if (top.key.lowercase().containsAny("starbucks", "กาแฟ", "cafe", "อเมซอน")) {
                    append("\n💡 ลองชงกาแฟทานเองบ้าง จะช่วยลดค่า ${top.key} ได้เยอะเลยครับ!")
                } else if (top.key.lowercase().containsAny("7-11", "เซเว่น", "family")) {
                    append("\n💡 ซื้อของจาก ${top.key} บ่อย อาจจะลองซื้อของตุนจากซุปเปอร์มาร์เก็ตทีเดียวจะถูกกว่าครับ!")
                }
            }
        }
    }
}
