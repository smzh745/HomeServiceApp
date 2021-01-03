package home.service.appmanage.online.work.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import home.service.appmanage.online.work.utils.SharedPrefUtils
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.PermissionsUtils
import kotlinx.android.synthetic.main.activity_choose_account.*

class ChooseAccountActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_account)
        if (SharedPrefUtils.getBooleanData(this@ChooseAccountActivity, "isLoggedIn")) {
            openActivity(MainActivity())
        }
        if (Build.VERSION.SDK_INT >= 23) {
            val permissionsUtils = PermissionsUtils().getInstance(this)
            if (permissionsUtils?.isAllPermissionAvailable()!!) {
                Log.d("Test", "Permission")
            } else {
                permissionsUtils.setActivity(this)
                permissionsUtils.requestPermissionsIfDenied()
            }
        }

        user1.setOnClickListener {
            openActivity(LoginActivity(), true, false)
        }
        partner.setOnClickListener {
            openActivity(LoginActivity(), false, false)

        }
        driverr.setOnClickListener {
            openActivity(LoginActivity(), false, true)

        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}