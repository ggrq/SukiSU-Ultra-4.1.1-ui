package com.sukisu.ultra.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
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
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical

// 王者荣耀英雄图鉴（内置静态资料，纯 UI 扩展，不影响核心功能）
private data class HonorHero(
    val name: String,
    val title: String,
    val role: String,
    val camp: String,
    val desc: String,
    val skill: String,
)

private val honorHeroes = listOf(
    HonorHero("李白", "青莲剑仙", "刺客/战士", "大唐", "十步杀一人，千里不留行。", "将进酒 / 青莲剑歌"),
    HonorHero("韩信", "国士无双", "刺客", "稷下", "暗度陈仓，偷家之王。", "背水一战 / 国士无双"),
    HonorHero("貂蝉", "绝世舞姬", "法师", "长安", "闭月之姿，乱世惊鸿。", "缘·心结 / 绽·风华"),
    HonorHero("鲁班七号", "机关造物", "射手", "稷下", "智商二百五，火力全开。", "空中支援 / 无敌鲨嘴炮"),
    HonorHero("孙尚香", "大小姐驾到", "射手", "吴", "翻滚突袭，一炮入魂。", "翻滚突袭 / 究极弩炮"),
    HonorHero("亚瑟", "圣骑之力", "战士/坦克", "圣殿", "正义的惩戒，永不后退。", "誓约之盾 / 圣剑裁决"),
    HonorHero("后羿", "半神之弓", "射手", "落日", "灼日之矢，逐日之弓。", "多重箭矢 / 灼日之矢"),
    HonorHero("安琪拉", "暗夜萝莉", "法师", "学院", "火球轰鸣，正义的魔法。", "混沌火种 / 炽热光辉"),
    HonorHero("兰陵王", "暗影猎手", "刺客", "西域", "隐身突袭，一刀毙命。", "隐匿 / 秘技·影袭"),
    HonorHero("妲己", "魅惑之狐", "法师", "稷下", "魅惑众生，秒人于无形。", "灵魂冲击 / 女王崇拜"),
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
    size: Int,
    color: Color = Color(0xFFE8B84B)
) {
    Canvas(modifier = Modifier.size(size.dp)) {
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
    val scrollBehavior = MiuixScrollBehavior()
    val hazeState = remember { HazeState() }
    val hazeStyle = HazeStyle(
        backgroundColor = colorScheme.surface,
        tint = HazeTint(colorScheme.surface.copy(0.8f))
    )
    var quoteIndex by remember { mutableIntStateOf((0..honorQuotes.lastIndex).random()) }

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
                            text = "共 ${honorHeroes.size} 位英雄 · 定位一览 · 技能速查",
                            color = Color(0xFFB8C4DC),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            items(honorHeroes.size) { index ->
                val hero = honorHeroes[index]
                HonorHeroCard(hero)
            }

            item {
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
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HonorHeroCard(hero: HonorHero) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        showIndication = false,
        pressFeedbackType = PressFeedbackType.Sink
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 英雄徽章（菱形 + 首字）
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFE8B84B), Color(0xFF9C6B12)))
                        .let { it }
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
                    text = "${hero.role} · ${hero.camp}",
                    fontSize = 12.sp,
                    color = Color(0xFF8A94AD)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = hero.desc,
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariantSummary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "技能：${hero.skill}",
                    fontSize = 12.sp,
                    color = Color(0xFFB8C4DC)
                )
            }
        }
    }
}
