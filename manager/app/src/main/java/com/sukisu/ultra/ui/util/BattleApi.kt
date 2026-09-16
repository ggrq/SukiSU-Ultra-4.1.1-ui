package com.sukisu.ultra.ui.util

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * 峡谷战绩查询（第三方数据接口 wv.kloping.top）
 * 1. query/show?name= → 按名字查玩家（返回候选列表：UID/段位/区服/等级/头像）
 * 2. battle/history/text?uid= → 战绩历史纯文本
 * 3. user/?uid= → 服务端渲染的战绩总结大图（PNG）
 */
data class HonorPlayer(
    val uid: String,
    val name: String,
    val dw: String,
    val region: String,
    val level: String,
    val avatar: String,
)

data class HeroStat(
    val rank: Int,
    val hero: String,
    val power: Int,
    val winRate: String,
    val games: Int,
)

data class MatchDetail(
    val time: String,
    val mode: String,
    val hero: String,
    val kda: String,
    val result: String,
    val score: String,
    val mvp: Boolean = false,
    val map: String = "",
)

data class BattleSummary(
    val heroStats: List<HeroStat> = emptyList(),
    val totalGames: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRate: String = "-",
    val modeStats: List<String> = emptyList(),
    val matches: List<MatchDetail> = emptyList(),
)

object BattleApi {
    private const val BASE = "https://wv.kloping.top"
    private const val UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"

    /** 按名字查询玩家（可能返回多个区服的候选） */
    suspend fun searchPlayer(name: String): List<HonorPlayer> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "$BASE/query/show?name=${URLEncoder.encode(name.trim(), "UTF-8")}"
            val text = getText(url)
            val arr = JSONArray(text)
            val list = ArrayList<HonorPlayer>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    HonorPlayer(
                        uid = o.optString("uid", ""),
                        name = o.optString("name", ""),
                        dw = o.optString("dw", ""),
                        region = o.optString("region", ""),
                        level = o.optString("level", ""),
                        avatar = o.optString("avatar", ""),
                    )
                )
            }
            list
        }.getOrElse { emptyList() }
    }

    /** 拉取战绩历史文本 */
    suspend fun fetchBattleText(uid: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val text = getText("$BASE/battle/history/text?sid=&uid=$uid")
            text.ifBlank { null }
        }.getOrNull()
    }

    /** 拉取服务端渲染的战绩总结大图 */
    suspend fun fetchUserImage(uid: String): ImageBitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = openConn("$BASE/user/?sid=&uid=$uid")
            try {
                conn.inputStream.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            } finally {
                conn.disconnect()
            }
        }.getOrNull()
    }

    private fun getText(url: String): String {
        val conn = openConn(url)
        return try {
            conn.inputStream.use { it.readBytes() }
                .let { bytes ->
                    val utf8 = String(bytes, Charsets.UTF_8)
                    if (utf8.contains('\uFFFD')) String(bytes, Charset.forName("GBK")) else utf8
                }
        } finally {
            conn.disconnect()
        }
    }

    private fun openConn(url: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 15000
            requestMethod = "GET"
            setRequestProperty("User-Agent", UA)
            setRequestProperty("Referer", "$BASE/")
        }
    }

    /** 解析战绩历史文本为结构化数据 */
    fun parseBattleText(text: String): BattleSummary {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val heroStats = ArrayList<HeroStat>()
        val modeStats = ArrayList<String>()
        val matches = ArrayList<MatchDetail>()
        var totalGames = 0
        var wins = 0
        var losses = 0
        var winRate = "-"

        var inHeroes = false
        var inMatches = false
        for (line in lines) {
            when {
                line.startsWith("常玩英雄") -> { inHeroes = true; inMatches = false }
                line.startsWith("王者荣耀战绩") -> { inHeroes = false; inMatches = false }
                line.startsWith("逐局明细") -> { inHeroes = false; inMatches = true }
                line.startsWith("一共") -> {
                    val g = Regex("""一共(\d+)局""").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    val w = Regex("""胜利(\d+)局""").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    val l = Regex("""失败(\d+)局""").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    totalGames = g; wins = w; losses = l
                    winRate = Regex("""胜率([\d.]+)%""").find(line)?.groupValues?.get(1)?.plus("%") ?: "-"
                }
                line.contains("局，胜率") && !line.startsWith("一共") -> modeStats.add(line)
                inHeroes -> {
                    // 1. 铠 | 战力:1259 | 胜率:51.6% | 场次:1125
                    val rank = Regex("""^(\d+)\.""").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: continue
                    val hero = line.substringAfter(".").substringBefore("|").trim()
                    val power = Regex("""战力:(\d+)""").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    val wr = Regex("""胜率:([\d.]+%)""").find(line)?.groupValues?.get(1) ?: "-"
                    val games = Regex("""场次:(\d+)""").find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    heroStats.add(HeroStat(rank, hero, power, wr, games))
                }
                inMatches && line.matches(Regex("""\d+\..*""")) -> {
                    // 1. 时间:08-15 18:25 | 模式:排位赛 单排 | 英雄:米莱狄 | KDA:4/1/24 | 结果:胜利 | 评分:11.7 | 地图:排位赛
                    matches.add(parseMatchLine(line))
                }
            }
        }
        return BattleSummary(heroStats, totalGames, wins, losses, winRate, modeStats, matches)
    }

    private fun parseMatchLine(line: String): MatchDetail {
        fun grab(key: String): String {
            val idx = line.indexOf("$key:")
            if (idx < 0) return ""
            var end = line.indexOf(" |", idx)
            if (end < 0) end = line.length
            return line.substring(idx + key.length + 1, end).trim()
        }
        val mvp = line.contains("MVP:是")
        return MatchDetail(
            time = grab("时间"),
            mode = grab("模式"),
            hero = grab("英雄"),
            kda = grab("KDA"),
            result = grab("结果"),
            score = grab("评分"),
            mvp = mvp,
            map = grab("地图"),
        )
    }
}
