package com.example.fitness.ui.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitness.ui.components.SlidableRow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fitness.R
import com.example.fitness.data.PlanExercise
import com.example.fitness.data.PlanType
import com.example.fitness.data.WorkoutPlan
import com.example.fitness.data.WorkoutPresets
import com.example.fitness.ui.exercise.ExerciseViewModel

/** 暴露给 NavHost 的路由常量。 */
object PlanRoutes {
    const val DETAIL = "plan/{planId}"
    const val DETAIL_ARG = "planId"
    const val NEW = "plan/new"
    const val EDIT = "plan/{planId}/edit"
    const val EDIT_ARG = "planId"
    fun detail(planId: String) = "plan/$planId"
    fun edit(planId: String) = "plan/$planId/edit"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: String,
    navController: NavHostController,
    exerciseViewModel: ExerciseViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val vm = remember { ExerciseViewModel.get(application) }
    // 用 PlanRepository.getPlan 拿全部方案（内置 + 自建）
    val planRepo = remember { com.example.fitness.data.PlanRepository.get(application) }
    val plans by planRepo.plans.collectAsState()
    val plan = plans.find { it.id == planId }

    if (plan == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("方案不存在") }
        return
    }

    val allExercises by exerciseViewModel.exercises.collectAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (plan.type == PlanType.CUSTOM) {
                        IconButton(onClick = {
                            navController.navigate(PlanRoutes.edit(plan.id))
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (plan.description.isNotBlank()) {
                item {
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_plan_exercises_count, plan.exercises.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (plan.type == PlanType.CUSTOM) {
                        FilledTonalButton(
                            onClick = { navController.navigate(PlanRoutes.edit(plan.id)) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("添加动作")
                        }
                    }
                }
            }
            itemsIndexed(plan.exercises) { index, pe ->
                val exercise = allExercises.find { it.id == pe.exerciseId }
                val spec = remember(exercise) { exercise?.let { WorkoutPresets.defaultSpec(it) } }
                // 用 SlidableRow：左滑露出一部分 → 出现删除按钮 → 用户手动点删除按钮
                // 内置方案不显示删除功能（避免误删演示数据）
                if (plan.type == PlanType.CUSTOM) {
                    SlidableRow(
                        onDelete = { vm.removeExerciseFromPlan(plan.id, index) }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    exercise?.let { navController.navigate("exercise/${it.id}") }
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = exercise?.let { exerciseViewModel.displayNameOf(it) } ?: pe.exerciseId,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SpecChip("${pe.sets} 组")
                                        SpecChip("${pe.reps} 次")
                                        SpecChip("休 ${pe.restSeconds}s")
                                        if (spec != null) {
                                            SpecChip(String.format("~%.0f kcal/min", spec.caloriesPerMinute))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 内置方案：不允许删除
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exercise?.let { navController.navigate("exercise/${it.id}") }
                            },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.tertiary)
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = exercise?.let { exerciseViewModel.displayNameOf(it) } ?: pe.exerciseId,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SpecChip("${pe.sets} 组")
                                    SpecChip("${pe.reps} 次")
                                    SpecChip("休 ${pe.restSeconds}s")
                                    if (spec != null) {
                                        SpecChip(String.format("~%.0f kcal/min", spec.caloriesPerMinute))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除方案") },
            text = { Text("确定要删除方案\"${plan.name}\"？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deletePlan(plan.id)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SpecChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * 方案详情页底部的设计参考来源卡片。
 * 内置方案：标注 WHO/ACSM/IOM 三个权威机构。
 * 自建方案：标注方案名称（用户自己负责）。
 */
@Composable
private fun DesignSourceCard(planType: PlanType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.plan_design_source),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            val sources = when (planType) {
                PlanType.BUILT_IN_LIGHT -> listOf(
                    stringResource(R.string.plan_source_who),
                    stringResource(R.string.plan_source_acs)
                )
                PlanType.BUILT_IN_MODERATE -> listOf(
                    stringResource(R.string.plan_source_acs),
                    stringResource(R.string.plan_source_who)
                )
                PlanType.BUILT_IN_INTENSE -> listOf(
                    stringResource(R.string.plan_source_acs),
                    stringResource(R.string.plan_source_iom)
                )
                PlanType.CUSTOM -> listOf("用户自建，无权威来源背书")
            }
            sources.forEach { source ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = source,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlanScreen(
    navController: NavHostController,
    exerciseViewModel: ExerciseViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val vm = remember { ExerciseViewModel.get(application) }

    var planName by remember { mutableStateOf("") }
    var planDesc by remember { mutableStateOf("") }
    val selected = remember { mutableStateListOf<PlanExercise>() }
    var addExerciseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_new_plan), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (planName.isNotBlank()) {
                                val id = vm.createPlan(planName, planDesc)
                                selected.forEach { pe -> vm.addExerciseToPlan(id, pe) }
                                navController.popBackStack()
                            }
                        }
                    ) { Text("保存") }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("方案名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = planDesc,
                    onValueChange = { planDesc = it },
                    label = { Text("方案说明（选填）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "已添加动作 (${selected.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = { addExerciseDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("搜索添加")
                    }
                }
            }
            items(selected) { pe ->
                val ex = exerciseViewModel.exercises.collectAsState().value.find { it.id == pe.exerciseId }
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ex?.let { exerciseViewModel.displayNameOf(it) } ?: pe.exerciseId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${pe.sets} 组 × ${pe.reps} 次 · 休 ${pe.restSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { selected.remove(pe) }) {
                            Icon(Icons.Default.Close, contentDescription = "移除")
                        }
                    }
                }
            }
        }
    }

    if (addExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { addExerciseDialog = false },
            exerciseViewModel = exerciseViewModel,
            onAdd = { exerciseId, sets, reps, rest ->
                selected.add(PlanExercise(exerciseId, sets, reps, rest))
                addExerciseDialog = false
            }
        )
    }
}

/**
 * 编辑模式：复用 NewPlanScreen 但加载已存在的 plan
 * 通过传入 planId 判断是新建还是编辑
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    planId: String,
    navController: NavHostController,
    exerciseViewModel: ExerciseViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val vm = remember { ExerciseViewModel.get(application) }
    val plan = vm.allCustomPlans().find { it.id == planId }

    if (plan == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("方案不存在") }
        return
    }

    var planName by remember { mutableStateOf(plan.name) }
    var planDesc by remember { mutableStateOf(plan.description) }
    val selected = remember { mutableStateListOf<PlanExercise>().apply { addAll(plan.exercises) } }
    var addExerciseDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑：${plan.name}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (planName.isNotBlank()) {
                            val updated = plan.copy(name = planName, description = planDesc, exercises = selected.toList())
                            vm.updatePlan(updated)
                            navController.popBackStack()
                        }
                    }) { Text("保存") }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("方案名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = planDesc,
                    onValueChange = { planDesc = it },
                    label = { Text("方案说明") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "动作 (${selected.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = { addExerciseDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("添加")
                    }
                }
            }
            itemsIndexed(selected) { index, pe ->
                val ex = exerciseViewModel.exercises.collectAsState().value.find { it.id == pe.exerciseId }
                // SlidableRow：左滑露出一部分 → 手动点删除 → 避免 SwipeToDismissBox 状态机 bug
                SlidableRow(
                    onDelete = {
                        if (index in selected.indices) selected.removeAt(index)
                    }
                ) {
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier
                                .clickable { editingIndex = index }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ex?.let { exerciseViewModel.displayNameOf(it) } ?: pe.exerciseId,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${pe.sets} 组 × ${pe.reps} 次 · 休 ${pe.restSeconds}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { if (index in selected.indices) selected.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "移除")
                            }
                        }
                    }
                }
            }
        }
    }

    if (addExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { addExerciseDialog = false },
            exerciseViewModel = exerciseViewModel,
            onAdd = { exerciseId, sets, reps, rest ->
                selected.add(PlanExercise(exerciseId, sets, reps, rest))
                addExerciseDialog = false
            }
        )
    }

    if (editingIndex != null) {
        val idx = editingIndex!!
        if (idx in selected.indices) {
            val pe = selected[idx]
            EditSetsRepsDialog(
                initial = pe,
                onDismiss = { editingIndex = null },
                onConfirm = { sets, reps, rest ->
                    selected[idx] = pe.copy(sets = sets, reps = reps, restSeconds = rest)
                    editingIndex = null
                }
            )
        } else {
            editingIndex = null
        }
    }
}

@Composable
private fun AddExerciseDialog(
    onDismiss: () -> Unit,
    exerciseViewModel: ExerciseViewModel,
    onAdd: (exerciseId: String, sets: Int, reps: Int, rest: Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var rest by remember { mutableStateOf("60") }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val results = remember(query) { exerciseViewModel.searchForSelection(query) }
    val selectedEx = results.find { it.id == selectedId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加动作到方案") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("搜索动作名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(results) { ex ->
                        val isSelected = ex.id == selectedId
                        Surface(
                            onClick = { selectedId = ex.id },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exerciseViewModel.displayNameOf(ex), fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${ex.category} · ${ex.equipment}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    if (results.isEmpty()) {
                        item {
                            Text(
                                text = if (query.isBlank()) "输入动作名搜索" else "未找到动作",
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (selectedEx != null) {
                    val spec = remember(selectedEx) { WorkoutPresets.defaultSpec(selectedEx) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sets,
                            onValueChange = { sets = it.filter { c -> c.isDigit() } },
                            label = { Text("组数") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it.filter { c -> c.isDigit() } },
                            label = { Text("每组次数") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rest,
                            onValueChange = { rest = it.filter { c -> c.isDigit() } },
                            label = { Text("休息(秒)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "默认推荐：${spec.sets} 组 × ${spec.reps} 次 · 休 ${spec.restSeconds}s",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = selectedId
                    if (id != null) {
                        onAdd(id, sets.toIntOrNull() ?: 3, reps.toIntOrNull() ?: 10, rest.toIntOrNull() ?: 60)
                    }
                }
            ) { Text("加入") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun EditSetsRepsDialog(
    initial: PlanExercise,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int) -> Unit
) {
    var sets by remember { mutableStateOf(initial.sets.toString()) }
    var reps by remember { mutableStateOf(initial.reps.toString()) }
    var rest by remember { mutableStateOf(initial.restSeconds.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑组数/次数") },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it.filter { c -> c.isDigit() } },
                        label = { Text("组数") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it.filter { c -> c.isDigit() } },
                        label = { Text("每组次数") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rest,
                        onValueChange = { rest = it.filter { c -> c.isDigit() } },
                        label = { Text("休息(秒)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(sets.toIntOrNull() ?: 3, reps.toIntOrNull() ?: 10, rest.toIntOrNull() ?: 60) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
