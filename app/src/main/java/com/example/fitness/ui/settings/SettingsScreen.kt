package com.example.fitness.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.fitness.R
import com.example.fitness.ui.exercise.ExerciseViewModel

/**
 * 关于页（替代 Settings）：介绍应用、开源、开发者、版本、仓库链接、赞助。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    exerciseViewModel: ExerciseViewModel
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item { SectionTitle(stringResource(R.string.about_intro_title)) }
            item { SectionCard(stringResource(R.string.about_intro_text)) }

            item { SectionTitle(stringResource(R.string.about_open_source_title)) }
            item { SectionCard(stringResource(R.string.about_open_source_text)) }

            item { SectionTitle(stringResource(R.string.about_dataset_title)) }
            item { SectionCard(stringResource(R.string.about_dataset_text)) }

            item { SectionTitle(stringResource(R.string.about_developer_title)) }
            item { SectionCard(stringResource(R.string.about_developer_text)) }

            item { SectionTitle(stringResource(R.string.about_version_title)) }
            item {
                SectionCard(stringResource(R.string.about_version))
            }

            // GitHub 仓库（可点击打开浏览器）
            item { SectionTitle(stringResource(R.string.about_repo_title)) }
            item {
                ExternalLinkCard(
                    label = stringResource(R.string.about_repo_url),
                    description = stringResource(R.string.about_repo_action),
                    url = stringResource(R.string.about_repo_url),
                    context = context
                )
            }

            // 开源许可（可点击打开）
            item { SectionTitle(stringResource(R.string.about_license_title)) }
            item {
                ExternalLinkCard(
                    label = "MIT License",
                    description = stringResource(R.string.about_license_url),
                    url = stringResource(R.string.about_license_url),
                    context = context
                )
            }
            item {
                Text(
                    text = stringResource(R.string.about_license_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 免责声明（嵌入文本，不能链接）
            item { SectionTitle(stringResource(R.string.about_disclaimer_title)) }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_disclaimer_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 赞助
            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle(stringResource(R.string.about_donate_title))
            }
            item {
                Text(
                    text = stringResource(R.string.about_donate_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            // 微信赞赏码
            item {
                DonateQrCard(
                    label = stringResource(R.string.about_donate_wechat),
                    imagePath = "file:///android_asset/vxJackLizsm.jpg"
                )
            }
            // 支付宝收款码
            item {
                DonateQrCard(
                    label = stringResource(R.string.about_donate_alipay),
                    imagePath = "file:///android_asset/zfbJackLiskm.png"
                )
            }
            // PayPal 海外赞助（点击打开浏览器）
            item {
                ExternalLinkCard(
                    label = stringResource(R.string.about_donate_paypal),
                    description = stringResource(R.string.about_donate_paypal_url),
                    url = stringResource(R.string.about_donate_paypal_url),
                    context = context
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/**
 * 打开外部链接：Intent.ACTION_VIEW
 */
@Composable
private fun ExternalLinkCard(
    label: String,
    description: String,
    url: String,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, R.string.about_external_link, Toast.LENGTH_SHORT).show()
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        R.string.about_link_open_failed,
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "${context.getString(R.string.about_link_open_failed)}\n$url",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

/**
 * 赞助二维码卡：缩略图 + 点击放大查看
 */
@Composable
private fun DonateQrCard(label: String, imagePath: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            Spacer(Modifier.height(8.dp))
            if (expanded) {
                AsyncImage(
                    model = imagePath,
                    contentDescription = label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "点击图片关闭",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "点击展开二维码",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
    )
}

@Composable
private fun SectionCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
    }
}