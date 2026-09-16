package com.sukisu.ultra.ui.screen

import android.content.Context
import android.os.Build
import android.system.Os
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin
import com.sukisu.ultra.KernelVersion
import com.sukisu.ultra.Natives
import com.sukisu.ultra.R
import com.sukisu.ultra.getKernelVersion
import com.sukisu.ultra.ui.LocalPagerState
import com.sukisu.ultra.ui.component.DropdownItem
import com.sukisu.ultra.ui.component.RebootListPopup
import com.sukisu.ultra.ui.component.rememberConfirmDialog
import com.sukisu.ultra.ui.navigation3.Navigator
import com.sukisu.ultra.ui.navigation3.Route
import com.sukisu.ultra.ui.theme.isInDarkTheme
import com.sukisu.ultra.ui.util.*
import com.sukisu.ultra.ui.util.module.LatestVersionInfo
import com.sukisu.ultra.ui.util.reboot
import com.sukisu.ultra.ui.util.rootAvailable
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.isDynamicColor
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun HomePager(
    navigator: Navigator,
    bottomInnerPadding: Dp
) {
    val kernelVersion = getKernelVersion()
    val scrollBehavior = MiuixScrollBehavior()
    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = colorScheme.surface,
        tint = HazeTint(colorScheme.surface.copy(0.8f))
    )

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val checkUpdate = prefs.getBoolean("check_update", true)
    val themeMode = prefs.getInt("color_mode", 0)

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                hazeState = hazeState,
                hazeStyle = hazeStyle,
            )
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp)
                .hazeSource(state = hazeState),
            contentPadding = innerPadding,
            overscrollEffect = null,
        ) {
            item {
                HonorHeroBanner()
            }
            item {
                val isManager = Natives.isManager
                val ksuVersion = if (isManager) Natives.version else null
                val lkmMode = ksuVersion?.let {
                    if (kernelVersion.isGKI()) Natives.isLkmMode else null
                }
                val pageState = LocalPagerState.current
                val coroutineScope = rememberCoroutineScope()

                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (isManager && Natives.requireNewKernel()) {
                        WarningCard(
                            stringResource(id = R.string.require_kernel_version)
                                .format(ksuVersion, Natives.MINIMAL_SUPPORTED_KERNEL),
                            themeMode
                        )
                    }
                    if (ksuVersion != null && !rootAvailable()) {
                        WarningCard(
                            stringResource(id = R.string.grant_root_failed),
                            themeMode
                        )
                    }
                    StatusCard(
                        kernelVersion, ksuVersion, lkmMode,
                        onClickInstall = {
                            navigator.push(Route.Install)
                        },
                        onClickSuperuser = {
                            coroutineScope.launch {
                                pageState.animateScrollToPage(page = 1, animationSpec = tween(easing = EaseInOut))
                            }
                        },
                        onclickModule = {
                            coroutineScope.launch {
                                pageState.animateScrollToPage(page = 2, animationSpec = tween(easing = EaseInOut))
                            }
                        },
                        themeMode = themeMode
                    )

                    HonorToolsSection(
                        onClickGuide = { navigator.push(Route.HonorGuide) },
                        onClickBattle = { navigator.push(Route.BattleQuery) }
                    )

                    if (checkUpdate) {
                        UpdateCard(themeMode)
                    }
                    InfoCard()
                }
                Spacer(Modifier.height(bottomInnerPadding))
            }
        }
    }
}

@Composable
private fun HonorHeroBanner() {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 8000, easing = LinearEasing))
    )
    val sweepPhase = ((phase / (2f * PI.toFloat())) % 1f).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.honor_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x44060B18), Color(0xE60A1220))
                    )
                )
        )
        // 金色流光扫过层
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepX = sweepPhase * (size.width + 480.dp.toPx()) - 240.dp.toPx()
            drawRect(
                brush = Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.3f to Color(0x22E8B84B),
                    0.5f to Color(0x3DE8B84B),
                    0.7f to Color(0x22E8B84B),
                    1f to Color.Transparent,
                ),
                topLeft = Offset(sweepX, 0f),
                size = Size(260.dp.toPx(), size.height)
            )
        }
        // 金色光粒子浮动层（双层）
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            repeat(18) { i ->
                val x = size.width * ((i * 0.055f + phase * 0.012f) % 1f)
                val y = size.height * (0.15f + 0.22f * sin(phase + i * 1.7f))
                val alpha = 0.08f + 0.12f * sin(phase * 1.3f + i)
                val r = (1.2f + 1.6f * sin(phase + i * 0.9f)).dp.toPx()
                drawCircle(
                    color = Color(0xFFE8B84B),
                    radius = r.coerceAtLeast(1.dp.toPx()),
                    center = Offset(x, y),
                    alpha = alpha.coerceIn(0f, 0.5f)
                )
            }
            repeat(8) { i ->
                val x = size.width * ((i * 0.13f + phase * 0.008f + 0.05f) % 1f)
                val y = size.height * (0.5f + 0.3f * sin(phase * 0.8f + i * 2.1f))
                drawCircle(
                    color = Color(0xFF9BD1FF),
                    radius = (0.8f + 0.7f * sin(phase + i)).dp.toPx().coerceAtLeast(0.8.dp.toPx()),
                    center = Offset(x, y),
                    alpha = 0.08f
                )
            }
        }
        // 右上角王者荣耀风格菱形徽记（带外圈光晕）
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val diamondSize = 110.dp.toPx()
            val centerX = size.width - 80.dp.toPx()
            val centerY = 78.dp.toPx()
            // 外圈光晕
            drawCircle(
                color = Color(0x22E8B84B),
                radius = diamondSize * 0.85f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0x11E8B84B),
                radius = diamondSize * 1.25f,
                center = Offset(centerX, centerY)
            )
            val path = Path().apply {
                moveTo(centerX, centerY - diamondSize / 2)
                lineTo(centerX + diamondSize / 2, centerY)
                lineTo(centerX, centerY + diamondSize / 2)
                lineTo(centerX - diamondSize / 2, centerY)
                close()
            }
            drawPath(path, color = Color(0x2AE8B84B))
            drawPath(path, color = Color(0xFFE8B84B), style = Stroke(width = 3.dp.toPx()))
            // 菱形内剑刃
            val blade = Path().apply {
                moveTo(centerX, centerY - diamondSize / 3)
                lineTo(centerX + diamondSize / 5, centerY)
                lineTo(centerX, centerY + diamondSize / 3)
                lineTo(centerX - diamondSize / 5, centerY)
                close()
            }
            drawPath(blade, color = Color(0xFFE8B84B))
            // 菱形两侧小尖
            val leftSpike = Path().apply {
                moveTo(centerX - diamondSize * 0.72f, centerY)
                lineTo(centerX - diamondSize * 0.52f, centerY - diamondSize * 0.14f)
                lineTo(centerX - diamondSize * 0.52f, centerY + diamondSize * 0.14f)
                close()
            }
            val rightSpike = Path().apply {
                moveTo(centerX + diamondSize * 0.72f, centerY)
                lineTo(centerX + diamondSize * 0.52f, centerY - diamondSize * 0.14f)
                lineTo(centerX + diamondSize * 0.52f, centerY + diamondSize * 0.14f)
                close()
            }
            drawPath(leftSpike, color = Color(0xFFE8B84B))
            drawPath(rightSpike, color = Color(0xFFE8B84B))
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(14.dp)) {
                    val c = center
                    val s = size.minDimension * 0.42f
                    val p = Path().apply {
                        moveTo(c.x, c.y - s); lineTo(c.x + s, c.y); lineTo(c.x, c.y + s); lineTo(c.x - s, c.y); close()
                    }
                    drawPath(p, color = Color(0xFFE8B84B))
                }
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "召唤师 · 欢迎回到峡谷",
                    color = Color(0x99E8B84B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "王者荣耀 · KernelSU",
                color = Color(0xFFE8B84B),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "HONOR OF KERNEL · 荣耀守护者 · 峡谷模式",
                color = Color(0xFFB8C4DC),
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
            )
        }
        // 底部金色能量带
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0x99E8B84B),
                            Color(0xFFE8B84B),
                            Color(0x99E8B84B),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun HonorToolsSection(
    onClickGuide: () -> Unit,
    onClickBattle: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val c = center
                val s = size.minDimension * 0.42f
                val p = Path().apply {
                    moveTo(c.x, c.y - s); lineTo(c.x + s, c.y); lineTo(c.x, c.y + s); lineTo(c.x - s, c.y); close()
                }
                drawPath(p, color = Color(0xFFE8B84B))
            }
            Spacer(Modifier.size(7.dp))
            Text(
                text = "荣耀工具 · 王者专区",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE8B84B)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "峡谷特供",
                fontSize = 10.sp,
                color = Color(0x99E8B84B),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x22E8B84B))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        HonorToolCard(
            title = "荣耀图鉴",
            subtitle = "英雄定位 · 皮肤列表 · 官方详情",
            accent = Color(0xFFE8B84B),
            onClick = onClickGuide
        )
        HonorToolCard(
            title = "峡谷战绩查询",
            subtitle = "召唤师搜索 · UID · 战绩总结图",
            accent = Color(0xFF7FB5FF),
            onClick = onClickBattle
        )
    }
}

@Composable
private fun HonorToolCard(
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt,
        colors = CardDefaults.defaultColors(
            color = Color(0xFF152A4D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 渐变底菱形图标
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(accent, Color(0xFF9C6B12)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(26.dp)) {
                    val c = center
                    val s = size.minDimension * 0.42f
                    val p = Path().apply {
                        moveTo(c.x, c.y - s); lineTo(c.x + s, c.y); lineTo(c.x, c.y + s); lineTo(c.x - s, c.y); close()
                    }
                    drawPath(p, color = Color(0xFF0A1220))
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFFB8C4DC)
                )
            }
            Icon(
                imageVector = MiuixIcons.Link,
                tint = colorScheme.onSurface,
                contentDescription = null
            )
        }
    }
}

@Composable
fun UpdateCard(
    themeMode: Int,
) {
    val context = LocalContext.current
    val latestVersionInfo = LatestVersionInfo()
    val newVersion by produceState(initialValue = latestVersionInfo) {
        value = withContext(Dispatchers.IO) {
            checkNewVersion()
        }
    }

    val currentVersionCode = getManagerVersion(context).second
    val newVersionCode = newVersion.versionCode
    val newVersionUrl = newVersion.downloadUrl
    val changelog = newVersion.changelog

    val uriHandler = LocalUriHandler.current
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)

    AnimatedVisibility(
        visible = newVersionCode > currentVersionCode,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { uriHandler.openUri(newVersionUrl) })
        WarningCard(
            message = stringResource(id = R.string.new_version_available).format(newVersionCode),
            themeMode = themeMode,
            color = colorScheme.outline
        ) {
            if (changelog.isEmpty()) {
                uriHandler.openUri(newVersionUrl)
            } else {
                updateDialog.showConfirm(
                    title = title,
                    content = changelog,
                    markdown = true,
                    confirm = updateText
                )
            }
        }
    }
}

@Composable
fun RebootDropdownItem(
    @StringRes id: Int, reason: String = "",
    showTopPopup: MutableState<Boolean>,
    optionSize: Int,
    index: Int,
) {
    DropdownItem(
        text = stringResource(id),
        optionSize = optionSize,
        onSelectedIndexChange = {
            reboot(reason)
            showTopPopup.value = false
        },
        index = index
    )
}

@Composable
private fun TopBar(
    scrollBehavior: ScrollBehavior,
    hazeState: HazeState,
    hazeStyle: HazeStyle,
) {
    TopAppBar(
        modifier = Modifier.hazeEffect(hazeState) {
            style = hazeStyle
            blurRadius = 30.dp
            noiseFactor = 0f
        },
        color = Color.Transparent,
        title = stringResource(R.string.app_name),
        actions = {
            RebootListPopup(
                modifier = Modifier.padding(end = 16.dp),
            )
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun StatusCard(
    kernelVersion: KernelVersion,
    ksuVersion: Int?,
    lkmMode: Boolean?,
    onClickInstall: () -> Unit = {},
    onClickSuperuser: () -> Unit = {},
    onclickModule: () -> Unit = {},
    themeMode: Int,
) {
    Column(
        modifier = Modifier
    ) {
        when {
            ksuVersion != null -> {
                val safeMode = when {
                    Natives.isSafeMode -> " [${stringResource(id = R.string.safe_mode)}]"
                    else -> ""
                }

                val workingMode = when (lkmMode) {
                    null -> ""
                    true -> " <LKM>"
                    else -> " <Built-in>"
                }

                val workingText = "${stringResource(id = R.string.home_working)}$workingMode$safeMode"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.defaultColors(
                            color = when {
                                isDynamicColor -> colorScheme.secondaryContainer
                                isInDarkTheme(themeMode) -> Color(0xFF152A4D)
                                else -> Color(0xFFF5EBD3)
                            }
                        ),
                        onClick = {
                            if (kernelVersion.isGKI()) onClickInstall()
                        },
                        showIndication = true,
                        pressFeedbackType = PressFeedbackType.Tilt
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(38.dp, 45.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Icon(
                                    modifier = Modifier.size(170.dp),
                                    imageVector = Icons.Rounded.CheckCircleOutline,
                                    tint = if (isDynamicColor) {
                                        colorScheme.primary.copy(alpha = 0.8f)
                                    } else {
                                        Color(0xFFE8B84B)
                                    },
                                    contentDescription = null
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(all = 16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Canvas(modifier = Modifier.size(10.dp)) {
                                        val c = center
                                        val s = size.minDimension * 0.42f
                                        val p = Path().apply {
                                            moveTo(c.x, c.y - s); lineTo(c.x + s, c.y); lineTo(c.x, c.y + s); lineTo(c.x - s, c.y); close()
                                        }
                                        drawPath(p, color = Color(0xFFE8B84B))
                                    }
                                    Spacer(Modifier.size(5.dp))
                                    Text(
                                        text = "运行状态",
                                        fontSize = 10.sp,
                                        color = Color(0xCCE8B84B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = workingText,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.home_working_version, ksuVersion),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0x33E8B84B), Color(0xFFE8B84B), Color(0x33E8B84B))
                                            )
                                        )
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            onClick = { onClickSuperuser() },
                            showIndication = true,
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.superuser),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = colorScheme.onSurfaceVariantSummary,
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = getSuperuserCount().toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE8B84B),
                                )
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0x33E8B84B), Color(0xFFE8B84B), Color(0x33E8B84B))
                                            )
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            insideMargin = PaddingValues(16.dp),
                            onClick = { onclickModule() },
                            showIndication = true,
                            pressFeedbackType = PressFeedbackType.Tilt
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.module),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = colorScheme.onSurfaceVariantSummary,
                                )
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = getModuleCount().toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE8B84B),
                                )
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0x33E8B84B), Color(0xFFE8B84B), Color(0x33E8B84B))
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            kernelVersion.isGKI() -> {
                Card(
                    onClick = {
                        if (kernelVersion.isGKI()) onClickInstall()
                    },
                    showIndication = true,
                    pressFeedbackType = PressFeedbackType.Tilt,
                    colors = CardDefaults.defaultColors(
                        color = if (isInDarkTheme(themeMode)) Color(0xFF152A4D) else Color(0xFFF5EBD3)
                    )
                ) {
                    BasicComponent(
                        title = "召唤师，内核尚未部署",
                        summary = "点击进入安装 · 荣耀由此开始",
                        startAction = {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                "内核尚未部署",
                                modifier = Modifier
                                    .padding(end = 16.dp),
                                tint = Color(0xFFE8B84B),
                            )
                        }
                    )
                }
            }

            else -> {
                Card(
                    onClick = {
                        if (kernelVersion.isGKI()) onClickInstall()
                    },
                    showIndication = true,
                    pressFeedbackType = PressFeedbackType.Tilt,
                    colors = CardDefaults.defaultColors(
                        color = if (isInDarkTheme(themeMode)) Color(0xFF152A4D) else Color(0xFFF5EBD3)
                    )
                ) {
                    BasicComponent(
                        title = "峡谷信号受阻 · 设备暂不支持",
                        summary = stringResource(R.string.home_unsupported_reason),
                        startAction = {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                "暂不支持",
                                modifier = Modifier
                                    .padding(end = 16.dp),
                                tint = Color(0xFFC0392B),
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WarningCard(
    message: String,
    themeMode: Int,
    color: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        onClick = {
            onClick?.invoke()
        },
        colors = CardDefaults.defaultColors(
            color = color ?: when {
                isDynamicColor -> colorScheme.errorContainer
                isInDarkTheme(themeMode) -> Color(0XFF310808)
                else -> Color(0xFFF8E2E2)
            }
        ),
        showIndication = onClick != null,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = message,
                color = if (isDynamicColor) colorScheme.onErrorContainer else Color(0xFFF72727),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun InfoCard() {
    val manualHookText = stringResource(R.string.manual_hook)
    val inlineHookText = stringResource(R.string.inline_hook)
    val susfsInfo = rememberSusfsInfo(manualHookText, inlineHookText)
    val isSusfsSupported = susfsInfo.status == SusfsStatus.Supported

    @Composable
    fun InfoText(
        title: String,
        content: String,
        bottomPadding: Dp = 24.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(8.dp)) {
                val c = center
                val s = size.minDimension * 0.42f
                val p = Path().apply {
                    moveTo(c.x, c.y - s); lineTo(c.x + s, c.y); lineTo(c.x, c.y + s); lineTo(c.x - s, c.y); close()
                }
                drawPath(p, color = Color(0xFFE8B84B))
            }
            Spacer(Modifier.size(6.dp))
            Text(
                text = title,
                fontSize = MiuixTheme.textStyles.headline1.fontSize,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE8B84B)
            )
        }
        Text(
            text = content,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 2.dp, bottom = bottomPadding)
        )
    }

    val context = LocalContext.current
    val uname = Os.uname()
    val managerVersion = getManagerVersion(context)

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            InfoText(
                title = stringResource(R.string.home_kernel),
                content = uname.release
            )
            InfoText(
                title = stringResource(R.string.home_manager_version),
                content = "${managerVersion.first} (${managerVersion.second})"
            )
            if (isSusfsSupported) {
                InfoText(
                    title = stringResource(R.string.home_susfs_version),
                    content = susfsInfo.detail
                )
            }
            InfoText(
                title = stringResource(R.string.home_selinux_status),
                content = getSELinuxStatus(),
                bottomPadding = 0.dp
            )
        }
    }
}

fun getManagerVersion(context: Context): Pair<String, Long> {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    return Pair(packageInfo.versionName!!, versionCode)
}

private enum class SusfsStatus {
    Idle, Loading, Supported, Unsupported, Error
}

private data class SusfsInfoState(
    val status: SusfsStatus = SusfsStatus.Idle,
    val detail: String = "",
)

@Composable
private fun rememberSusfsInfo(
    manualHookLabel: String,
    inlineHookLabel: String,
): SusfsInfoState {
    return remember(manualHookLabel, inlineHookLabel) {
        runCatching {
            val supported = getSuSFSStatus().equals("true", ignoreCase = true)
            if (supported) {
                val version = getSuSFSVersion().trim()
                val hookLabel = when (val type = Natives.getHookType()) {
                    "Manual" -> manualHookLabel
                    "Inline" -> inlineHookLabel
                    else -> type
                }.takeIf { it.isNotBlank() }?.let { "($it)" }.orEmpty()
                SusfsInfoState(
                    status = SusfsStatus.Supported,
                    detail = listOf(version, hookLabel)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                )
            } else {
                SusfsInfoState(
                    status = SusfsStatus.Unsupported,
                    detail = ""
                )
            }
        }.getOrElse {
            SusfsInfoState(status = SusfsStatus.Error)
        }
    }
}
