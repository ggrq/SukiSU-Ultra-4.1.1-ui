package com.sukisu.ultra.ui.util

import com.sukisu.ultra.ksuApp
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

/**
 * 兼容模式探测（共存版专用）：
 * 魔改版使用独立包名与原版 KernelSU 管理器共存时，本应用不是内核认证的管理器
 * （KernelSU 内核只认「包名 + APK 签名」与编译内核时内置值一致的 APK 为管理器）。
 * 此探测通过 root shell 直接调用 ksud，读取内核部署状态，让魔改版 UI 正常展示
 * 「已部署 / Root 正常 / 内核信息 / 模块列表」。
 */
data class KsuCompatInfo(
    val isRoot: Boolean = false,
    val ksudReady: Boolean = false,
    val kmi: String = "",
    val moduleCount: Int = 0,
)

suspend fun detectKsuCompat(): KsuCompatInfo = withContext(Dispatchers.IO) {
    runCatching {
        val shell = getRootShell()
        if (!shell.isRoot) return@withContext KsuCompatInfo()
        val ksudPath = ksuApp.applicationInfo.nativeLibraryDir + File.separator + "libksud.so"
        val moduleOut = ShellUtils.fastCmd(shell, "$ksudPath module list").orEmpty()
        val ksudReady = moduleOut.isNotBlank()
        val kmi = ShellUtils.fastCmd(shell, "$ksudPath boot-info current-kmi").orEmpty().trim()
        val moduleCount = runCatching { JSONArray(moduleOut).length() }.getOrDefault(0)
        KsuCompatInfo(
            isRoot = true,
            ksudReady = ksudReady,
            kmi = kmi,
            moduleCount = moduleCount,
        )
    }.getOrDefault(KsuCompatInfo())
}
