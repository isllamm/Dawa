package com.family.dawa.core.permissions

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

data class HealthCheckItem(
    val id: String,
    val title: String,
    val description: String,
    val isOk: Boolean,
    val fixActionLabel: String = "إصلاح الإذن",
    val fixIntent: Intent? = null
)

object PermissionChecker {

    fun checkHealth(context: Context): List<HealthCheckItem> {
        val items = mutableListOf<HealthCheckItem>()

        // 1. Notifications
        val notifsOk = NotificationManagerCompat.from(context).areNotificationsEnabled()
        items.add(
            HealthCheckItem(
                id = "notifications",
                title = "إشعارات التطبيق",
                description = if (notifsOk) "مسموح بها" else "التطبيق يحتاج إذن إرسال الإشعارات لتنبيه المواعيد",
                isOk = notifsOk,
                fixActionLabel = "تفعيل الإشعارات",
                fixIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    XiaomiHelper.getAppDetailsIntent(context)
                }
            )
        )

        // 2. Exact Alarms
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val exactAlarmsOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
        items.add(
            HealthCheckItem(
                id = "exact_alarms",
                title = "التنبيهات في الموعد بالدقيقة",
                description = if (exactAlarmsOk) "مسموح بالتنبيه الدقيق" else "يجب منح إذن التنبيهات الدقيقة حتى يرن المنبه في نفس الدقيقة",
                isOk = exactAlarmsOk,
                fixActionLabel = "تفعيل التنبيه الدقيق",
                fixIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                } else null
            )
        )

        // 3. Full Screen Intent (Android 14+)
        val fullScreenOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.canUseFullScreenIntent() ?: true
        } else {
            true
        }
        items.add(
            HealthCheckItem(
                id = "full_screen",
                title = "الظهور فوق شاشة القفل",
                description = if (fullScreenOk) "مسموح بالظهور المباشر" else "يجب السماح للتطبيق بفتح شاشة التنبيه الكبيرة عند إغلاق الشاشة",
                isOk = fullScreenOk,
                fixActionLabel = "سماح بالظهور",
                fixIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                } else null
            )
        )

        // 4. Battery Optimization
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        items.add(
            HealthCheckItem(
                id = "battery_optimization",
                title = "توفير البطارية (بدون قيود)",
                description = if (batteryIgnored) "التطبيق معفي من إيقاف الخلفية" else "يجب استثناء التطبيق من توفير البطارية لضمان عدم إيقافه",
                isOk = batteryIgnored,
                fixActionLabel = "إلغاء قيود البطارية",
                fixIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
            )
        )

        // 5. Xiaomi Autostart (specific for Xiaomi / Redmi / Poco)
        if (XiaomiHelper.isXiaomi()) {
            items.add(
                HealthCheckItem(
                    id = "xiaomi_autostart",
                    title = "التشغيل التلقائي (شاومي / ريدمي)",
                    description = "في هواتف شاومي يجب تفعيل (التشغيل التلقائي) حتى ترن المنبهات دائماً",
                    isOk = false, // Advisory item for Xiaomi
                    fixActionLabel = "فتح إعدادات شاومي",
                    fixIntent = XiaomiHelper.getAutostartIntent(context)
                )
            )
        }

        // 6. Alarm Volume
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val alarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 1
        val volumeOk = alarmVolume > 0
        items.add(
            HealthCheckItem(
                id = "alarm_volume",
                title = "صوت المنبه في الهاتف",
                description = if (volumeOk) "مستوى صوت المنبه مرتفع" else "صوت المنبه في الهاتف مكتوم أو منخفض جداً",
                isOk = volumeOk,
                fixActionLabel = "ضبط الصوت",
                fixIntent = Intent(Settings.ACTION_SOUND_SETTINGS)
            )
        )

        return items
    }
}
