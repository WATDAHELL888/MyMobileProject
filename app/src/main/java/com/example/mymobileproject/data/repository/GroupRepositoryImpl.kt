package com.example.mymobileproject.data.repository

import com.example.mymobileproject.domain.model.Group
import com.example.mymobileproject.domain.model.GroupExpense
import com.example.mymobileproject.domain.model.SplitType
import com.example.mymobileproject.domain.model.TransactionCategory
import com.example.mymobileproject.domain.repository.GroupRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : GroupRepository {

    private val groupsRef = firestore.collection("groups")
    private val currentUid get() = auth.currentUser?.uid ?: ""

    override fun getUserGroups(): Flow<List<Group>> = callbackFlow {
        val listener = groupsRef
            .whereArrayContains("members", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val groups = snapshot?.documents?.mapNotNull { doc ->
                    docToGroup(doc.id, doc.data)
                } ?: emptyList()
                trySend(groups)
            }
        awaitClose { listener.remove() }
    }

    override fun getGroup(groupId: String): Flow<Group?> = callbackFlow {
        val listener = groupsRef.document(groupId)
            .addSnapshotListener { doc, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(doc?.let { docToGroup(it.id, it.data) })
            }
        awaitClose { listener.remove() }
    }

    override fun getGroupExpenses(groupId: String): Flow<List<GroupExpense>> = callbackFlow {
        val listener = groupsRef.document(groupId).collection("expenses")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    docToExpense(doc.id, groupId, doc.data)
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createGroup(name: String, memberNames: List<String>): Result<String> = runCatching {
        val memberIds = mutableListOf(currentUid)
        val nameMap = mutableMapOf(currentUid to (auth.currentUser?.displayName ?: "Me"))

        // For simplicity, generate placeholder IDs for non-registered members
        memberNames.forEach { mName ->
            val tempId = UUID.randomUUID().toString().take(8)
            memberIds.add(tempId)
            nameMap[tempId] = mName
        }

        val data = mapOf(
            "name" to name,
            "members" to memberIds,
            "memberNames" to nameMap,
            "createdBy" to currentUid,
            "createdAt" to Date()
        )
        val docRef = groupsRef.add(data).await()
        docRef.id
    }

    override suspend fun addExpense(groupId: String, expense: GroupExpense): Result<String> = runCatching {
        val data = mapOf(
            "amount" to expense.amount,
            "category" to expense.category.name,
            "note" to expense.note,
            "paidBy" to expense.paidBy,
            "splitType" to expense.splitType.name,
            "splits" to expense.splits,
            "date" to Date.from(expense.date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            "createdAt" to Date()
        )
        val docRef = groupsRef.document(groupId).collection("expenses").add(data).await()
        docRef.id
    }

    override suspend fun deleteExpense(groupId: String, expenseId: String): Result<Unit> = runCatching {
        groupsRef.document(groupId).collection("expenses").document(expenseId).delete().await()
    }

    override suspend fun addMember(groupId: String, memberName: String): Result<Unit> = runCatching {
        val tempId = UUID.randomUUID().toString().take(8)
        groupsRef.document(groupId).update(
            mapOf(
                "members" to com.google.firebase.firestore.FieldValue.arrayUnion(tempId),
                "memberNames.$tempId" to memberName
            )
        ).await()
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = runCatching {
        // Delete all expenses in subcollection first
        val expenses = groupsRef.document(groupId).collection("expenses").get().await()
        for (doc in expenses.documents) {
            doc.reference.delete().await()
        }
        // Delete the group document
        groupsRef.document(groupId).delete().await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun docToGroup(id: String, data: Map<String, Any>?): Group? {
        data ?: return null
        return try {
            Group(
                id = id,
                name = data["name"] as? String ?: "",
                members = data["members"] as? List<String> ?: emptyList(),
                memberNames = data["memberNames"] as? Map<String, String> ?: emptyMap(),
                createdBy = data["createdBy"] as? String ?: ""
            )
        } catch (e: Exception) { null }
    }

    @Suppress("UNCHECKED_CAST")
    private fun docToExpense(id: String, groupId: String, data: Map<String, Any>?): GroupExpense? {
        data ?: return null
        return try {
            val rawSplits = data["splits"] as? Map<String, Any> ?: emptyMap()
            val splits = rawSplits.mapValues { (it.value as? Number)?.toDouble() ?: 0.0 }
            GroupExpense(
                id = id,
                groupId = groupId,
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                category = TransactionCategory.valueOf(data["category"] as? String ?: "OTHER"),
                note = data["note"] as? String ?: "",
                paidBy = data["paidBy"] as? String ?: "",
                splitType = SplitType.valueOf(data["splitType"] as? String ?: "EQUAL"),
                splits = splits,
                date = (data["date"] as? com.google.firebase.Timestamp)?.toDate()
                    ?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()
            )
        } catch (e: Exception) { null }
    }
}
