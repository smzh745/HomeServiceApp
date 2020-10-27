package home.service.appmanage.online.work.services


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.HelperUtils
import home.service.appmanage.online.work.utils.SharedPrefUtils
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val CHANNEL_ID = "my_channel_01"

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAGI, "onNewToken: $p0")
    }

    internal var name: CharSequence = "My Channel"// The user-visible name of the channel.

    @RequiresApi(Build.VERSION_CODES.N)
    internal var importance = NotificationManager.IMPORTANCE_HIGH
    private val random = Random()


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val messageTitle = remoteMessage.data["title"]
        val messageBody = remoteMessage.data["message"]
        val action = remoteMessage.data["action"]
        val data = remoteMessage.data["data"]
        Log.d(TAGI, "msg: $messageBody")
        Log.d(TAGI, "title: $messageTitle")
        Log.d(TAGI, "data: $data")
        //  sendNotificationMsg(MessageTitle.toString(), MessageBody.toString())
        if (SharedPrefUtils.getBooleanData(applicationContext, "isLoggedIn")) {
            if (messageTitle.equals("Request_Worker", true)) {
            /*    if (HelperUtils().isForeground(applicationContext, packageName)) {
                    Log.d(TAGI, "onMessageReceived fore:  ")
                    Handler(Looper.getMainLooper()).post {
                        val intent = Intent(action)
                        intent.putExtra("data", data)
                        intent.putExtra("type", "worker")
                        startActivity(intent)
                    }

                } else {*/
                    sendNotificationMsg(
                        messageBody.toString(),
                        rand(1, 100),
                        action,
                        data,
                        "worker"
                    )

//                }
            } else if (messageTitle.equals("booking_accept", true)){
                Log.d(TAGI, "onMessageReceived: accepted")
                sendNotificationMsg(
                    messageBody.toString(),
                    rand(1, 100),
                    action,
                    data,
                    "worker_accept"
                )

            }

        }

    }


    private fun sendNotificationMsg(
        body: String,
        rand: Int,
        action: String?,
        data: String?,
        type: String
    ) {
        try {
            Log.d(TAGI, "rand: $rand")
            val intent = Intent(action)
            intent.putExtra("data", data)
            intent.putExtra("type", type)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent =
                PendingIntent.getActivity(this, rand, intent, PendingIntent.FLAG_ONE_SHOT)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                mChannel.setSound(null, null)
                notificationManager.createNotificationChannel(mChannel)

                val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(body)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                notificationManager.notify(rand, mBuilder.build())
                // Turn on the screen for notification


            } else {
                val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(body)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                val mNotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(rand, mBuilder.build())


            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }


}