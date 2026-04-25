package com.example.mymobileproject.core.util

object Constants {
    const val FIRESTORE_USERS = "users"
    const val FIRESTORE_TRANSACTIONS = "transactions"
    const val FIRESTORE_GROUPS = "groups"
    const val FIRESTORE_EXPENSES = "expenses"

    const val STORAGE_RECEIPTS = "receipts"

    const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
    const val OPENAI_MODEL = "gpt-4o"

    const val MAX_AI_MESSAGES_PER_DAY = 50
    const val QUICK_ADD_PATTERN = """^(.+?)\s+(\d+(?:\.\d{1,2})?)$"""
}
