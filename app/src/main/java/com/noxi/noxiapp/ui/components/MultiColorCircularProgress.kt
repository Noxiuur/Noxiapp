package com.noxi.noxiapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noxi.noxiapp.data.Habit

/**
 * Çok renkli dairesel ilerleme göstergesi
 * Her alışkanlık yüzdesine göre kendi renginde segment gösterir
 */
@Composable
fun MultiColorCircularProgress(
    habits: List<Habit>,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    strokeWidth: Dp = 20.dp,
    backgroundColor: Color = Color(0xFF1E1E1E)
) {
    // Toplam tamamlanma yüzdesi
    val totalPercentage = if (habits.isEmpty()) 0f else {
        habits.sumOf { it.completionPercentage.toDouble() }.toFloat() / habits.size
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val radius = (canvasSize / 2) - strokeWidth.toPx() / 2
            val topLeft = Offset(
                x = (this.size.width - canvasSize) / 2 + strokeWidth.toPx() / 2,
                y = (this.size.height - canvasSize) / 2 + strokeWidth.toPx() / 2
            )
            val arcSize = Size(radius * 2, radius * 2)
            
            // Arka plan dairesi (gri)
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            
            // İlerleme çubuğu - Kırmızı renk
            if (habits.isNotEmpty() && totalPercentage > 0) {
                val sweepAngle = (totalPercentage / 100f) * 360f
                
                drawArc(
                    color = Color(0xFFEF4444), // Kırmızı renk
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        
    // Ortadaki yüzde metni
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${totalPercentage.toInt()}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = com.noxi.noxiapp.ui.theme.LocalStrings.current.completed,
                fontSize = 14.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
