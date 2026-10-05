package com.pakhshmahdi.app.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pakhshmahdi.app.MainActivity
import com.pakhshmahdi.app.core.AppConfig
import com.pakhshmahdi.app.data.local.LocalStore
import com.pakhshmahdi.app.data.remote.ApiClient
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class OrderStatusWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        LocalStore.init(applicationContext)
        val token = LocalStore.loadAuthToken()?.takeIf { it.isNotBlank() }
            ?: return Result.success()

        val orders = try {
            ApiClient.api.orders("Bearer $token").items
        } catch (error: HttpException) {
            return if (error.code() == 401 || error.code() == 403) {
                LocalStore.clearAccountSession()
                Result.success()
            } else {
                Result.retry()
            }
        } catch (_: Throwable) {
            return Result.retry()
        }

        val current = orders.associate { it.id to normalizeStatus(it.status) }
        val previous = LocalStore.loadOrderStatuses()

        if (previous.isNotEmpty()) {
            orders.forEach { order ->
                val status = normalizeStatus(order.status)
                val old = previous[order.id]
                if (old != null && old != status) {
                    showStatusNotification(order.id, status)
                }
            }
        }

        LocalStore.saveOrderStatuses(current)
        return Result.success()
    }

    private fun showStatusNotification(orderId: Long, status: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_ORDER_ID, orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            orderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle(orderId, status))
            .setContentText(notificationBody(status))
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody(status)))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(orderId.hashCode(), notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "وضعیت سفارش‌های ${AppConfig.APP_NAME}",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "اعلان تغییر وضعیت سفارش‌های ${AppConfig.APP_NAME}"
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private val CHANNEL_ID: String get() = "orders_${AppConfig.CLIENT_ID}"
        private val UNIQUE_WORK: String get() = "order_status_sync_${AppConfig.CLIENT_ID}"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<OrderStatusWorker>(
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}

private fun normalizeStatus(status: String): String =
    status.removePrefix("wc-").trim().lowercase()

private fun notificationTitle(orderId: Long, status: String): String = when (status) {
    "processing" -> "سفارش #$orderId در حال پردازش است"
    "completed" -> "سفارش #$orderId تکمیل شد"
    "on-hold" -> "سفارش #$orderId در انتظار بررسی است"
    "pending" -> "سفارش #$orderId در انتظار پرداخت است"
    "cancelled" -> "سفارش #$orderId لغو شد"
    "refunded" -> "سفارش #$orderId مرجوع شد"
    "failed" -> "پرداخت سفارش #$orderId ناموفق بود"
    else -> "وضعیت سفارش #$orderId تغییر کرد"
}

private fun notificationBody(status: String): String = when (status) {
    "processing" -> "سفارش شما ثبت شده و در حال آماده‌سازی است."
    "completed" -> "فرآیند سفارش تکمیل شده است."
    "on-hold" -> "سفارش برای بررسی فروشگاه نگه داشته شده است."
    "pending" -> "برای تکمیل خرید، وضعیت پرداخت را بررسی کنید."
    "cancelled" -> "این سفارش لغو شده است."
    "refunded" -> "وضعیت سفارش به مرجوع‌شده تغییر کرده است."
    "failed" -> "پرداخت انجام نشده یا ناموفق بوده است."
    else -> "برای مشاهده جزئیات، برنامه ${AppConfig.APP_NAME} را باز کنید."
}
