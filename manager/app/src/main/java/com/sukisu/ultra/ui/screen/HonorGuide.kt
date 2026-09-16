package com.sukisu.ultra.ui.screen

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
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.sukisu.ultra.R
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.util.HonorApi
import com.sukisu.ultra.ui.util.rememberNetworkImage
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical

// 王者荣耀英雄图鉴：优先加载官方 API 真实数据，失败时使用内置英雄兜底
private data class HeroData(
    val name: String,
    val title: String,
    val typeLabel: String,
    val skins: List<String>,
    val avatarUrl: String? = null,
    val detailUrl: String? = null,
)

private val fallbackHeroes = listOf(
    HeroData("李白", "青莲剑仙", "刺客/战士", listOf("将进酒", "青莲剑歌")),
    HeroData("韩信", "国士无双", "刺客", listOf("背水一战", "国士无双")),
    HeroData("貂蝉", "绝世舞姬", "法师", listOf("缘·心结", "绽·风华")),
    HeroData("鲁班七号", "机关造物", "射手", listOf("空中支援", "无敌鲨嘴炮")),
    HeroData("孙尚香", "大小姐驾到", "射手", listOf("翻滚突袭", "究极弩炮")),
    HeroData("亚瑟", "圣骑之力", "战士/坦克", listOf("誓约之盾", "圣剑裁决")),
    HeroData("后羿", "半神之弓", "射手", listOf("多重箭矢", "灼日之矢")),
    HeroData("安琪拉", "暗夜萝莉", "法师", listOf("混沌火种", "炽热光辉")),
    HeroData("兰陵王", "暗影猎手", "刺客", listOf("隐匿", "秘技·影袭")),
    HeroData("妲己", "魅惑之狐", "法师", listOf("灵魂冲击", "女王崇拜")),
    HeroData("铠", "破灭刀锋", "战士", listOf("回旋之刃", "不灭魔躯")),
    HeroData("花木兰", "传说之刃", "战士/刺客", listOf("空裂斩", "绽放刀锋")),
    HeroData("吕布", "无双之魔", "战士/坦克", listOf("方天画斩", "魔神降世")),
    HeroData("孙悟空", "齐天大圣", "刺客/战士", listOf("护身咒法", "如意金箍")),
    HeroData("小乔", "恋之微风", "法师", listOf("绽放之舞", "星华缭乱")),
    HeroData("王昭君", "冰雪之华", "法师", listOf("凋零冰晶", "凛冬已至")),
)

private val honorRanks = listOf(
    "倔强青铜 · Ⅲ/Ⅱ/Ⅰ",
    "秩序白银 · Ⅲ/Ⅱ/Ⅰ",
    "荣耀黄金 · Ⅳ/Ⅲ/Ⅱ/Ⅰ",
    "尊贵铂金 · Ⅳ/Ⅲ/Ⅱ/Ⅰ",
    "永恒钻石 · Ⅴ~Ⅰ",
    "至尊星耀 · Ⅴ~Ⅰ",
    "最强王者 · 0~49星",
    "荣耀王者 · 50~99星",
    "传奇王者 · 100星+",
)

private val honorQuotes = listOf(
    "稳住，我们能赢！",
    "集合，进攻主宰！",
    "发起进攻！",
    "注意，草丛有埋伏！",
    "猥琐发育，别浪！",
    "收到！",
    "干的漂亮！",
    "你强任你强，东皇加张良。",
    "没有撤退可言。",
    "心怀不惧，方能翱翔于天际。",
)

@Composable
private fun HonorDiamondIcon(
    diamondSize: Int,
    color: Color = Color(0xFFE8B84B)
) {
    Canvas(modifier = Modifier.size(diamondSize.dp)) {
        val c = center
        val s = size.minDimension * 0.42f
        val outer = Path().apply {
            moveTo(c.x, c.y - s)
            lineTo(c.x + s, c.y)
            lineTo(c.x, c.y + s)
            lineTo(c.x - s, c.y)
            close()
        }
        drawPath(outer, color = color)
        val inner = Path().apply {
            moveTo(c.x, c.y - s * 0.45f)
            lineTo(c.x + s * 0.26f, c.y)
            lineTo(c.x, c.y + s * 0.45f)
            lineTo(c.x - s * 0.26f, c.y)
            close()
        }
        drawPath(inner, color = Color(0xFF0A1220))
    }
}

@Composable
fun HonorGuideScreen() {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = MiuixScrollBehavior()
    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = colorScheme.surface,
        tint = HazeTint(colorScheme.surface.copy(0.8f))
    )

    var heroList by remember { mutableStateOf(fallbackHeroes) }
    var loading by remember { mutableStateOf(true) }
    var fromApi by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val api = HonorApi.fetchHeroList()
        if (api.isNotEmpty()) {
            heroList = api.map {
                HeroData(
                    name = it.cname,
                    title = it.title,
                    typeLabel = HonorApi.heroTypeName(it.heroType),
                    skins = HonorApi.skinList(it.skinName),
                    avatarUrl = HonorApi.heroAvatarUrl(it.ename),
                    detailUrl = HonorApi.heroDetailUrl(it.ename),
                )
            }
            fromApi = true
        }
        loading = false
    }

    var quoteIndex by remember { mutableIntStateOf((0..honorQuotes.lastIndex).random()) }
    var randomHero by remember { mutableStateOf<HeroData?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.hazeEffect(hazeState) {
                    style = hazeStyle
                    blurRadius = 30.dp
                    noiseFactor = 0f
                },
                color = Color.Transparent,
                title = "荣耀图鉴",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(24.dp))
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
                                    listOf(Color(0x33060B18), Color(0xE60A1220))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HonorDiamondIcon(22)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "王者荣耀 · 英雄图鉴",
                                color = Color(0xFFE8B84B),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (fromApi) "官方数据 · 共 ${heroList.size} 位英雄 · 点击查看详情"
                            else "共 ${heroList.size} 位英雄 · 定位一览 · 技能速查",
                            color = Color(0xFFB8C4DC),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (loading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.defaultColors(color = Color(0xFF152A4D))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HonorDiamondIcon(16)
                            Spacer(Modifier.size(10.dp))
                            Text(
                                text = "正在召唤峡谷英雄…",
                                fontSize = 14.sp,
                                color = Color(0xFFB8C4DC)
                            )
                        }
                    }
                }
            } else {
                items(heroList.size) { index ->
                    val hero = heroList[index]
                    HonorHeroCard(
                        hero = hero,
                        clickable = fromApi && hero.detailUrl != null,
                        onClick = { hero.detailUrl?.let { uriHandler.openUri(it) } }
                    )
                }
            }

            item {
                // 随机英雄 · 摇一摇
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = Color(0xFF152A4D)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HonorDiamondIcon(18)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "荣耀小工具 · 随机英雄",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE8B84B)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "摇一摇",
                                fontSize = 12.sp,
                                color = Color(0xFFE8B84B),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33E8B84B))
                                    .clickable { randomHero = heroList.random() }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        if (randomHero != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val avatar = randomHero!!.avatarUrl?.let { rememberNetworkImage(it) }
                                if (avatar != null) {
                                    Image(
                                        bitmap = avatar,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFE8B84B), Color(0xFF9C6B12))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = randomHero!!.name.take(1),
                                            color = Color(0xFF0A1220),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(Modifier.size(12.dp))
                                Column {
                                    Text(
                                        text = "${randomHero!!.name} · ${randomHero!!.title}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFFD97A)
                                    )
                                    Text(
                                        text = "${randomHero!!.typeLabel} · 皮肤 ${randomHero!!.skins.size} 款",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB8C4DC)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "点击「摇一摇」，随机召唤一位英雄",
                                fontSize = 13.sp,
                                color = Color(0xFF8A94AD)
                            )
                        }
                    }
                }
            }

            item {
                // 段位对照表
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = Color(0xFF152A4D)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HonorDiamondIcon(18)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "荣耀小工具 · 段位对照",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE8B84B)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        honorRanks.forEach { rank ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Canvas(modifier = Modifier.size(6.dp)) {
                                    drawCircle(color = Color(0xFFE8B84B))
                                }
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    text = rank,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE8EAF2)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // 每日荣耀箴言
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.defaultColors(
                        color = Color(0xFF152A4D)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HonorDiamondIcon(18)
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "荣耀小工具 · 每日箴言",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE8B84B)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "“${honorQuotes[quoteIndex]}”",
                            fontSize = 15.sp,
                            color = Color(0xFFE8EAF2)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "点击换一句",
                            fontSize = 12.sp,
                            color = Color(0xFF8A94AD),
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22E8B84B))
                                .clickable { quoteIndex = (0..honorQuotes.lastIndex).random() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HonorHeroCard(
    hero: HeroData,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (clickable) onClick else null,
        showIndication = clickable,
        pressFeedbackType = PressFeedbackType.Sink
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 英雄头像（网络图或首字徽章）
            val avatar = hero.avatarUrl?.let { rememberNetworkImage(it) }
            if (avatar != null) {
                Image(
                    bitmap = avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE8B84B), Color(0xFF9C6B12))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hero.name.take(1),
                        color = Color(0xFF0A1220),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hero.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE8B84B)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = hero.title,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = hero.typeLabel,
                    fontSize = 12.sp,
                    color = Color(0xFF8A94AD)
                )
                Spacer(Modifier.height(6.dp))
                val skins = hero.skins
                Text(
                    text = if (skins.size > 3) {
                        "皮肤 ${skins.size} 款：${skins.take(3).joinToString(" / ")} 等"
                    } else {
                        "皮肤：${skins.joinToString(" / ")}"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFFB8C4DC)
                )
            }
            if (clickable) {
                Icon(
                    imageVector = MiuixIcons.Link,
                    tint = colorScheme.onSurface,
                    contentDescription = null
                )
            }
        }
    }
}
