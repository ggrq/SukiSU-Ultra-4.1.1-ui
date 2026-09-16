package com.sukisu.ultra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.sukisu.ultra.ui.webui.MonetColorsProvider.UpdateCss
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * 王者荣耀主题色板（全局可用）
 * 以「暗夜峡谷 + 荣耀金」为核心视觉语言。
 */
object HonorPalette {
    // 基底
    val Abyss = Color(0xFF0A1220)          // 深渊黑（主背景）
    val Canyon = Color(0xFF152A4D)         // 峡谷藏青（卡片/容器）
    val CanyonLight = Color(0xFF1E3A66)    // 峡谷藏青亮
    val Midnight = Color(0xFF0D1B34)       // 暗夜蓝（深卡片）

    // 荣耀金系
    val Gold = Color(0xFFE8B84B)           // 荣耀金（主强调）
    val GoldBright = Color(0xFFFFD97A)     // 亮金（文字高亮）
    val GoldDark = Color(0xFF9C6B12)       // 暗金（渐变/边框）
    val GoldGlow = Color(0x33E8B84B)       // 金辉（光晕/淡底）

    // 辅助色
    val IceBlue = Color(0xFF7FB2E5)        // 冰霜蓝（蓝方/信息）
    val BloodRed = Color(0xFFC0392B)       // 血橙红（红方/警告）
    val JadeGreen = Color(0xFF2E8B57)      // 翡翠绿（成功）
    val NightPurple = Color(0xFF6C4BB0)    // 暗夜紫（神秘/高级）

    // 文本
    val TextPrimary = Color(0xFFE8EAF2)    // 主文字
    val TextSecondary = Color(0xFFB8C4DC)  // 次级文字
    val TextMuted = Color(0xFF8A94AD)      // 弱化文字

    // 金色渐变
    fun goldGradient(alpha: Float = 1f) = listOf(
        Color(0xFFE8B84B).copy(alpha = alpha),
        Color(0xFF9C6B12).copy(alpha = alpha)
    )

    fun canyonGradient(alpha: Float = 1f) = listOf(
        Color(0xFF1E3A66).copy(alpha = alpha),
        Color(0xFF0A1220).copy(alpha = alpha)
    )
}

@Composable
fun KernelSUTheme(
    colorMode: Int = 0,
    keyColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    // 王者荣耀主题：默认使用荣耀金作为主色
    val effectiveKeyColor = keyColor ?: HonorPalette.Gold
    val controller = when (colorMode) {
        1 -> ThemeController(ColorSchemeMode.Light)
        2 -> ThemeController(ColorSchemeMode.Dark)
        3 -> ThemeController(
            ColorSchemeMode.MonetSystem,
            keyColor = effectiveKeyColor,
            isDark = isDark
        )

        4 -> ThemeController(
            ColorSchemeMode.MonetLight,
            keyColor = effectiveKeyColor,
        )

        5 -> ThemeController(
            ColorSchemeMode.MonetDark,
            keyColor = effectiveKeyColor,
        )

        else -> ThemeController(ColorSchemeMode.System)
    }
    return MiuixTheme(
        controller = controller,
        content = {
            UpdateCss()
            content()
        }
    )
}

@Composable
@ReadOnlyComposable
fun isInDarkTheme(themeMode: Int): Boolean {
    return when (themeMode) {
        1, 4 -> false  // Force light mode
        2, 5 -> true   // Force dark mode
        else -> isSystemInDarkTheme()  // Follow system (0 or default)
    }
}
