package home.service.appmanage.online.work.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE


class HelperUtils {
    fun isForeground(context: Context, myPackage: String): Boolean {
        val manager =
            context.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val runningTaskInfo = manager!!.getRunningTasks(1)
        val componentInfo = runningTaskInfo[0].topActivity
        return componentInfo!!.packageName == myPackage
    }
}