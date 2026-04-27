package com.example.mymobileproject.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_prefs")

object WidgetDataStore {
    val BALANCE_KEY = doublePreferencesKey("balance")
    val SPEND_KEY = doublePreferencesKey("spend")
    val AI_INSIGHT_KEY = stringPreferencesKey("ai_insight")

    suspend fun saveWidgetData(context: Context, balance: Double, spend: Double, aiInsight: String) {
        context.widgetDataStore.edit { prefs ->
            prefs[BALANCE_KEY] = balance
            prefs[SPEND_KEY] = spend
            prefs[AI_INSIGHT_KEY] = aiInsight
        }
    }

    fun getWidgetData(context: Context): Flow<WidgetData> {
        return context.widgetDataStore.data.map { prefs ->
            WidgetData(
                balance = prefs[BALANCE_KEY] ?: 0.0,
                spend = prefs[SPEND_KEY] ?: 0.0,
                aiInsight = prefs[AI_INSIGHT_KEY] ?: ""
            )
        }
    }
}

data class WidgetData(
    val balance: Double,
    val spend: Double,
    val aiInsight: String
)
