package com.example.mymobileproject.data.repository

import com.example.mymobileproject.domain.model.Transaction
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.domain.model.TransactionType
import com.example.mymobileproject.domain.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    private fun userTransactionsRef() =
        firestore.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("transactions")

    private fun LocalDate.toDate(): Date =
        Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

    private fun Date.toLocalDate(): LocalDate =
        this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    override fun getTransactions(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> =
        callbackFlow {
            val listener = userTransactionsRef()
                .whereGreaterThanOrEqualTo("date", startDate.toDate())
                .whereLessThanOrEqualTo("date", endDate.plusDays(1).toDate())
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val transactions = snapshot?.documents?.mapNotNull { doc ->
                        docToTransaction(doc.id, doc.data)
                    } ?: emptyList()
                    trySend(transactions)
                }
            awaitClose { listener.remove() }
        }

    override fun getAllTransactions(): Flow<List<Transaction>> = callbackFlow {
        val listener = userTransactionsRef()
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    docToTransaction(doc.id, doc.data)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<String> = runCatching {
        val data = mapOf(
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "category" to transaction.category.name,
            "note" to transaction.note,
            "imageUrl" to transaction.imageUrl,
            "location" to transaction.location,
            "date" to transaction.date.toDate(),
            "createdAt" to Date()
        )
        val docRef = userTransactionsRef().add(data).await()
        docRef.id
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> = runCatching {
        val data = mapOf(
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "category" to transaction.category.name,
            "note" to transaction.note,
            "imageUrl" to transaction.imageUrl,
            "location" to transaction.location,
            "date" to transaction.date.toDate()
        )
        userTransactionsRef().document(transaction.id).update(data).await()
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> = runCatching {
        userTransactionsRef().document(transactionId).delete().await()
    }

    override fun getTransactionsByCategory(
        category: TransactionCategory,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>> = callbackFlow {
        val listener = userTransactionsRef()
            .whereEqualTo("category", category.name)
            .whereGreaterThanOrEqualTo("date", startDate.toDate())
            .whereLessThanOrEqualTo("date", endDate.plusDays(1).toDate())
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    docToTransaction(doc.id, doc.data)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }

    private fun docToTransaction(id: String, data: Map<String, Any>?): Transaction? {
        data ?: return null
        return try {
            Transaction(
                id = id,
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                type = TransactionType.valueOf(data["type"] as? String ?: "EXPENSE"),
                category = TransactionCategory.valueOf(data["category"] as? String ?: "OTHER"),
                note = data["note"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String,
                location = data["location"] as? String,
                date = (data["date"] as? com.google.firebase.Timestamp)
                    ?.toDate()?.toLocalDate() ?: LocalDate.now(),
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)
                    ?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                    ?: java.time.LocalDateTime.now()
            )
        } catch (e: Exception) { null }
    }
}
