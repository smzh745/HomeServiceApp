package home.service.appmanage.online.work.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.TAGI

class SplashScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)


    }

    override fun onResume() {
        super.onResume()
        Log.d(TAGI, "on R")
        Handler(Looper.getMainLooper()).postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            startNewActivty(MainActivity())

        }, 4000)
    }

    override fun onBackPressed() {
        finish()

    }


}
