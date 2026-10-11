package com.joeri.bloeddruk.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

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

    /** Downloadt de nieuwste APK van een URL (inclusief GitHub Releases redirects) en start de installatie */
    suspend fun downloadAndInstallApk(
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

            // Volg GitHub 301/302 redirects automatisch
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
                    Exception("HTTP $status - Geen release APK gevonden op $currentUrl")
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
