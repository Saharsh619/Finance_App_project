package com.example.financeapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SimpleLineChart(values: List<Double>) {
    if (values.isEmpty()) return
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        val points = values.mapIndexed { index, v ->
            Offset(index * step, size.height - ((v / max).toFloat() * size.height))
        }
        for (i in 0 until points.lastIndex) {
            drawLine(Color(0xFF4CAF50), points[i], points[i + 1], strokeWidth = 4f)
        }
    }
}

@Composable
fun SimpleBarChart(values: List<Double>) {
    if (values.isEmpty()) return
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val width = size.width / (values.size * 1.5f)
        values.forEachIndexed { i, v ->
            val h = (v / max).toFloat() * size.height
            drawRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(i * width * 1.5f, size.height - h),
                size = androidx.compose.ui.geometry.Size(width, h)
            )
        }
    }
}

@Composable
fun SimplePieChart(portions: List<Double>) {
    if (portions.isEmpty()) return
    val total = portions.sum().takeIf { it > 0 } ?: 1.0
    val colors = listOf(Color(0xFFFF9800), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0))
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
