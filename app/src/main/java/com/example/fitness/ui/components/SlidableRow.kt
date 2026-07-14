package com.example.fitness.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 自定义"左滑露出一部分 → 出现删除按钮 → 手动点删除"组件。
 *
 * 为什么不用 Material 3 的 SwipeToDismissBox？
 * - 用户反馈：删除一行后下一行会卡在红色状态拖不回来 → 是 SwipeToDismissBox 的状态机 bug
 * - SwipeToDismissBox 在 dismiss 完成时立即从 LazyList 移除 item，下一行 index 重排时
 *   旧 dismissState 复用到新 item，触发级联误删
 *
 * 这个组件的行为：
 * - 左滑到 > 阈值（如 80dp）→ 露出一部分（最远 100dp）→ 露出删除按钮
 * - 用户必须手动点删除按钮才删除，避免误删
 * - 点其他地方或滑回会自动关闭
 */
@Composable
fun SlidableRow(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val maxOffset = 100f  // dp 转 px 在下面计算
    var deleteWidthPx by remember { mutableStateOf(80f) }
    var rowWidthPx by remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                rowWidthPx = size.width.toFloat()
            }
    ) {
        // 背景：删除按钮（始终存在，点击区域固定）
        // 用 Box.align 让按钮靠右
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(IntrinsicSize.Min)
        ) {
            Surface(
                onClick = {
                    onDelete()
                    scope.launch {
                        offsetX.animateTo(0f, animationSpec = tween(200))
                    }
                },
                color = Color(0xFFE53935),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(80.dp)
                    .height(56.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.White
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "删除",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 前景：可滑动的内容
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 滑动结束：如果超出阈值则停在 -maxOffset，否则回弹
                            scope.launch {
                                if (offsetX.value < -40f) {
                                    offsetX.animateTo(-maxOffset, spring())
                                } else {
                                    offsetX.animateTo(0f, spring())
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, spring())
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newOffset = (offsetX.value + dragAmount)
                                    .coerceIn(-maxOffset, 0f)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
                .clickable {
                    // 单击：如果已展开就收回，否则照常
                    if (offsetX.value != 0f) {
                        scope.launch { offsetX.animateTo(0f, spring()) }
                    }
                }
        ) {
            content()
        }
    }
}