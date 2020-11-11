package home.service.appmanage.online.work.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.UPLOAD_DIRECTORY
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.change_pass_layout.view.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivity : BaseActivity() {
    private var headerView: View? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private fun closeNavigationDrawer() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

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
                R.id.homeFragment,
                R.id.ratesFragment,
                R.id.bookingFragment,
                R.id.change_pass,
                R.id.logout
            ), drawer_layout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
        val logout = navigationView.menu.findItem(R.id.logout)
        val changePasword = navigationView.menu.findItem(R.id.change_pass)
        logout.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                SharedPrefUtils.saveData(this@MainActivity, "isLoggedIn", false)
                finish()
                openActivity(ChooseAccountActivity())
                return true
            }
        })
        changePasword.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                closeNavigationDrawer()
                changePasswordDialog()
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

    private fun changePasswordDialog() {
        val factory = LayoutInflater.from(this)
        @SuppressLint("InflateParams") val deleteDialogView: View =
            factory.inflate(R.layout.change_pass_layout, null)
        val deleteDialog = if (Build.VERSION.SDK_INT > 23) {

            MaterialAlertDialogBuilder(this@MainActivity).create()
        } else {
            AlertDialog.Builder(this@MainActivity).create()
        }

        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setView(deleteDialogView)
        deleteDialog.setCancelable(false)
        deleteDialogView.updateBtn.setOnClickListener {
            if (deleteDialogView.pass.text!!.isEmpty() || deleteDialogView.oldPass.text!!.isEmpty()) {
                showToast(getString(R.string.please_fill_field))
            } else {

            }
        }
        deleteDialogView.cancelBtn.setOnClickListener {
            deleteDialog.dismiss()
        }


        deleteDialog.show()
        deleteDialog.window!!.decorView.setBackgroundResource(android.R.color.transparent)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}