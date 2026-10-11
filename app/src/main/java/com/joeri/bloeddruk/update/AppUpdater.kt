package com.joeri.bloeddruk.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class GitHubReleaseInfo(
    val tagName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object AppUpdater {

    /** Controleert of de app toestemming heeft om APK's te installeren (Android 8+) */
    fun canInstallApk(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /** Opent de systeeminstellingen om 'Onbekende apps installeren' toe te staan */
    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /** Controleert op de GitHub API of er een nieuwere release aanwezig is op het account jokke1987/bloeddruk */
    suspend fun checkForUpdates(context: Context): GitHubReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/jokke1987/bloeddruk/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonText)
                val tagName = json.optString("tag_name", "")
                val body = json.optString("body", "Nieuwe versie beschikbaar op GitHub.")

                var downloadUrl = "https://github.com/jokke1987/bloeddruk/releases/latest/download/app-debug.apk"
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk")) {
                            downloadUrl = asset.optString("browser_download_url", downloadUrl)
                            break
                        }
                    }
                }

                if (tagName.isNotBlank()) {
                    return@withContext GitHubReleaseInfo(tagName, downloadUrl, body)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    /** Downloadt de nieuwste APK van een URL (inclusief automatische fallback tussen app-debug.apk en app-release.apk) */
    suspend fun downloadAndInstallApk(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val primaryResult = tryDownload(context, apkUrl, onProgress)
        if (primaryResult.isSuccess) return@withContext primaryResult

        // Slimme automatische fallback als het bestand app-debug.apk of app-release.apk heet op GitHub
        val fallbackUrl = when {
            apkUrl.contains("app-release.apk") -> apkUrl.replace("app-release.apk", "app-debug.apk")
            apkUrl.contains("app-debug.apk") -> apkUrl.replace("app-debug.apk", "app-release.apk")
            else -> null
        }

        if (fallbackUrl != null) {
            val fallbackResult = tryDownload(context, fallbackUrl, onProgress)
            if (fallbackResult.isSuccess) return@withContext fallbackResult
        }

        return@withContext primaryResult
    }

    private suspend fun tryDownload(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = apkUrl
            var connection = URL(currentUrl).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            var status = connection.responseCode
            var redirectCount = 0

            while ((status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == HttpURLConnection.HTTP_SEE_OTHER ||
                        status == 307 || status == 308) && redirectCount < 5
            ) {
                val newUrl = connection.getHeaderField("Location")
                connection.disconnect()
                if (newUrl.isNullOrBlank()) break
                currentUrl = newUrl
                connection = URL(currentUrl).openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()
                status = connection.responseCode
                redirectCount++
            }

            if (status != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(
                    Exception("HTTP $status - Geen bestand op $currentUrl")
                )
            }

            val fileLength = connection.contentLength
            val outputFile = File(context.cacheDir, "update.apk")
            if (outputFile.exists()) outputFile.delete()

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                if (fileLength > 0) {
                    val progress = ((totalBytesRead * 100) / fileLength).toInt()
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                launchAppInstaller(context, outputFile)
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /** Start de Android installatiewizard met de gedownloade APK */
    fun launchAppInstaller(context: Context, apkFile: File) {
        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }
}
