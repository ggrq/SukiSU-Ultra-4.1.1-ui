package com.sukisu.ultra.ui.util

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

/**
 * 王者荣耀官方英雄数据（来源 pvp.qq.com 公开接口）
 * 仅供个人 UI 扩展展示使用。
 */
data class HonorHeroApi(
    val ename: Int = 0,
    val cname: String = "",
    val idName: String = "",
    val title: String = "",
    val skinName: String = "",
    val heroType: Int = 0,
    val heroType2: Int = 0,
    val roles: String = "",
)

object HonorApi {
    private const val HERO_LIST_URL = "https://pvp.qq.com/web201605/js/herolist.json"
    const val HERO_DETAIL_URL = "https://pvp.qq.com/web201605/herodetail/"
    private const val HERO_IMG_BASE = "https://game.gtimg.cn/images/yxzj/img201606/heroimg/"

    /** 英雄头像 URL */
    fun heroAvatarUrl(ename: Int): String = "$HERO_IMG_BASE$ename/$ename.jpg"

    /** 英雄详情页 URL */
    fun heroDetailUrl(ename: Int): String = "$HERO_DETAIL_URL$ename.shtml"

    /** 英雄类型名 */
    fun heroTypeName(type: Int): String = when (type) {
        1 -> "战士"
        2 -> "法师"
        3 -> "坦克"
        4 -> "刺客"
        5 -> "射手"
        6 -> "辅助"
        else -> "英雄"
    }

    /** 皮肤列表（skin_name 以 | 分隔） */
    fun skinList(skinName: String): List<String> =
        skinName.split("|").map { it.trim() }.filter { it.isNotEmpty() }

    /** 拉取官方英雄列表（UTF-8 优先，乱码回退 GBK） */
    suspend fun fetchHeroList(): List<HonorHeroApi> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = downloadBytes(HERO_LIST_URL)
            val text = decodeJsonText(bytes)
            val arr = JSONArray(text)
            val list = ArrayList<HonorHeroApi>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val hero = HonorHeroApi(
                    ename = o.optInt("ename", 0),
                    cname = o.optString("cname", ""),
                    idName = o.optString("id_name", ""),
                    title = o.optString("title", ""),
                    skinName = o.optString("skin_name", ""),
                    heroType = o.optInt("hero_type", 0),
                    heroType2 = o.optInt("hero_type2", 0),
                    roles = o.optString("roles", ""),
                )
                if (hero.ename > 0 && hero.cname.isNotBlank()) list.add(hero)
            }
            list
        }.getOrElse { emptyList() }
    }

    private fun downloadBytes(url: String): ByteArray {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 10000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) KernelSU-Honor")
            setRequestProperty("Referer", "https://pvp.qq.com/")
        }
        return try {
            conn.inputStream.use { it.readBytes() }
        } finally {
            conn.disconnect()
        }
    }

    private fun decodeJsonText(bytes: ByteArray): String {
        // 优先 UTF-8；若含替换符 U+FFFD 说明编码不符，回退 GBK
        val utf8 = String(bytes, Charsets.UTF_8)
        return if (utf8.contains('\uFFFD')) {
            String(bytes, Charset.forName("GBK"))
        } else {
            utf8
        }
    }
}

/** 简易网络图片加载（HttpURLConnection + BitmapFactory，无额外依赖） */
@Composable
fun rememberNetworkImage(url: String?): ImageBitmap? {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(url) {
        if (url == null) return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) KernelSU-Honor")
                    setRequestProperty("Referer", "https://pvp.qq.com/")
                }
                try {
                    conn.inputStream.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                } finally {
                    conn.disconnect()
                }
            }.getOrNull()
        }
    }
    return bitmap
}
