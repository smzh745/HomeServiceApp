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
import home.service.appmanage.online.work.utils.Constants.ACCEPT_DRIVER_URL
import home.service.appmanage.online.work.utils.Constants.END_DRIVER_URL
import home.service.appmanage.online.work.utils.Constants.FETCH_USER_INFO_URL
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_accept_driver_booking.*
import kotlinx.android.synthetic.main.worker_request_layout.view.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

@SuppressLint("SetTextI18n")
class AcceptDriverBooking : BaseActivity() {
    var alertDialog2: AlertDialog? = null
    private var isStart: Boolean = false
    private var rideFare: String? = null
    private var bookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_driver_booking)
        title = getString(R.string.request_for_ride)
        if (intent.getStringExtra("type").equals("driver", true)) {

            showDriverAcceptDialog(intent.getStringExtra("data").toString())
            initView()
        }
    }

    private fun initView() {
        val itemsString = intent.getStringExtra("data").toString().split(",")
        requestName.text = itemsString[1]
        if (gpsTracker!!.canGetLocation()) {
            workDistance.text =
                roundOffDecimal(
                    distance(
                        itemsString[2].toDouble(),
                        itemsString[3].toDouble(),
                        itemsString[4].toDouble(),
                        itemsString[5].toDouble()
                    )
                ).toString() + " km"
        }
        workLoc.text = fetchLocationName(itemsString[4].toDouble(), itemsString[5].toDouble())
        yourLoc.text = fetchLocationName(itemsString[2].toDouble(), itemsString[3].toDouble())
        if (gpsTracker!!.canGetLocation()) {
            workDistance.text =
                roundOffDecimal(
                    distance(
                        itemsString[2].toDouble(),
                        itemsString[3].toDouble(),
                        itemsString[4].toDouble(),
                        itemsString[5].toDouble()
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
                endRide()
            } else {
                isStart = true
                start.text = getString(R.string.end)
            }
        }
    }

    private fun endRide() {
        showDialog(getString(R.string.ending_ride))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, END_DRIVER_URL,
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
                params["fare"] = rideFare.toString()
                params["bid"] = bookingId.toString()
                params["wid"] =
                    SharedPrefUtils.getStringData(this@AcceptDriverBooking, "id").toString()

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
            AlertDialog.Builder(this@AcceptDriverBooking)
        builder.setMessage("Work ended. Please collect " + rideFare + " from " + requestName.text.toString() + ".")
            .setPositiveButton("OK", dialogClickListener).show()
    }


    private fun showDriverAcceptDialog(values: String) {
        val itemsString = values.split(",")
        rideFare = itemsString[9]
        bookingId = itemsString[8]
        Log.d(TAGI, "showDriverAcceptDialog: $rideFare")
        val dialog = MaterialAlertDialogBuilder(
            this@AcceptDriverBooking
        )

        val layoutInflater = layoutInflater
        val view1 =
            layoutInflater.inflate(R.layout.worker_request_layout, null)
        dialog.setView(view1)
        dialog.setCancelable(false)
        view1.requestName.text = itemsString[1]
        view1.textView.text = getString(R.string.request_for_ride)
        view1!!.textView3.visibility = View.GONE
        view1.workDesp.visibility = View.GONE
        view1.textView4.text = "Ride PickUp Location"
        if (gpsTracker!!.canGetLocation()) {
            view1.workDistance.text =
                roundOffDecimal(
                    distance(
                        itemsString[2].toDouble(),
                        itemsString[3].toDouble(),
                        itemsString[4].toDouble(),
                        itemsString[5].toDouble()
                    )
                ).toString() + " km"
        }
        geocoder = Geocoder(this@AcceptDriverBooking, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses =
                geocoder!!.getFromLocation(itemsString[2].toDouble(), itemsString[3].toDouble(), 1)
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

    override fun onBackPressed() {
        Log.d(TAGI, "onBackPressed: ")
    }

    private fun acceptRequest(values: String) {
        val itemsString = values.split(",")
        showDialog(getString(R.string.accepting_request))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, ACCEPT_DRIVER_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "ok status")
                    SharedPrefUtils.saveData(this@AcceptDriverBooking, "isRideAccepted", true)
                    layout1.visibility = View.VISIBLE
                    SharedPrefUtils.saveData(this@AcceptDriverBooking, "isWorkAccepted", true)
                    showToast(jsonObjects.getString("data"))
                    alertDialog2!!.dismiss()
                    fetchUserInfo(values)
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
                params["type"] = itemsString[7]
                params["bid"] = itemsString[8]
                params["token"] = itemsString[6]
                params["name"] =
                    SharedPrefUtils.getStringData(this@AcceptDriverBooking, "name").toString()
                params["wid"] =
                    SharedPrefUtils.getStringData(this@AcceptDriverBooking, "id").toString()
                params["w_lat"] = gpsTracker!!.getLatitude().toString()
                params["w_longi"] = gpsTracker!!.getLongitude().toString()
                params["rideFare"] = rideFare.toString()
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

    private fun fetchUserInfo(values: String) {
        val itemsString = values.split(",")
        showDialog(getString(R.string.loading))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, FETCH_USER_INFO_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "ok status")
                    Log.d(TAGI, "fetchUserInfo: " + jsonObjects.getString("data"))
                    hideDialog()
                } else if (jsonObjects.getInt("status") == 0) {
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

}