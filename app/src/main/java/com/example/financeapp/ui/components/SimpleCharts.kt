package com.example.financeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.min

private fun DrawScope.drawAxes(leftPad: Float, bottomPad: Float) {
    val axis = Color(0xFF7A7A7A)
    drawLine(axis, Offset(leftPad, 8f), Offset(leftPad, size.height - bottomPad), strokeWidth = 2f)
    drawLine(axis, Offset(leftPad, size.height - bottomPad), Offset(size.width - 8f, size.height - bottomPad), strokeWidth = 2f)
}

@Composable
fun SimpleLineChart(values: List<Double>, xLabels: List<String>, title: String) {
    if (values.isEmpty()) {
        Text("$title: no data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val safeValues = values.map { it.coerceAtLeast(0.0) }
    val max = safeValues.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val leftPad = 56f
    val bottomPad = 34f

    Column {
        Text(title, fontWeight = FontWeight.SemiBold)
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 4.dp)) {
            drawAxes(leftPad, bottomPad)
            val plotW = size.width - leftPad - 16f
            val plotH = size.height - bottomPad - 14f
            val stepX = plotW / (safeValues.size - 1).coerceAtLeast(1)

            val points = safeValues.mapIndexed { idx, v ->
                val y = 12f + plotH - ((v / max).toFloat() * plotH)
                Offset(leftPad + idx * stepX, y)
            }

            points.zipWithNext { a, b -> drawLine(Color(0xFF1B8F4A), a, b, strokeWidth = 4f) }
            points.forEach { drawCircle(Color(0xFF156B38), radius = 4.5f, center = it) }

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 20f
                isAntiAlias = true
            }

            for (i in 0..4) {
                val y = 12f + plotH - (plotH * i / 4f)
                val tickValue = max * i / 4
                drawLine(Color(0xFFE0E0E0), Offset(leftPad, y), Offset(size.width - 8f, y), strokeWidth = 1f)
                drawContext.canvas.nativeCanvas.drawText("${"%.0f".format(tickValue)}", 4f, y + 6f, paint)
            }

            points.forEachIndexed { i, point ->
                val label = xLabels.getOrElse(i) { (i + 1).toString() }
                drawContext.canvas.nativeCanvas.drawText(label, point.x - 10f, size.height - 6f, paint)
            }
        }
    }
}

@Composable
fun SimpleBarChart(values: List<Double>, xLabels: List<String>, title: String) {
    if (values.isEmpty()) {
        Text("$title: no data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val safeValues = values.map { it.coerceAtLeast(0.0) }
    val max = safeValues.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val leftPad = 56f
    val bottomPad = 34f

    Column {
        Text(title, fontWeight = FontWeight.SemiBold)
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 4.dp)) {
            drawAxes(leftPad, bottomPad)
            val plotW = size.width - leftPad - 16f
            val plotH = size.height - bottomPad - 14f
            val slot = plotW / safeValues.size.coerceAtLeast(1)
            val barW = min(48f, slot * 0.65f)

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 20f
                isAntiAlias = true
            }

            safeValues.forEachIndexed { i, value ->
                val h = ((value / max) * plotH).toFloat()
                val x = leftPad + i * slot + (slot - barW) / 2
                drawRect(Color(0xFF2C7BE5), Offset(x, 12f + plotH - h), Size(barW, h), style = Fill)
                drawContext.canvas.nativeCanvas.drawText(xLabels.getOrElse(i) { "${i + 1}" }, x, size.height - 6f, paint)
            }

            drawContext.canvas.nativeCanvas.drawText("0", 4f, size.height - bottomPad + 8f, paint)
            drawContext.canvas.nativeCanvas.drawText("${"%.0f".format(max)}", 4f, 20f, paint)
        }
    }
}

@Composable
fun SimplePieChart(entries: List<Pair<String, Double>>, title: String) {
    val filtered = entries.filter { it.second > 0 }
    if (filtered.isEmpty()) {
        Text("$title: no data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val total = filtered.sumOf { it.second }
    val colors = listOf(Color(0xFFFF9800), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFF44336))

    Column {
        Text(title, fontWeight = FontWeight.SemiBold)
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            var start = -90f
            filtered.forEachIndexed { index, (_, amount) ->
                val sweep = ((amount / total) * 360.0).toFloat()
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(40f, 10f),
                    size = Size(size.minDimension - 80f, size.minDimension - 20f)
                )
                start += sweep
            }
            drawCircle(
                color = Color.White,
                radius = (size.minDimension - 80f) * 0.22f,
                center = Offset(size.width / 2f, (size.minDimension - 20f) / 2f + 10f),
                style = Fill
            )
        }

        filtered.forEachIndexed { index, (label, amount) ->
            val pct = (amount / total) * 100
            Text("${index + 1}. $label: ${"%.1f".format(pct)}%")
        }
    }
}
