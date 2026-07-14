package com.example.fitness.ui.home

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitness.R
import com.example.fitness.data.PlanRepository
import com.example.fitness.data.PlanType
import com.example.fitness.data.WorkoutPlan
import com.example.fitness.notifications.WaterReminderScheduler
import com.example.fitness.ui.exercise.ExerciseViewModel
import com.example.fitness.ui.plans.PlanRoutes
import com.example.fitness.ui.theme.BgDeep1
import com.example.fitness.ui.theme.BgDeep2
import com.example.fitness.ui.theme.BgLight1
import com.example.fitness.ui.theme.BgLight2
import com.example.fitness.ui.theme.Orange500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ExerciseViewModel = viewModel()
) {
    val context = LocalContext.current
    val planRepo = remember { PlanRepository(context) }
    val plans by planRepo.plans.collectAsState()
    val waterCount by planRepo.waterCount.collectAsState()
    val waterReminder by planRepo.waterReminderOn.collectAsState()
    val waterInterval by planRepo.waterReminderIntervalMinutes.collectAsState()
    val waterStartDelay by planRepo.waterReminderStartDelayMinutes.collectAsState()
    val waterRemaining by planRepo.waterReminderRemaining.collectAsState()
    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val isDark = !MaterialTheme.colorScheme.background.luminanceGreaterThan(0.5f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. 渐变 Hero 头部 + 饮水进度环
            item {
                HeroWaterSection(
                    currentMl = waterCount * planRepo.waterCupMl,
                    goalMl = planRepo.dailyWaterGoalMl,
                    cupMl = planRepo.waterCupMl,
                    onDrink = { planRepo.addWater() },
                    onReset = { planRepo.resetWater() },
                    isDark = isDark
                )
            }

            // 2. 今日推荐
            item {
                SectionHeader(stringResource(R.string.home_today_recommendation))
            }
            val builtIn = plans.filter { it.type != PlanType.CUSTOM }
            items(builtIn) { plan ->
                PlanCard(plan = plan, onClick = { navController.navigate(PlanRoutes.detail(plan.id)) })
            }

            // 3. 健康饮食建议
            item {
                SectionHeader(stringResource(R.string.home_diet_tip))
                DietCard()
            }

            // 4. 我的方案
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.home_my_plans),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = { navController.navigate(PlanRoutes.NEW) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.home_new_plan))
                    }
                }
            }
            val custom = plans.filter { it.type == PlanType.CUSTOM }
            item {
                if (custom.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_no_plans),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            items(custom) { plan ->
                PlanCard(plan = plan, onClick = { navController.navigate(PlanRoutes.detail(plan.id)) })
            }

            // 5. 饮水提醒：系统闹钟 + 起始倒计时 + 提醒次数
            item {
                Spacer(Modifier.height(8.dp))
                WaterReminderCard(
                    planRepo = planRepo,
                    remaining = waterRemaining,
                    startDelay = waterStartDelay,
                    intervalMinutes = waterInterval,
                    count = waterCount
                )
            }
        }
    }
}

@Composable
private fun HeroWaterSection(
    currentMl: Int,
    goalMl: Int,
    cupMl: Int,
    onDrink: () -> Unit,
    onReset: () -> Unit,
    isDark: Boolean
) {
    val gradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(BgDeep2, BgDeep1))
    } else {
        Brush.verticalGradient(colors = listOf(Orange500, BgLight1))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(vertical = 32.dp, horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 中央动画环
            WaterRing(currentMl = currentMl, goalMl = goalMl)
            Spacer(Modifier.height(16.dp))

            // 喝一口 / 重置 按钮组
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = onDrink,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.home_water_drink) + "  " + stringResource(R.string.home_water_cup_size))
                }
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(R.string.home_water_reset))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_water_source),
                fontSize = 11.sp,
                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun DietCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.home_diet_text),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: WorkoutPlan,
    onClick: () -> Unit
) {
    val intensityColor = when (plan.type) {
        PlanType.BUILT_IN_LIGHT -> MaterialTheme.colorScheme.secondary
        PlanType.BUILT_IN_MODERATE -> MaterialTheme.colorScheme.primary
        PlanType.BUILT_IN_INTENSE -> Color(0xFFE63946)
        PlanType.CUSTOM -> MaterialTheme.colorScheme.tertiary
    }
    val isBuiltIn = plan.type != PlanType.CUSTOM

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // 左侧强度色条
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(intensityColor)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBuiltIn) Icons.Default.Star else Icons.Default.Edit,
                        contentDescription = null,
                        tint = intensityColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(intensityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (isBuiltIn) R.string.home_plan_type_built_in
                                else R.string.home_plan_type_custom
                            ),
                            fontSize = 11.sp,
                            color = intensityColor
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_plan_exercises_count, plan.exercises.size),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = intensityColor
                )
            }
        }
    }
}

@Composable
private fun WaterReminderCard(
    planRepo: PlanRepository,
    remaining: Int,
    startDelay: Int,
    intervalMinutes: Int,
    count: Int
) {
    val context = LocalContext.current
    var showConfigDialog by remember { mutableStateOf(false) }

    val reminderOn = remaining > 0

    // 通知权限请求 launcher
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showConfigDialog = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_water_reminder),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (reminderOn) {
                    TextButton(onClick = {
                        // 关闭
                        WaterReminderScheduler.cancel(context)
                        planRepo.disarmReminder()
                    }) {
                        Text("关闭", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    FilledTonalButton(
                        onClick = {
                            // 检查 POST_NOTIFICATIONS 权限 (Android 13+)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (!granted) {
                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@FilledTonalButton
                                }
                            }
                            // 检查 SCHEDULE_EXACT_ALARM (Android 12+)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val am = context.getSystemService(AlarmManager::class.java)
                                if (am?.canScheduleExactAlarms() != true) {
                                    // 引导用户去设置授权
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    runCatching { context.startActivity(intent) }
                                    return@FilledTonalButton
                                }
                            }
                            showConfigDialog = true
                        }
                    ) {
                        Text("开启")
                    }
                }
            }
            if (reminderOn) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "剩余 ${remaining} 次提醒（间隔 ${intervalMinutes} 分钟）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                val nextAt = planRepo.waterReminderStartEpochMillis.value
                if (nextAt > 0) {
                    val millisLeft = nextAt - System.currentTimeMillis()
                    val minsLeft = if (millisLeft > 0) (millisLeft / 60_000).toInt() else 0
                    Text(
                        text = "下次提醒：${minsLeft} 分钟后",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "点击开启 → 输入起始倒计时 + 提醒次数 → 系统闹钟按时通知",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showConfigDialog) {
        WaterReminderConfigDialog(
            initialStartDelay = startDelay,
            initialInterval = intervalMinutes,
            onDismiss = { showConfigDialog = false },
            onConfirm = { startDelay, interval, count ->
                planRepo.armReminder(startDelay, interval, count)
                val firstTrigger = System.currentTimeMillis() + startDelay * 60_000L
                planRepo.setWaterReminderStartEpochMillis(firstTrigger)
                WaterReminderScheduler.scheduleNext(context, firstTrigger)
                showConfigDialog = false
            }
        )
    }
}

/**
 * 配置对话框：起始倒计时（多少分钟后第一次）+ 每次间隔 + 提醒次数
 * 至少要给一个有效配置，否则不开启
 */
@Composable
private fun WaterReminderConfigDialog(
    initialStartDelay: Int,
    initialInterval: Int,
    onDismiss: () -> Unit,
    onConfirm: (startDelay: Int, interval: Int, count: Int) -> Unit
) {
    var startDelay by remember { mutableStateOf(initialStartDelay.toString()) }
    var interval by remember { mutableStateOf(initialInterval.toString()) }
    var count by remember { mutableStateOf(if (initialStartDelay > 0) "5" else "5") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置饮水提醒") },
        text = {
            Column {
                Text(
                    text = "告诉我多久后开始第一次提醒，每次间隔多久，共响几次",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = startDelay,
                    onValueChange = { startDelay = it.filter { c -> c.isDigit() } },
                    label = { Text("起始倒计时（分钟后）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter { c -> c.isDigit() } },
                    label = { Text("间隔（分钟）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it.filter { c -> c.isDigit() } },
                    label = { Text("提醒次数") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (errorText != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sd = startDelay.toIntOrNull()
                val iv = interval.toIntOrNull()
                val c = count.toIntOrNull()
                if (sd == null || sd < 1) {
                    errorText = "起始倒计时至少 1 分钟"
                    return@TextButton
                }
                if (iv == null || iv < 1) {
                    errorText = "间隔至少 1 分钟"
                    return@TextButton
                }
                if (c == null || c < 1) {
                    errorText = "至少提醒 1 次"
                    return@TextButton
                }
                onConfirm(sd, iv, c)
            }) {
                Text("开启提醒")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// 颜色亮度工具
private fun Color.luminanceGreaterThan(threshold: Float): Boolean {
    return (0.299f * red + 0.587f * green + 0.114f * blue) > threshold
}
