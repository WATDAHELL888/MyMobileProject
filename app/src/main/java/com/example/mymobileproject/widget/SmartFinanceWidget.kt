package com.example.mymobileproject.widget

import android.content.Context
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.first
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProvider
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.mymobileproject.MainActivity
import com.example.mymobileproject.R
import com.example.mymobileproject.core.util.CurrencyUtils

class SmartFinanceWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = WidgetDataStore.getWidgetData(context).first()
        provideContent {
            GlanceTheme {
                WidgetContent(widgetData)
            }
        }
    }
}

@Composable
fun WidgetContent(widgetData: WidgetData) {

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_bg)) // Premium rounded dark background
            .padding(20.dp),
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = "${CurrencyUtils.formatBaht(widgetData.balance)}",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFF8FAFC), night = Color(0xFFF8FAFC)), // Bright White
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(6.dp))
                Text(
                    text = "Spend: ${CurrencyUtils.formatBaht(widgetData.spend)}",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF94A3B8), night = Color(0xFF94A3B8)), // Slate 400
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            
            // Custom circular Add button
            Box(
                modifier = GlanceModifier
                    .width(48.dp)
                    .height(48.dp)
                    .background(ImageProvider(R.drawable.widget_button_bg))
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(GlanceModifier.height(16.dp))

        // AI Insight
        val displayInsight = if (widgetData.aiInsight.isNotBlank()) widgetData.aiInsight else "เริ่มใช้งานแอปเพื่อรับคำแนะนำ AI"
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ImageProvider(R.drawable.insight_bg)) // Premium rounded emerald background
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Text(
                text = "✨ AI Insight",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF34D399), night = Color(0xFF34D399)), // Emerald400
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = displayInsight.lines().firstOrNull() ?: "", // Just take the first line to keep it brief
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFF6EE7B7), night = Color(0xFF6EE7B7)), // Emerald 300
                    fontSize = 14.sp
                ),
                maxLines = 2
            )
        }
    }
}
