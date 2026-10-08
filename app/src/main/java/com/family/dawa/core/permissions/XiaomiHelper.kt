package com.family.dawa.core.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object XiaomiHelper {

    fun isXiaomi(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        return manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ||
                brand.contains("xiaomi") || brand.contains("redmi") || manufacturer.contains("poco")
    }

    /**
     * Intent to open Xiaomi Autostart settings.
     */
    fun getAutostartIntent(context: Context): Intent {
        val intent = Intent()
        intent.component = ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        return if (isIntentAvailable(context, intent)) intent else getAppDetailsIntent(context)
    }

    /**
     * Intent to open Xiaomi App Permissions (for display pop-up / lock screen).
     */
    fun getMiuiPermissionsIntent(context: Context): Intent {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
            setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
            putExtra("extra_pkgname", context.packageName)
        }
        return if (isIntentAvailable(context, intent)) intent else getAppDetailsIntent(context)
    }

    fun getAppDetailsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
        return resolveInfos.isNotEmpty()
    }
}
