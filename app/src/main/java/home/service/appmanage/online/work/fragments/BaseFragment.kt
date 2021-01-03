package home.service.appmanage.online.work.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.FETCH_WORKER_FARE_URL
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.GPSTracker
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.layout_loading_dialog.view.*
import org.json.JSONObject
import java.util.*

open class BaseFragment : Fragment() {
    var root: View? = null
    private var dialog: AlertDialog? = null
    var geocoder: Geocoder? = null
    var gpsTracker: GPSTracker? = null

    var queue: RequestQueue? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queue = Volley.newRequestQueue(requireActivity())


    }
    fun buildAlertMessageNoGps() {
        val builder =
            MaterialAlertDialogBuilder(requireActivity())
        builder.setMessage(getString(R.string.gps_seems_to_disable))
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialog: DialogInterface?, id: Int ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(
                getString(R.string.no)
            ) { dialog: DialogInterface, id: Int ->
                dialog.cancel()
                findNavController().navigateUp()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onResume() {
        super.onResume()
        if (SharedPrefUtils.getBooleanData(requireActivity(), "isLoggedIn")) {
            if (SharedPrefUtils.getBooleanData(requireActivity(), "isWorker")) {
                loadWorkerFare()
            }
        }
    }

    private fun loadWorkerFare() {
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, FETCH_WORKER_FARE_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "ok status")
                    val jsonObj = JSONObject(jsonObjects.getString("data"))
                    Log.d(TAGI, "loadWorkerFare: " + jsonObj.getString("totalFare"))
                    SharedPrefUtils.saveData(
                        requireContext(),
                        "totalFare",
                        jsonObj.getString("totalFare")
                    )

                } else {
                    SharedPrefUtils.saveData(
                        requireContext(),
                        "totalFare",
                        "0"
                    )
                }
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "error: " + error!!.message)
                hideDialog()

            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()

                params["wid"] =
                    SharedPrefUtils.getStringData(requireActivity(), "id").toString()
                return params
            }
        }
        queue!!.add(postRequest)
    }

    fun showToast(toast: String) {
        Toast.makeText(requireActivity(), toast, Toast.LENGTH_LONG).show()

    }

    //TODO: show dialog
    fun showDialog(message: String) {
        dialog = setProgressDialog(requireActivity(), message)
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

    fun navigateFragmentbyType(id: Int, value: String) {
        val bundle = bundleOf("type" to value)
        findNavController().navigate(id, bundle)

    }
}