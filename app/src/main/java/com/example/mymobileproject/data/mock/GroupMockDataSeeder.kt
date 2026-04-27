package com.example.mymobileproject.data.mock

import com.example.mymobileproject.domain.model.GroupExpense
import com.example.mymobileproject.domain.model.SplitType
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.domain.repository.GroupRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds Firestore with realistic group mock data for testing.
 * Creates a group "ทริปเชียงใหม่ 🏔️" with 4 members and 8 expenses.
 */
@Singleton
class GroupMockDataSeeder @Inject constructor(
    private val groupRepo: GroupRepository,
    private val auth: FirebaseAuth
) {
    suspend fun seed(): Result<String> = runCatching {
        // 1. Create Group with 3 extra members (current user is auto-added)
        val groupId = groupRepo.createGroup(
            name = "ทริปเชียงใหม่ 🏔️",
            memberNames = listOf("บอล", "มิ้นท์", "เจมส์")
        ).getOrThrow()

        // Wait for Firestore to propagate
        delay(1500)

        // Fetch group to get generated member IDs
        val group = groupRepo.getGroup(groupId).first { it != null }
            ?: throw Exception("Group not found")
        val members = group.members

        // Ensure we have members (currentUser + 3 others)
        if (members.size < 4) throw Exception("Group members not loaded: ${members.size}")

        val me = members[0]       // current user
        val ball = members[1]     // บอล
        val mint = members[2]     // มิ้นท์
        val james = members[3]    // เจมส์

        // 2. Add Expenses — various categories, payers, and split types
        val expenses = listOf(
            // 🍔 ข้าวเย็นร้านอาหาร — มิ้นท์จ่าย, หารเท่ากัน 4 คน
            GroupExpense(
                groupId = groupId, amount = 1200.0,
                category = TransactionCategory.FOOD, note = "ข้าวเย็นร้านอาหารเหนือ",
                paidBy = mint, splitType = SplitType.EQUAL,
                splits = mapOf(me to 300.0, ball to 300.0, mint to 300.0, james to 300.0),
                date = LocalDate.now().minusDays(3)
            ),
            // 🚗 ค่ารถเช่า — เจมส์จ่าย, หาร 4 คน
            GroupExpense(
                groupId = groupId, amount = 2400.0,
                category = TransactionCategory.TRANSPORT, note = "ค่าเช่ารถ 2 วัน",
                paidBy = james, splitType = SplitType.EQUAL,
                splits = mapOf(me to 600.0, ball to 600.0, mint to 600.0, james to 600.0),
                date = LocalDate.now().minusDays(3)
            ),
            // 🍔 กาแฟ — ฉันจ่าย, หารแค่ 3 คน (เจมส์ไม่ดื่ม)
            GroupExpense(
                groupId = groupId, amount = 450.0,
                category = TransactionCategory.FOOD, note = "กาแฟร้าน Ristr8to",
                paidBy = me, splitType = SplitType.EQUAL,
                splits = mapOf(me to 150.0, ball to 150.0, mint to 150.0),
                date = LocalDate.now().minusDays(2)
            ),
            // 🎬 ค่าตั๋วซิปไลน์ — บอลจ่าย, หาร 4 คน
            GroupExpense(
                groupId = groupId, amount = 3200.0,
                category = TransactionCategory.ENTERTAINMENT, note = "ซิปไลน์ Flight of the Gibbon",
                paidBy = ball, splitType = SplitType.EQUAL,
                splits = mapOf(me to 800.0, ball to 800.0, mint to 800.0, james to 800.0),
                date = LocalDate.now().minusDays(2)
            ),
            // 🛍️ ของฝาก — มิ้นท์จ่าย, custom split
            GroupExpense(
                groupId = groupId, amount = 800.0,
                category = TransactionCategory.SHOPPING, note = "ของฝากจากวัดพระธาตุ",
                paidBy = mint, splitType = SplitType.CUSTOM,
                splits = mapOf(me to 250.0, ball to 200.0, mint to 200.0, james to 150.0),
                date = LocalDate.now().minusDays(1)
            ),
            // 🍔 ข้าวเช้า — ฉันจ่าย, หาร 4 คน
            GroupExpense(
                groupId = groupId, amount = 560.0,
                category = TransactionCategory.FOOD, note = "ข้าวซอยไก่ + กาแฟเช้า",
                paidBy = me, splitType = SplitType.EQUAL,
                splits = mapOf(me to 140.0, ball to 140.0, mint to 140.0, james to 140.0),
                date = LocalDate.now().minusDays(1)
            ),
            // 📄 ค่าห้อง — เจมส์จ่าย, หาร 4 คน
            GroupExpense(
                groupId = groupId, amount = 4800.0,
                category = TransactionCategory.BILLS, note = "ค่าห้อง Airbnb 2 คืน",
                paidBy = james, splitType = SplitType.EQUAL,
                splits = mapOf(me to 1200.0, ball to 1200.0, mint to 1200.0, james to 1200.0),
                date = LocalDate.now()
            ),
            // 🚗 ค่าน้ำมัน — บอลจ่าย, หาร 4 คน
            GroupExpense(
                groupId = groupId, amount = 900.0,
                category = TransactionCategory.TRANSPORT, note = "เติมน้ำมันรถ",
                paidBy = ball, splitType = SplitType.EQUAL,
                splits = mapOf(me to 225.0, ball to 225.0, mint to 225.0, james to 225.0),
                date = LocalDate.now()
            )
        )

        for (expense in expenses) {
            groupRepo.addExpense(groupId, expense)
            delay(200) // stagger writes
        }

        groupId
    }
}
