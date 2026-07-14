package com.example.fitness.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * GIF/图片本地缓存。
 *
 * 策略：
 * - 缓存目录: filesDir/exercises/{mediaId}.gif
 * - 首次访问：检查本地是否存在，没有就从 CDN 下载到本地
 * - 已存在：直接返回本地文件路径
 * - 下载失败：返回 null，UI 继续用远程 URL 兜底
 *
 * 后续访问不需要重新下载，可离线使用。
 */
object MediaCache {
    private const val TAG = "MediaCache"
    private const val CDN_BASE = "https://static.exercisedb.dev/media"

    /**
     * 获取动作 GIF 的本地路径（如已下载）。
     */
    fun localPath(context: Context, mediaId: String): File? {
        val file = File(context.filesDir, "exercises/$mediaId.gif")
        return if (file.exists() && file.length() > 0) file else null
    }

    /**
     * 检查 GIF 是否已下载（未在下载）。
     */
    fun isCached(context: Context, mediaId: String): Boolean =
        localPath(context, mediaId) != null

    /**
     * 异步下载并保存到本地。返回本地文件路径（成功）或 null（失败）。
     * 已有缓存则直接返回本地路径。
     */
    suspend fun downloadAndCache(context: Context, mediaId: String): File? = withContext(Dispatchers.IO) {
        // 已存在直接返回
        localPath(context, mediaId)?.let { return@withContext it }

        val url = URL("$CDN_BASE/$mediaId.gif")
        val targetDir = File(context.filesDir, "exercises")
        if (!targetDir.exists()) targetDir.mkdirs()
        val targetFile = File(targetDir, "$mediaId.gif")

        try {
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            conn.requestMethod = "GET"
            conn.connect()

            if (conn.responseCode !in 200..299) {
                Log.w(TAG, "GIF download failed for $mediaId: HTTP ${conn.responseCode}")
                conn.disconnect()
                return@withContext null
            }

            conn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            conn.disconnect()
            Log.d(TAG, "Cached GIF for $mediaId (${targetFile.length()} bytes)")
            targetFile
        } catch (e: Exception) {
            Log.w(TAG, "GIF download error for $mediaId: ${e.message}")
            // 不完整的文件删除
            if (targetFile.exists() && targetFile.length() == 0L) targetFile.delete()
            null
        }
    }

    /**
     * 已下载的 GIF 总大小（字节）。
     */
    fun cachedSize(context: Context): Long {
        val dir = File(context.filesDir, "exercises")
        if (!dir.exists()) return 0L
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * 已下载的 GIF 数量。
     */
    fun cachedCount(context: Context): Int {
        val dir = File(context.filesDir, "exercises")
        if (!dir.exists()) return 0
        return dir.listFiles()?.count { it.name.endsWith(".gif") } ?: 0
    }
}