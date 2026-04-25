package com.example.mymobileproject.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val note: String = "",
    val imageUrl: String? = null,
    val location: String? = null,
    val date: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType { INCOME, EXPENSE }

enum class TransactionCategory(val iconName: String) {
    FOOD("restaurant"),
    TRANSPORT("directions_car"),
    SHOPPING("shopping_bag"),
    ENTERTAINMENT("movie"),
    BILLS("receipt_long"),
    HEALTH("medical_services"),
    EDUCATION("school"),
    SALARY("payments"),
    FREELANCE("work"),
    OTHER("more_horiz");

    companion object {
        /** Try to guess category from text (for Quick Add) */
        fun guessFromText(text: String): TransactionCategory {
            val lower = text.lowercase()
            return when {
                lower.containsAny("กาแฟ","coffee","ชา","tea","ข้าว","rice","อาหาร","food","ก๋วยเตี๋ยว","ส้มตำ","ชาไข่มุก","boba","grab food","foodpanda","ร้าน","มาม่า","ขนม","snack") -> FOOD
                lower.containsAny("แท็กซี่","taxi","grab","bolt","รถ","bus","bts","mrt","train","เดินทาง","travel","น้ำมัน","gas","fuel","ตั๋ว","ticket") -> TRANSPORT
                lower.containsAny("ช้อป","shop","lazada","shopee","เสื้อ","กางเกง","รองเท้า","clothes") -> SHOPPING
                lower.containsAny("หนัง","movie","netflix","game","เกม","concert","spotify","youtube") -> ENTERTAINMENT
                lower.containsAny("ค่าน้ำ","ค่าไฟ","ค่าเน็ต","ค่าโทร","bill","internet","phone","rent","ค่าเช่า") -> BILLS
                lower.containsAny("ยา","หมอ","doctor","medicine","hospital","โรงพยาบาล","gym","ฟิตเนส") -> HEALTH
                lower.containsAny("เรียน","หนังสือ","book","course","คอร์ส","udemy","school") -> EDUCATION
                lower.containsAny("เงินเดือน","salary","bonus","โบนัส") -> SALARY
                lower.containsAny("freelance","ฟรีแลนซ์","งาน","project") -> FREELANCE
                else -> OTHER
            }
        }

        private fun String.containsAny(vararg keywords: String): Boolean =
            keywords.any { this.contains(it) }
    }
}
