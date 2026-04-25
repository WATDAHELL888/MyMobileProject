package com.example.mymobileproject.domain.repository

import com.example.mymobileproject.domain.model.Group
import com.example.mymobileproject.domain.model.GroupExpense
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun getUserGroups(): Flow<List<Group>>
    fun getGroup(groupId: String): Flow<Group?>
    fun getGroupExpenses(groupId: String): Flow<List<GroupExpense>>
    suspend fun createGroup(name: String, memberNames: List<String>): Result<String>
    suspend fun addExpense(groupId: String, expense: GroupExpense): Result<String>
    suspend fun deleteExpense(groupId: String, expenseId: String): Result<Unit>
    suspend fun addMember(groupId: String, memberName: String): Result<Unit>
}
