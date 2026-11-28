package com.legend.mc

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SkinEditorScreen() {

    val pixels = remember {
        mutableStateListOf<Color>().apply {
            repeat(64 * 64) { add(Color.Transparent) }
        }
    }

    val undoHistory = remember { mutableStateListOf<List<Color>>() }
    val redoHistory = remember { mutableStateListOf<List<Color>>() }

    val palette = listOf(
        Color.Black,
        Color.White,
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFF795548),
        Color(0xFF00BCD4),
        Color(0xFFFF80AB),
        Color(0xFF607D8B)
    )

    var selectedColor by remember { mutableStateOf(Color.Black) }
    var eraser by remember { mutableStateOf(false) }
    var mirror by remember { mutableStateOf(false) }

    fun saveUndo() {
        if (undoHistory.size >= 50) {
            undoHistory.removeAt(0)
        }
        undoHistory.add(pixels.toList())
        redoHistory.clear()
    }

    fun undo() {
        if (undoHistory.isNotEmpty()) {
            if (redoHistory.size >= 50) {
                redoHistory.removeAt(0)
            }
            redoHistory.add(pixels.toList())
            val previous = undoHistory.removeAt(undoHistory.lastIndex)
            for (i in pixels.indices) {
                pixels[i] = previous[i]
            }
        }
    }

    fun redo() {
        if (redoHistory.isNotEmpty()) {
            if (undoHistory.size >= 50) {
                undoHistory.removeAt(0)
            }
            undoHistory.add(pixels.toList())
            val next = redoHistory.removeAt(redoHistory.lastIndex)
            for (i in pixels.indices) {
                pixels[i] = next[i]
            }
        }
    }

    fun paintAt(position: Offset, width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return

        val column = (position.x / width * 64).toInt().coerceIn(0, 63)
        val row = (position.y / height * 64).toInt().coerceIn(0, 63)
        val color = if (eraser) Color.Transparent else selectedColor

        pixels[row * 64 + column] = color

        if (mirror) {
            val oppositeColumn = 63 - column
            pixels[row * 64 + oppositeColumn] = color
        }
    }

    fun correctSymmetry() {
        saveUndo()
        for (row in 0 until 64) {
            for (column in 0 until 32) {
                val left = pixels[row * 64 + column]
                pixels[row * 64 + (63 - column)] = left
            }
        }
    }

    fun clearAll() {
        saveUndo()
        for (i in pixels.indices) {
            pixels[i] = Color.Transparent
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "🎨 محرر السكنات",
            color = Color(0xFFFFC857),
            fontSize = 23.sp
        )

        Text(
            "لوحة رسم 64 × 64 بكسل",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFF202A25))
                .pointerInput(eraser, selectedColor, mirror) {
                    detectTapGestures { position ->
                        saveUndo()
                        paintAt(
                            position,
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                    }
                }
                .pointerInput(eraser, selectedColor, mirror) {
                    var touched = false
                    detectDragGestures(
                        onDragStart = { position ->
                            touched = true
                            saveUndo()
                            paintAt(
                                position,
                                size.width.toFloat(),
                                size.height.toFloat()
                            )
                        },
                        onDrag = { change, _ ->
                            paintAt(
                                change.position,
                                size.width.toFloat(),
                                size.height.toFloat()
                            )
                        },
                        onDragEnd = {
                            touched = false
                        },
                        onDragCancel = {
                            touched = false
                        }
                    )
                }
        ) {
            val cellWidth = size.width / 64f
            val cellHeight = size.height / 64f

            for (row in 0 until 64) {
                for (column in 0 until 64) {
                    val index = row * 64 + column
                    val color = pixels[index]

                    if (color.alpha > 0f) {
                        drawRect(
                            color = color,
                            topLeft = Offset(
                                column * cellWidth,
                                row * cellHeight
                            ),
                            size = Size(cellWidth, cellHeight)
                        )
                    } else if ((row + column) % 2 == 0) {
                        drawRect(
                            color = Color(0xFF29362F),
                            topLeft = Offset(
                                column * cellWidth,
                                row * cellHeight
                            ),
                            size = Size(cellWidth, cellHeight)
                        )
                    }
                }
            }

            for (i in 0..64) {
                val x = i * cellWidth
                val y = i * cellHeight

                drawLine(
                    color = Color(0x553F5147),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.5f
                )

                drawLine(
                    color = Color(0x553F5147),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.5f
                )
            }
        }

        Text("لوحة الألوان", color = Color.White, fontSize = 16.sp)

        for (row in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (column in 0 until 6) {
                    val color = palette[row * 6 + column]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .background(
                                color,
                                RoundedCornerShape(7.dp)
                            )
                            .clickable {
                                selectedColor = color
                                eraser = false
                            }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { undo() },
                modifier = Modifier.weight(1f),
                enabled = undoHistory.isNotEmpty()
            ) {
                Text("↶ تراجع")
            }

            OutlinedButton(
                onClick = { redo() },
                modifier = Modifier.weight(1f),
                enabled = redoHistory.isNotEmpty()
            ) {
                Text("↷ إعادة")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { eraser = !eraser },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (eraser) "الممحاة مفعّلة" else "الممحاة")
            }

            Button(
                onClick = { mirror = !mirror },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (mirror) "التماثل مفعّل" else "الرسم المتماثل")
            }
        }

        Button(
            onClick = { correctSymmetry() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تصحيح التماثل تلقائيًا")
        }

        OutlinedButton(
            onClick = { clearAll() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("مسح اللوحة بالكامل")
        }

        Text(
            "التراجع والإعادة يحفظان آخر 50 عملية. " +
                "تصحيح التماثل ينسخ النصف الأيسر إلى النصف الأيمن.",
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}
