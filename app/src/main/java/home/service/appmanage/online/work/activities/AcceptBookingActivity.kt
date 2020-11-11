@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package home.service.appmanage.online.work.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.ACCEPT_WORKER_URL
import home.service.appmanage.online.work.utils.Constants.END_WORK_URL
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.Constants.WORKER_DETAILS_FARE_URL
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_accept_booking.*
import kotlinx.android.synthetic.main.worker_request_layout.view.*
import org.json.JSONObject
import java.io.IOException
import java.util.*


@SuppressLint("SetTextI18n")
class AcceptBookingActivity : BaseActivity() {
    var alertDialog2: AlertDialog? = null
    private var isStart: Boolean = false
    private var fare1: String? = null
    private var bookId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_booking)
        title = getString(R.string.request_for_work)
        if (intent.getStringExtra("type").equals("worker", true)) {

            showWorkerAcceptDialog(intent.getStringExtra("data").toString())
            initView()
        }
    }

    private fun initView() {
        val itemsString = intent.getStringExtra("data").toString().split(",")
        fetchFare(itemsString[7])
        requestName.text = itemsString[2]
        workDesp.text = "A user has requested " + itemsString[6] + " to be resolve"
        if (gpsTracker!!.canGetLocation()) {
            workDistance.text =
                roundOffDecimal(
                    distance(
                        itemsString[1].toDouble(),
                        itemsString[3].toDouble(),
                        gpsTracker!!.getLatitude(),
                        gpsTracker!!.getLongitude()
                    )
                ).toString() + " km"
        }
        workLoc.text = fetchLocationName(itemsString[1].toDouble(), itemsString[3].toDouble())
        yourLoc.text = fetchLocationName(gpsTracker!!.getLatitude(), gpsTracker!!.getLongitude())
        if (gpsTracker!!.canGetLocation()) {
            workDistance.text =
                roundOffDecimal(
                    distance(
                        itemsString[1].toDouble(),
                        itemsString[3].toDouble(),
                        gpsTracker!!.getLatitude(),
                        gpsTracker!!.getLongitude()
                    )
                ).toString() + " km"
        }
        viewMap.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "http://maps.google.com/maps?saddr=" + itemsString[1].toDouble()
                            + "," + itemsString[3].toDouble() + "&daddr=" + gpsTracker!!.getLatitude() + "," + gpsTracker!!.getLongitude()
                )
            )
            startActivity(intent)
        }
        start.setOnClickListener {
            if (isStart) {
                endWork()
            } else {
                isStart = true
                start.text = getString(R.string.end)
            }
        }
    }

    private fun endWork() {
        showDialog(getString(R.string.ending_work))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, END_WORK_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "ok status")
                    showToast(jsonObjects.getString("data"))
                    hideDialog()
                    endWOrkDialog()
                } else if (jsonObjects.getInt("status") == 0) {
                    showToast(jsonObjects.getString("data"))
                    hideDialog()
                }
//                hideDialog()
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "error: " + error!!.message)
                hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["fare"] = fare1.toString()
                params["bid"] = bookId.toString()
                params["wid"] =
                    SharedPrefUtils.getStringData(this@AcceptBookingActivity, "id").toString()

                return params
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue!!.add(postRequest)
    }

    private fun endWOrkDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialog.dismiss()

                        openActivity(MainActivity())
                        finish()
                    }

                }
            }

        val builder =
            AlertDialog.Builder(this@AcceptBookingActivity)
        builder.setMessage("Work ended. Please collect " + fare1 + " from " + requestName.text.toString() + ".")
            .setPositiveButton("OK", dialogClickListener).show()
    }


    override fun onBackPressed() {
        Log.d(TAGI, "onBackPressed: ")
    }

    private fun showWorkerAcceptDialog(values: String) {
        val itemsString = values.split(",")
        val dialog = MaterialAlertDialogBuilder(
            this@AcceptBookingActivity
        )

        val layoutInflater = layoutInflater
        val view1 =
            layoutInflater.inflate(R.layout.worker_request_layout, null)
        dialog.setView(view1)
        dialog.setCancelable(false)
        view1.requestName.text = itemsString[2]
        view1.workDesp.text = "A user has requested " + itemsString[6] + " to be resolve"
        if (gpsTracker!!.canGetLocation()) {
            view1.workDistance.text =
                roundOffDecimal(
                    distance(
                        itemsString[1].toDouble(),
                        itemsString[3].toDouble(),
                        gpsTracker!!.getLatitude(),
                        gpsTracker!!.getLongitude()
                    )
                ).toString() + " km"
        }
        geocoder = Geocoder(this@AcceptBookingActivity, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses =
                geocoder!!.getFromLocation(itemsString[1].toDouble(), itemsString[3].toDouble(), 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val obj = addresses?.get(0)
        val add: String?
        if (obj != null) {
            add = obj.getAddressLine(0)
            view1.workLoc.text = add
        }
        dialog.setPositiveButton(getString(R.string.accept)) { dialogBox, id ->

            acceptRequest(values)

        }
        dialog.setNegativeButton(getString(R.string.cancel)) { dialogBox, id ->

            finish()
            dialogBox.dismiss()
        }


        alertDialog2 = dialog.create()
        alertDialog2!!.show()
    }

    private fun acceptRequest(values: String) {
        val itemsString = values.split(",")
        showDialog(getString(R.string.accepting_request))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, ACCEPT_WORKER_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "ok status")
                    layout1.visibility = View.VISIBLE
                    SharedPrefUtils.saveData(this@AcceptBookingActivity, "isWorkAccepted", true)
                    showToast(jsonObjects.getString("data"))
                    alertDialog2!!.dismiss()
                    hideDialog()
                } else if (jsonObjects.getInt("status") == 0) {
                    showToast(jsonObjects.getString("data"))
                    hideDialog()
                }
//                hideDialog()
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "error: " + error!!.message)
                hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["uid"] = itemsString[0]
                params["u_lat"] = itemsString[1]
                params["u_longi"] = itemsString[3]
                params["token"] = itemsString[4]
                params["type"] = itemsString[5]
                params["worktype"] = itemsString[6]
                params["bid"] = itemsString[8]
                bookId = itemsString[8]
                params["name"] =
                    SharedPrefUtils.getStringData(this@AcceptBookingActivity, "name").toString()
                params["wid"] =
                    SharedPrefUtils.getStringData(this@AcceptBookingActivity, "id").toString()
                params["w_lat"] = gpsTracker!!.getLatitude().toString()
                params["w_longi"] = gpsTracker!!.getLongitude().toString()

                return params
            }
        }
        postRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue!!.add(postRequest)
    }

    private fun fetchFare(s: String) {
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, WORKER_DETAILS_FARE_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "fetchFare: " + jsonObjects.getString("data"))
                    fare1 =
                        jsonObjects.getJSONObject("data").getString("fare")
                    val currency: String =
                        jsonObjects.getJSONObject("data").getString("currency")
                    fare.text = "$currency $fare1"
                } else if (jsonObjects.getInt("status") == 0) {
                    Log.d(TAGI, "fetchFare: " + jsonObjects.getString("data"))
                }
//                hideDialog()
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "error: " + error!!.message)
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["subId"] = s
                return params
            }
        }
        queue!!.add(postRequest)
    }
}