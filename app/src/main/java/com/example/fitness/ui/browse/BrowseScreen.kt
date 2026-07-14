package com.example.fitness.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitness.R
import com.example.fitness.data.Exercise
import com.example.fitness.ui.exercise.ExerciseViewModel

enum class FilterType { Category, Equipment }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(navController: NavController, viewModel: ExerciseViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val categories by remember { derivedStateOf { viewModel.getCategories().sorted() } }
    val equipments by remember { derivedStateOf { viewModel.getEquipments().sorted() } }
    var selectedFilter by remember { mutableStateOf(FilterType.Category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.browse_title), fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == FilterType.Category,
                    onClick = { selectedFilter = FilterType.Category },
                    label = { Text(stringResource(R.string.browse_by_body_part)) }
                )
                FilterChip(
                    selected = selectedFilter == FilterType.Equipment,
                    onClick = { selectedFilter = FilterType.Equipment },
                    label = { Text(stringResource(R.string.browse_by_equipment)) }
                )
            }

            // Filter list
            when (selectedFilter) {
                FilterType.Category -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val catExercises = exercises.filter { it.category.equals(category, ignoreCase = true) }
                            FilterCard(
                                icon = Icons.Default.FitnessCenter,
                                title = com.example.fitness.data.DataTranslations.category(category),
                                subtitle = stringResource(R.string.browse_exercises_count, catExercises.size),
                                onClick = {
                                    navController.navigate("browse/category/$category")
                                }
                            )
                        }
                    }
                }
                FilterType.Equipment -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(equipments) { equipment ->
                            val eqExercises = exercises.filter { it.equipment.equals(equipment, ignoreCase = true) }
                            FilterCard(
                                icon = Icons.Outlined.Build,
                                title = com.example.fitness.data.DataTranslations.equipment(equipment),
                                subtitle = stringResource(R.string.browse_exercises_count, eqExercises.size),
                                onClick = {
                                    navController.navigate("browse/equipment/$equipment")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 圆形 icon 容器（用主题主色做强调）
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 右侧箭头
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
