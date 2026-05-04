package com.example.financeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private fun DrawScope.drawAxes(leftPad: Float, bottomPad: Float, axisColor: Color = Color.Gray) {
    drawLine(axisColor, Offset(leftPad, 0f), Offset(leftPad, size.height - bottomPad), strokeWidth = 2f)
    drawLine(axisColor, Offset(leftPad, size.height - bottomPad), Offset(size.width, size.height - bottomPad), strokeWidth = 2f)
}

@Composable
fun SimpleLineChart(values: List<Double>, xLabels: List<String>) {
    if (values.isEmpty()) return
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val min = values.minOrNull() ?: 0.0
    val leftPad = 70f
    val bottomPad = 45f

    Column {
        Text("Line (X: Entry, Y: Amount)", fontWeight = FontWeight.SemiBold)
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 4.dp)) {
            drawAxes(leftPad, bottomPad)
            val plotWidth = size.width - leftPad - 10f
            val plotHeight = size.height - bottomPad - 10f
            val stepX = plotWidth / (values.size - 1).coerceAtLeast(1)
            val points = values.mapIndexed { index, v ->
                val range = (max - min).takeIf { it > 0 } ?: 1.0
                val normalized = ((v - min) / range).toFloat()
                Offset(leftPad + index * stepX, 10f + plotHeight - normalized * plotHeight)
            }

            for (i in 0 until points.lastIndex) {
                drawLine(Color(0xFF4CAF50), points[i], points[i + 1], strokeWidth = 4f)
            }

            val yTicks = 4
            (0..yTicks).forEach { i ->
                val fraction = i / yTicks.toFloat()
                val y = 10f + plotHeight - (plotHeight * fraction)
                val value = min + (max - min) * fraction
                drawLine(Color.LightGray, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1f)
                drawContext.canvas.nativeCanvas.drawText("${"%.0f".format(value)}", 6f, y + 8f, android.graphics.Paint().apply { textSize = 22f; color = android.graphics.Color.DKGRAY })
            }

            points.forEachIndexed { i, p ->
                drawCircle(Color(0xFF2E7D32), radius = 5f, center = p)
                val label = xLabels.getOrNull(i) ?: (i + 1).toString()
                drawContext.canvas.nativeCanvas.drawText(label, p.x - 12f, size.height - 8f, android.graphics.Paint().apply { textSize = 20f; color = android.graphics.Color.DKGRAY })
            }
        }
    }
}

@Composable
fun SimpleBarChart(values: List<Double>, xLabels: List<String>) {
    if (values.isEmpty()) return
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val leftPad = 70f
    val bottomPad = 45f

    Column {
        Text("Bar (X: Month, Y: Total)", fontWeight = FontWeight.SemiBold)
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 4.dp)) {
            drawAxes(leftPad, bottomPad)
            val plotHeight = size.height - bottomPad - 10f
            val slot = (size.width - leftPad - 10f) / values.size.coerceAtLeast(1)
            val barWidth = slot * 0.6f

            values.forEachIndexed { i, v ->
                val h = (v / max).toFloat() * plotHeight
                val x = leftPad + i * slot + (slot - barWidth) / 2
                drawRect(Color(0xFF2196F3), topLeft = Offset(x, 10f + plotHeight - h), size = Size(barWidth, h))
                val label = xLabels.getOrNull(i) ?: (i + 1).toString()
                drawContext.canvas.nativeCanvas.drawText(label, x, size.height - 8f, android.graphics.Paint().apply { textSize = 20f; color = android.graphics.Color.DKGRAY })
            }
            drawContext.canvas.nativeCanvas.drawText("0", 20f, size.height - bottomPad + 8f, android.graphics.Paint().apply { textSize = 22f; color = android.graphics.Color.DKGRAY })
            drawContext.canvas.nativeCanvas.drawText("${"%.0f".format(max)}", 6f, 20f, android.graphics.Paint().apply { textSize = 22f; color = android.graphics.Color.DKGRAY })
        }
    }
}

@Composable
fun SimplePieChart(portions: List<Double>) {
    if (portions.isEmpty()) return
    val total = portions.sum().takeIf { it > 0 } ?: 1.0
    val colors = listOf(Color(0xFFFF9800), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0))
    Column {
        Text("Pie (Category Share %)", fontWeight = FontWeight.SemiBold)
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            var start = -90f
            portions.forEachIndexed { index, value ->
                val sweep = ((value / total) * 360f).toFloat()
                drawArc(colors[index % colors.size], start, sweep, useCenter = true)
                start += sweep
            }
            drawCircle(Color.White, radius = size.minDimension * 0.2f, style = Stroke(width = 1f))
        }
    }
}
