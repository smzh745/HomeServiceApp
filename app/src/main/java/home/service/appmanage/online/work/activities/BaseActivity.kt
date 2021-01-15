@file:Suppress("DEPRECATION")

package home.service.appmanage.online.work.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants
import home.service.appmanage.online.work.utils.Constants.CHECK_DRIVER_ACTIVE
import home.service.appmanage.online.work.utils.Constants.CHECK_WORKER_ACTIVE
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.Constants.UPDATE_TOKEN_URL
import home.service.appmanage.online.work.utils.GPSTracker
import home.service.appmanage.online.work.utils.RequestHandler
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.layout_loading_dialog.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

open class BaseActivity : AppCompatActivity() {
    var isUserLogin: Boolean = false
    var isDriverLogin: Boolean = false
    var queue: RequestQueue? = null
    private var dialog: AlertDialog? = null
    var geocoder: Geocoder? = null
    var gpsTracker: GPSTracker? = null
    lateinit var auth: FirebaseAuth
    var userName: String? = null
    var userEmail: String? = null
    var userProfile: String? = null

    //TODO: add back arrow to activity
    fun addBackArrow() {
        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    fun fetchLocationName(lati: Double, longi: Double): String {
        geocoder = Geocoder(this@BaseActivity, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses =
                geocoder!!.getFromLocation(lati, longi, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val obj = addresses?.get(0)
        var add: String? = null
        if (obj != null) {
            add = obj.getAddressLine(0)

        }
        return add.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        super.onCreate(savedInstanceState)
        gpsTracker = GPSTracker(this)
        queue = Volley.newRequestQueue(this)
        auth = FirebaseAuth.getInstance()

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAGI, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token.toString()

                // Log and toast
                Log.d(TAGI, token)
                SharedPrefUtils.saveData(this, "deviceToken", token)
            })
        if (SharedPrefUtils.getBooleanData(this@BaseActivity, "isLoggedIn")) {
            if (SharedPrefUtils.getBooleanData(this, "isWorker")) {
                updateToken("wid")

            } else if (SharedPrefUtils.getBooleanData(this, "isDriver")) {
                updateToken("did")

            } else {
                updateToken("uid")
            }
        }
    }

    private fun accountDeactiveDialog() {
        val builder =
            MaterialAlertDialogBuilder(this)
        builder.setMessage("Your account is not active. Please try again later!")
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.cancel)
            ) { dialog: DialogInterface?, id: Int ->
                finishAffinity()
            }.setNegativeButton(
                getString(R.string.logout)
            ) { dialog: DialogInterface?, id: Int ->
                SharedPrefUtils.saveData(this@BaseActivity, "isLoggedIn", false)
                SharedPrefUtils.saveData(this@BaseActivity, "isWorker", false)
                SharedPrefUtils.saveData(this@BaseActivity, "isDriver", false)
                finish()
                openActivity(ChooseAccountActivity())
            }

        val alert = builder.create()
        alert.show()
    }

    fun checkDriverActive() {
        showDialog(getString(R.string.loading))
        val stringRequest: StringRequest =
            object : StringRequest(
                Method.POST,
                CHECK_DRIVER_ACTIVE,
                Response.Listener { response: String ->
                    try {
                        try {
                            val response_data = JSONObject(response)
                            if (response_data.getString("status") == "1") {
                                Log.d(
                                    TAGI,
                                    "checkWorkerActive: " + response_data.getJSONObject("data")
                                        .getBoolean("isActivated")
                                )
                                val isActivated: Boolean =
                                    response_data.getJSONObject("data").getBoolean("isActivated")
                                SharedPrefUtils.saveData(
                                    this@BaseActivity,
                                    "isActivated",
                                    isActivated
                                )
                                hideDialog()
                                if (!isActivated) {
                                    accountDeactiveDialog()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    Log.d(TAGI, "updateToken: " + error.message)
                    try {
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params["uid"] =
                        SharedPrefUtils.getStringData(this@BaseActivity, "id").toString()
                    return params
                }
            }


        RequestHandler.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    fun checkWorkerActive() {
        showDialog(getString(R.string.loading))
        val stringRequest: StringRequest =
            object : StringRequest(
                Method.POST,
                CHECK_WORKER_ACTIVE,
                Response.Listener { response: String ->
                    try {
                        try {
                            val response_data = JSONObject(response)
                            if (response_data.getString("status") == "1") {
                                Log.d(
                                    TAGI,
                                    "checkWorkerActive: " + response_data.getJSONObject("data")
                                        .getBoolean("isActivated")
                                )
                                val isActivated: Boolean =
                                    response_data.getJSONObject("data").getBoolean("isActivated")
                                SharedPrefUtils.saveData(
                                    this@BaseActivity,
                                    "isActivated",
                                    isActivated
                                )
                                hideDialog()
                                if (!isActivated) {
                                    accountDeactiveDialog()
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    Log.d(TAGI, "updateToken: " + error.message)
                    try {
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params["uid"] =
                        SharedPrefUtils.getStringData(this@BaseActivity, "id").toString()
                    return params
                }
            }


        RequestHandler.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    //TODO: rate us
    fun rateUs() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }

    //TODO: share App
    fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + packageName + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateToken(key: String) {
        val stringRequest: StringRequest =
            object : StringRequest(
                Method.POST,
                UPDATE_TOKEN_URL,
                Response.Listener { response: String ->
                    try {
                        try {
                            val response_data = JSONObject(response)
                            if (response_data.getString("status") == "1") {
                                Log.d(TAGI, "updateToken: " + response_data.getString("data"))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    Log.d(TAGI, "updateToken: " + error.message)
                    try {
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params[key] = SharedPrefUtils.getStringData(this@BaseActivity, "id").toString()
                    params["isOnline"] = true.toString()
                    params["token"] =
                        SharedPrefUtils.getStringData(this@BaseActivity, "deviceToken")
                            .toString()
                    return params
                }
            }


        RequestHandler.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    //TODO: start activity
    fun startNewActivty(activity: Activity) {
        startActivity(Intent(this@BaseActivity, activity.javaClass))
        finish()
    }

    fun openActivity(activity: Activity, isUserLogin: Boolean, isDriverLogin: Boolean) {
        val intent = Intent(this@BaseActivity, activity.javaClass)
        intent.putExtra("isUserLogin", isUserLogin)
        intent.putExtra("isDriverLogin", isDriverLogin)
        startActivity(intent)
    }

    fun openActivity(activity: Activity) {
        val intent = Intent(this@BaseActivity, activity.javaClass)
        startActivity(intent)
    }

    //TODO:  navigate to new fragment
    fun navigateFragment(id: Int, bundle: Bundle) {
        findNavController(R.id.nav_host_fragment).navigate(id, bundle)

    }

    fun showToast(toast: String) {
        Toast.makeText(this@BaseActivity, toast, Toast.LENGTH_LONG).show()

    }

    //TODO: show dialog
    fun showDialog(message: String) {
        dialog = setProgressDialog(this, message)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    //TODO: hide dialog
    fun hideDialog() {
        if (dialog?.isShowing!!) {
            dialog?.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun setProgressDialog(context: Context, message: String): AlertDialog {

        val builder = MaterialAlertDialogBuilder(
            context
        )
        builder.setCancelable(false)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.layout_loading_dialog, null)
        builder.setView(view)

        view.dialogText.text = message
        return builder.create()
    }

    fun setSpinner(array: Int, spinner: Spinner) {
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            this@BaseActivity,
            array, android.R.layout.simple_spinner_item
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinner.adapter = adapter
    }

    fun startCountdown() {
        object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAGI, "onTick: seconds remaining: " + millisUntilFinished / 1000)
                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                hideDialog()
            }
        }.start()
    }

    fun distance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val theta = lon1 - lon2
        var dist = (Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    fun roundOffDecimal(number: Double): Double? {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toDouble()
    }

}