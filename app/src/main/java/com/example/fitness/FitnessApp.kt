package com.example.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.fitness.data.Exercise
import com.example.fitness.data.InstructionSteps
import com.example.fitness.data.Instructions
import com.example.fitness.i18n.LocaleManager
import com.example.fitness.ui.browse.BrowseScreen
import com.example.fitness.ui.exercise.ExerciseViewModel
import com.example.fitness.ui.favorites.FavoritesScreen
import com.example.fitness.ui.home.HomeScreen
import com.example.fitness.ui.search.SearchScreen
import com.example.fitness.ui.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    // 用单例，避免每个 backStackEntry 各自创建 VM 导致 nameZh 重复读取或竞争
    val application = context.applicationContext as android.app.Application
    val exerciseViewModel: ExerciseViewModel = remember { ExerciseViewModel.get(application) }

    LaunchedEffect(Unit) {
        exerciseViewModel.loadExercises()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = navController.currentDestination?.route == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.GridOn, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_browse)) },
                    selected = navController.currentDestination?.route?.startsWith("browse") == true,
                    onClick = {
                        navController.navigate("browse") { launchSingleTop = true }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_search)) },
                    selected = navController.currentDestination?.route == "search",
                    onClick = {
                        navController.navigate("search") { launchSingleTop = true }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_favorites)) },
                    selected = navController.currentDestination?.route == "favorites",
                    onClick = {
                        navController.navigate("favorites") { launchSingleTop = true }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen(navController, exerciseViewModel) }
            composable("browse") { BrowseScreen(navController, exerciseViewModel) }
            composable("browse/category/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                CategoryScreen(category, navController, exerciseViewModel)
            }
            composable("browse/equipment/{equipment}") { backStackEntry ->
                val equipment = backStackEntry.arguments?.getString("equipment") ?: ""
                EquipmentScreen(equipment, navController, exerciseViewModel)
            }
            composable("search") { SearchScreen(navController, exerciseViewModel) }
            composable("favorites") { FavoritesScreen(navController) }
            composable("settings") {
                SettingsScreen(
                    navController = navController,
                    exerciseViewModel = exerciseViewModel
                )
            }
            composable("exercise/{exerciseId}") { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId")
                ExerciseDetailScreen(
                    exerciseId = exerciseId ?: "",
                    navController = navController,
                    exerciseViewModel = exerciseViewModel
                )
            }
            composable(com.example.fitness.ui.plans.PlanRoutes.DETAIL) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString(com.example.fitness.ui.plans.PlanRoutes.DETAIL_ARG) ?: ""
                com.example.fitness.ui.plans.PlanDetailScreen(
                    planId = planId,
                    navController = navController,
                    exerciseViewModel = exerciseViewModel
                )
            }
            composable(com.example.fitness.ui.plans.PlanRoutes.NEW) {
                com.example.fitness.ui.plans.NewPlanScreen(
                    navController = navController,
                    exerciseViewModel = exerciseViewModel
                )
            }
            composable(com.example.fitness.ui.plans.PlanRoutes.EDIT) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString(com.example.fitness.ui.plans.PlanRoutes.EDIT_ARG) ?: ""
                com.example.fitness.ui.plans.EditPlanScreen(
                    planId = planId,
                    navController = navController,
                    exerciseViewModel = exerciseViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    navController: NavHostController,
    exerciseViewModel: ExerciseViewModel
) {
    val context = LocalContext.current
    val repository = remember { com.example.fitness.data.ExerciseRepository(context) }
    val exercises by exerciseViewModel.exercises.collectAsState(initial = emptyList())
    val exercise = exercises.find { it.id == exerciseId }

    if (exercise == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // "加入方案"对话框控制
    var showAddToPlanDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 锁中文时从中文 instructions 字段取
    val langCode = LocaleManager.currentLanguageCode(context)
    val instructionText = pickLocalized(exercise.instructions, langCode)
    val steps = pickLocalizedSteps(exercise.instructionSteps, langCode)
    val langLabel = langCode.uppercase()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = exerciseViewModel.displayNameOf(exercise), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddToPlanDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("加入方案")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GIF Display（本地缓存优先，没有就触发后台下载，同时从 CDN 加载）
            var gifModel: Any by remember(exercise.mediaId) {
                mutableStateOf<Any>(
                    com.example.fitness.data.MediaCache.localPath(context, exercise.mediaId)
                        ?: "https://static.exercisedb.dev/media/${exercise.mediaId}.gif"
                )
            }
            LaunchedEffect(exercise.mediaId) {
                // 如果本地没有，触发后台下载到本地
                if (!com.example.fitness.data.MediaCache.isCached(context, exercise.mediaId)) {
                    val file = com.example.fitness.data.MediaCache.downloadAndCache(context, exercise.mediaId)
                    if (file != null) {
                        gifModel = file
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = gifModel,
                        contentDescription = exerciseViewModel.displayNameOf(exercise),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            DetailSection(stringResource(R.string.detail_target), com.example.fitness.data.DataTranslations.target(exercise.target))
            DetailSection(stringResource(R.string.detail_equipment), com.example.fitness.data.DataTranslations.equipment(exercise.equipment))
            DetailSection(stringResource(R.string.detail_category), com.example.fitness.data.DataTranslations.category(exercise.category))
            DetailSection(stringResource(R.string.detail_muscle_group), com.example.fitness.data.DataTranslations.muscleGroup(exercise.muscleGroup))

            if (exercise.secondaryMuscles.isNotEmpty()) {
                DetailSection(
                    stringResource(R.string.detail_secondary_muscles),
                    exercise.secondaryMuscles.joinToString(", ") { com.example.fitness.data.DataTranslations.muscleGroup(it) }
                )
            }

            // 建议健身规范
            val spec = remember(exercise) { com.example.fitness.data.WorkoutPresets.defaultSpec(exercise) }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.detail_spec_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    SpecRow(stringResource(R.string.detail_spec_sets), "${spec.sets}")
                    SpecRow(stringResource(R.string.detail_spec_reps), "${spec.reps} ${stringResource(R.string.detail_spec_reps_unit)}")
                    SpecRow(stringResource(R.string.detail_spec_rest), "${spec.restSeconds} ${stringResource(R.string.detail_spec_rest_unit)}")
                    SpecRow(
                        stringResource(R.string.detail_spec_calories),
                        String.format("%.1f %s", spec.caloriesPerMinute, stringResource(R.string.detail_spec_calories_unit))
                    )
                }
            }

            DetailSection(stringResource(R.string.detail_instructions, langLabel), instructionText)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.detail_steps, langLabel),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (steps.isEmpty()) {
                        Text("—", fontSize = 16.sp)
                    } else {
                        steps.forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. $step",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddToPlanDialog) {
        AddToPlanDialog(
            exercise = exercise,
            exerciseViewModel = exerciseViewModel,
            onDismiss = { showAddToPlanDialog = false },
            onAdded = { planName ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.snackbar_added_to_plan, planName)
                    )
                }
            }
        )
    }
}

/**
 * "加入方案"对话框：根据用户有几个自建方案决定交互。
 * - 0 个方案：提示用户去新建
 * - 1 个方案：直接加入
 * - 多个：让用户选一个
 */
@Composable
private fun AddToPlanDialog(
    exercise: com.example.fitness.data.Exercise,
    exerciseViewModel: ExerciseViewModel,
    onDismiss: () -> Unit,
    onAdded: (planName: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customPlans = exerciseViewModel.allCustomPlans()
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var rest by remember { mutableStateOf("60") }
    var selectedPlanId by remember { mutableStateOf(customPlans.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("加入方案") },
        text = {
            Column {
                if (customPlans.isEmpty()) {
                    Text(
                        text = "你还没有健身方案。先去\"方案\"页面新建一个再来添加。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    if (customPlans.size == 1) {
                        Text(
                            text = "将加入方案：${customPlans[0].name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text("选择方案：", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(customPlans) { plan ->
                                Surface(
                                    onClick = { selectedPlanId = plan.id },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (plan.id == selectedPlanId) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(plan.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = "${plan.exercises.size} 个动作",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (plan.id == selectedPlanId) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("每组参数：", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
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
                            label = { Text("次数") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rest,
                            onValueChange = { rest = it.filter { c -> c.isDigit() } },
                            label = { Text("休息(s)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (customPlans.isEmpty()) {
                TextButton(onClick = onDismiss) { Text("关闭") }
            } else {
                TextButton(
                    onClick = {
                        if (selectedPlanId.isNotBlank()) {
                            val planName = customPlans.find { it.id == selectedPlanId }?.name ?: ""
                            exerciseViewModel.addExerciseToPlan(
                                selectedPlanId,
                                com.example.fitness.data.PlanExercise(
                                    exerciseId = exercise.id,
                                    sets = sets.toIntOrNull() ?: 3,
                                    reps = reps.toIntOrNull() ?: 10,
                                    restSeconds = rest.toIntOrNull() ?: 60
                                )
                            )
                            onDismiss()
                            onAdded(planName)
                        }
                    }
                ) { Text("加入") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/**
 * 在 Instructions 数据类里按用户选定的语言 code 取值，没有就按优先级回退：
 * 用户选的语言 -> 英语 -> 任何存在的非空语言。
 */
private fun pickLocalized(inst: Instructions, code: String): String {
    val candidates = listOf(code, "en", "es", "it", "ru", "tr", "zh", "hi", "ko", "pl")
    for (c in candidates) {
        val value = when (c) {
            "en" -> inst.en
            "es" -> inst.es
            "it" -> inst.it
            "tr" -> inst.tr
            "ru" -> inst.ru
            "zh" -> inst.zh
            else -> null
        }
        if (!value.isNullOrBlank()) return value
    }
    return ""
}

private fun pickLocalizedSteps(steps: InstructionSteps, code: String): List<String> {
    val candidates = listOf(code, "en", "es", "it", "ru", "tr", "zh", "hi", "ko", "pl")
    for (c in candidates) {
        val value = when (c) {
            "en" -> steps.en
            "es" -> steps.es
            "it" -> steps.it
            "tr" -> steps.tr
            "ru" -> steps.ru
            "zh" -> steps.zh
            else -> emptyList()
        }
        if (value.isNotEmpty()) return value
    }
    return emptyList()
}

@Composable
private fun DetailSection(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(category: String, navController: NavHostController, viewModel: ExerciseViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { com.example.fitness.data.ExerciseRepository(context) }
    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val filtered = exercises.filter { it.category.equals(category, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(com.example.fitness.data.DataTranslations.category(category), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { exercise ->
                ExerciseGridItem(exercise, repository, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(equipment: String, navController: NavHostController, viewModel: ExerciseViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { com.example.fitness.data.ExerciseRepository(context) }
    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val filtered = exercises.filter { it.equipment.equals(equipment, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(com.example.fitness.data.DataTranslations.equipment(equipment), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { exercise ->
                ExerciseGridItem(exercise, repository, navController)
            }
        }
    }
}

@Composable
private fun ExerciseGridItem(
    exercise: Exercise,
    repository: com.example.fitness.data.ExerciseRepository,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("exercise/${exercise.id}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = repository.displayNameOf(exercise),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            Text(
                text = com.example.fitness.data.DataTranslations.equipment(exercise.equipment),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}