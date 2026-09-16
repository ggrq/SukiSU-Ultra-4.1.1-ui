package com.sukisu.ultra.ui.screen

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.util.BattleApi
import com.sukisu.ultra.ui.util.BattleSummary
import com.sukisu.ultra.ui.util.HonorPlayer
import com.sukisu.ultra.ui.util.rememberNetworkImage
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private val HonorGold = Color(0xFFE8B84B)
private val HonorGoldDeep = Color(0xFF9C6B12)
private val HonorDark = Color(0xFF0A1220)
private val HonorCard = Color(0xFF152A4D)
private val HonorText = Color(0xFFE8EAF2)
private val HonorDim = Color(0xFF8A94AD)
private val HonorSub = Color(0xFFB8C4DC)

@Composable
private fun BattleDiamond(diamondSize: Int, color: Color = HonorGold) {
    Canvas(modifier = Modifier.size(diamondSize.dp)) {
        val c = center
        val s = size.minDimension * 0.42f
        val outer = Path().apply {
            moveTo(c.x, c.y - s); lineTo(c.x + s, c.y); lineTo(c.x, c.y + s); lineTo(c.x - s, c.y); close()
        }
        drawPath(outer, color = color)
        val inner = Path().apply {
            moveTo(c.x, c.y - s * 0.45f); lineTo(c.x + s * 0.26f, c.y); lineTo(c.x, c.y + s * 0.45f); lineTo(c.x - s * 0.26f, c.y); close()
        }
        drawPath(inner, color = HonorDark)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BattleDiamond(16)
        Spacer(Modifier.size(8.dp))
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HonorGold)
    }
}

@Composable
fun BattleQueryScreen() {
    val navigator = LocalNavigator.current
    val scrollBehavior = MiuixScrollBehavior()
    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = colorScheme.surface,
        tint = HazeTint(colorScheme.surface.copy(0.8f))
    )
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var players by remember { mutableStateOf<List<HonorPlayer>>(emptyList()) }
    var searched by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<HonorPlayer?>(null) }
    var summary by remember { mutableStateOf<BattleSummary?>(null) }
    var battleImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var loadingDetail by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var savedTip by remember { mutableStateOf(false) }

    fun doSearch() {
        val name = query.trim()
        if (name.isEmpty()) return
        searching = true
        searched = true
        players = emptyList()
        selected = null
        summary = null
        battleImage = null
        errorMsg = null
        scope.launch {
            players = BattleApi.searchPlayer(name)
            searching = false
            if (players.isEmpty()) errorMsg = "没有找到「$name」，换个名字试试"
        }
    }

    fun loadDetail(player: HonorPlayer) {
        selected = player
        summary = null
        battleImage = null
        errorMsg = null
        savedTip = false
        loadingDetail = true
        scope.launch {
            val text = BattleApi.fetchBattleText(player.uid)
            val img = BattleApi.fetchUserImage(player.uid)
            summary = text?.let { BattleApi.parseBattleText(it) }
            battleImage = img
            loadingDetail = false
            if (summary == null && img == null) errorMsg = "战绩数据拉取失败，请稍后重试"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.hazeEffect(hazeState) {
                    style = hazeStyle
                    blurRadius = 30.dp
                    noiseFactor = 0f
                },
                color = Color.Transparent,
                title = "峡谷战绩查询",
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = dropUnlessResumed { navigator.pop() }
                    ) {
                        val layoutDirection = LocalLayoutDirection.current
                        Icon(
                            modifier = Modifier.graphicsLayer {
                                if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                            },
                            imageVector = MiuixIcons.Back,
                            contentDescription = null,
                            tint = colorScheme.onBackground
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 搜索区
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(color = HonorCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionTitle("召唤师查询")
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x22060B18))
                            ) {
                                TextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    label = "输入玩家名字",
                                    modifier = Modifier.fillMaxWidth(),
                                    useLabelAsPlaceholder = query.isEmpty()
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(listOf(HonorGold, HonorGoldDeep))
                                    )
                                    .clickable(enabled = !searching) { doSearch() }
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (searching) {
                                    Text("查询中…", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HonorDark)
                                } else {
                                    Text("查询", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HonorDark)
                                }
                            }
                        }
                    }
                }
            }

            // 候选玩家
            if (players.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(color = HonorCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle("找到 ${players.size} 位同名召唤师，点击选择")
                            Spacer(Modifier.height(12.dp))
                            players.forEach { p ->
                                PlayerRow(p, selected?.uid == p.uid) { loadDetail(p) }
                            }
                        }
                    }
                }
            }

            // 详情加载中
            if (loadingDetail) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(color = HonorCard)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BattleDiamond(16)
                            Spacer(Modifier.size(10.dp))
                            Text("正在召唤峡谷战绩…", fontSize = 14.sp, color = HonorSub)
                        }
                    }
                }
            }

            // 错误
            if (errorMsg != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(color = Color(0x33A03030))
                    ) {
                        Text(
                            text = errorMsg!!,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = Color(0xFFFF9B9B)
                        )
                    }
                }
            }

            selected?.let { player ->
                // 战绩总结图
                battleImage?.let { img ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.defaultColors(color = HonorCard)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SectionTitle("战绩总结图")
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        text = if (savedTip) "已保存" else "保存图片",
                                        onClick = {
                                            val ok = runCatching {
                                                val name = "honor_${player.name}_${System.currentTimeMillis()}.png"
                                                val values = ContentValues().apply {
                                                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                                                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                                                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KernelSU")
                                                }
                                                val uri = context.contentResolver.insert(
                                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                                                ) ?: return@TextButton
                                                context.contentResolver.openOutputStream(uri)?.use { out ->
                                                    img.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
                                                } ?: run { context.contentResolver.delete(uri, null, null) }
                                                true
                                            }.getOrDefault(false)
                                            savedTip = ok
                                        }
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Image(
                                    bitmap = img,
                                    contentDescription = "战绩总结图",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        }
                    }
                }

                summary?.let { s ->
                    // 总览
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.defaultColors(color = HonorCard)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SectionTitle("战绩总览")
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = player.dw,
                                        fontSize = 12.sp,
                                        color = HonorGold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x33E8B84B))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text(s.winRate, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = HonorGold)
                                        Spacer(Modifier.height(4.dp))
                                        Text("总胜率", fontSize = 12.sp, color = HonorDim)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Text("${s.totalGames}", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = HonorText)
                                        Spacer(Modifier.height(4.dp))
                                        Text("总场次", fontSize = 12.sp, color = HonorDim)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Canvas(modifier = Modifier.size(8.dp)) { drawCircle(Color(0xFF4CAF7D)) }
                                            Spacer(Modifier.width(4.dp))
                                            Text("${s.wins}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7FE0A8))
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Canvas(modifier = Modifier.size(8.dp)) { drawCircle(Color(0xFFE05555)) }
                                            Spacer(Modifier.width(4.dp))
                                            Text("${s.losses}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF8A8A))
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                                if (s.modeStats.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    s.modeStats.forEach { m ->
                                        Text(m, fontSize = 12.sp, color = HonorSub)
                                        Spacer(Modifier.height(3.dp))
                                    }
                                }
                            }
                        }
                    }

                    // 常玩英雄
                    if (s.heroStats.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.defaultColors(color = HonorCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    SectionTitle("常玩英雄 TOP${s.heroStats.size}")
                                    Spacer(Modifier.height(10.dp))
                                    s.heroStats.forEach { h ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .then(
                                                        if (h.rank <= 3) Modifier.background(Brush.linearGradient(listOf(HonorGold, HonorGoldDeep)))
                                                        else Modifier.background(Color(0x33060B18))
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${h.rank}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (h.rank <= 3) HonorDark else HonorSub)
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(h.hero, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HonorText)
                                                Spacer(Modifier.height(2.dp))
                                                Text("战力 ${h.power}", fontSize = 11.sp, color = HonorDim)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(h.winRate, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HonorGold)
                                                Spacer(Modifier.height(2.dp))
                                                Text("${h.games} 场", fontSize = 11.sp, color = HonorDim)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 逐局明细
                    if (s.matches.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.defaultColors(color = HonorCard)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    SectionTitle("逐局明细 · 最近 ${s.matches.size} 局")
                                    Spacer(Modifier.height(6.dp))
                                    s.matches.forEach { m ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(52.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (m.result == "胜利") Color(0x334CAF7D) else Color(0x33E05555)
                                                    )
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    m.result,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (m.result == "胜利") Color(0xFF7FE0A8) else Color(0xFFFF8A8A)
                                                )
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(m.hero, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HonorText)
                                                    if (m.mvp) {
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            "MVP",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = HonorDark,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(HonorGold)
                                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    "${m.time} · ${m.mode}",
                                                    fontSize = 11.sp,
                                                    color = HonorDim
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("KDA ${m.kda}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = HonorSub)
                                                Spacer(Modifier.height(2.dp))
                                                Text("评分 ${m.score}", fontSize = 11.sp, color = if (m.score.toDoubleOrNull()?.let { it >= 10.0 } == true) HonorGold else HonorDim)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!searched && selected == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(color = HonorCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BattleDiamond(34)
                            Spacer(Modifier.height(14.dp))
                            Text("输入召唤师名字，查询段位、UID 与战绩", fontSize = 14.sp, color = HonorSub)
                            Spacer(Modifier.height(6.dp))
                            Text("支持生成战绩总结图 · 一键保存", fontSize = 12.sp, color = HonorDim)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun PlayerRow(player: HonorPlayer, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0x33E8B84B) else Color(0x22060B18))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val avatar = player.avatar.let { rememberNetworkImage(it.ifBlank { null }) }
        if (avatar != null) {
            Image(
                bitmap = avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(HonorGold, HonorGoldDeep))),
                contentAlignment = Alignment.Center
            ) {
                Text(player.name.take(1), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = HonorDark)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(player.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HonorText)
                Spacer(Modifier.width(8.dp))
                Text(player.dw, fontSize = 11.sp, color = HonorGold)
            }
            Spacer(Modifier.height(3.dp))
            Text("${player.region} · 等级 ${player.level} · UID ${player.uid}", fontSize = 11.sp, color = HonorDim)
        }
        if (selected) {
            Text("查看中", fontSize = 11.sp, color = HonorGold)
        } else {
            Text("查看战绩", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HonorGold)
        }
    }
}
