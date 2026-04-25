package com.example.mymobileproject.domain.usecase.group

import com.example.mymobileproject.domain.model.GroupExpense
import com.example.mymobileproject.domain.model.Settlement
import javax.inject.Inject

/**
 * Smart Settlement Algorithm — minimizes the number of transactions needed.
 *
 * 1. Calculate net balance for each member (paid - owed)
 * 2. Separate into debtors (negative) and creditors (positive)
 * 3. Greedily match largest debtor with largest creditor
 * 4. Result: minimum number of settlements
 */
class SmartSettlementUseCase @Inject constructor() {

    operator fun invoke(
        expenses: List<GroupExpense>,
        memberNames: Map<String, String>
    ): List<Settlement> {
        if (expenses.isEmpty()) return emptyList()

        // Step 1: Calculate net balances
        val balances = mutableMapOf<String, Double>()
        for (expense in expenses) {
            // Person who paid gets credit
            balances[expense.paidBy] =
                (balances[expense.paidBy] ?: 0.0) + expense.amount

            // Each person's share is subtracted
            for ((uid, share) in expense.splits) {
                balances[uid] = (balances[uid] ?: 0.0) - share
            }
        }

        // Step 2: Separate into debtors and creditors
        val debtors = mutableListOf<Pair<String, Double>>() // uid, negative amount (owes)
        val creditors = mutableListOf<Pair<String, Double>>() // uid, positive amount (owed)

        for ((uid, balance) in balances) {
            when {
                balance < -0.01 -> debtors.add(uid to balance)
                balance > 0.01 -> creditors.add(uid to balance)
            }
        }

        // Step 3: Greedy matching
        val settlements = mutableListOf<Settlement>()
        debtors.sortBy { it.second }     // most negative first
        creditors.sortByDescending { it.second } // most positive first

        var i = 0
        var j = 0
        val debtAmounts = debtors.map { it.second }.toMutableList()
        val creditAmounts = creditors.map { it.second }.toMutableList()

        while (i < debtors.size && j < creditors.size) {
            val debtorUid = debtors[i].first
            val creditorUid = creditors[j].first
            val amount = minOf(-debtAmounts[i], creditAmounts[j])

            if (amount > 0.01) {
                settlements.add(
                    Settlement(
                        from = debtorUid,
                        fromName = memberNames[debtorUid] ?: debtorUid,
                        to = creditorUid,
                        toName = memberNames[creditorUid] ?: creditorUid,
                        amount = Math.round(amount * 100) / 100.0
                    )
                )
            }

            debtAmounts[i] += amount
            creditAmounts[j] -= amount

            if (Math.abs(debtAmounts[i]) < 0.01) i++
            if (Math.abs(creditAmounts[j]) < 0.01) j++
        }

        return settlements
    }
}
