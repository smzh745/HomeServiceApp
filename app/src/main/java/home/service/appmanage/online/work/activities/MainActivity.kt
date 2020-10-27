package home.service.appmanage.online.work.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.UPLOAD_DIRECTORY
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivity : BaseActivity() {
    private var headerView: View? = null
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!SharedPrefUtils.getBooleanData(this@MainActivity, "isLoggedIn")) {
            openActivity(ChooseAccountActivity())
        }


        setSupportActionBar(toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.ratesFragment, R.id.bookingFragment,
                R.id.logout
            ), drawer_layout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
        val logout = navigationView.menu.findItem(R.id.logout);
        logout.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                SharedPrefUtils.saveData(this@MainActivity, "isLoggedIn", false)
                finish()
                openActivity(ChooseAccountActivity())
                return true
            }
        })
        headerView = navigationView.getHeaderView(0)
        if (SharedPrefUtils.getBooleanData(this, "isWorker")) {
            headerView!!.name.text = SharedPrefUtils.getStringData(this@MainActivity, "name")
            headerView!!.email.text = SharedPrefUtils.getStringData(this@MainActivity, "email")
            Glide.with(this)
                .load(UPLOAD_DIRECTORY + SharedPrefUtils.getStringData(this, "profilePic"))
                .into(headerView!!.profileImage)
        } else {
            headerView!!.name.text = SharedPrefUtils.getStringData(this@MainActivity, "name")
            headerView!!.email.text = SharedPrefUtils.getStringData(this@MainActivity, "email")
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}