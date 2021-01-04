package home.service.appmanage.online.work.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.BOOK_DRIVER_URL
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.fragment_book_rider.view.*
import org.json.JSONObject
import java.util.*


@Suppress("LocalVariableName")
class BookRiderFragment : BaseFragment(), OnMapReadyCallback {
    private var dropUpLoc: String? = null
    private var picLoc: String? = null
    private var rideType: String? = null
    private var options: PolylineOptions? = null
    private lateinit var googleMap: GoogleMap
    private var sourceLoc: Location? = null
    private var descLoc: Location? = null
    private var points: ArrayList<LatLng>? = null
    private var kms: Float? = null
    private var p: Int = 0
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_book_rider, container, false)
        rideType = requireArguments().getString("type")
        dropUpLoc = requireArguments().getString("dropOfflocation")
        picLoc = requireArguments().getString("droppiclocation")

        sourceLoc = Location("")
        val splitterSource = picLoc!!.split(",")
        sourceLoc!!.latitude = splitterSource[0].toDouble()
        sourceLoc!!.longitude = splitterSource[1].toDouble()
        descLoc = Location("")
        val splitterDesc = dropUpLoc!!.split(",")
        descLoc!!.latitude = splitterDesc[0].toDouble()
        descLoc!!.longitude = splitterDesc[1].toDouble()
        Log.d(TAGI, "onCreateView: " + getTimeTaken(sourceLoc!!, descLoc!!))
        root!!.estimatedTime.text =
            "Estimated Time to reach destination: " + getTimeTaken(sourceLoc!!, descLoc!!)
        root!!.vehicleType.text = rideType
        setEstimatePrice()
        showDialog("Loading Map..")
        Handler(Looper.getMainLooper()).postDelayed({

            val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment!!.getMapAsync(this)

        }, 2000)
        options = PolylineOptions()
        points = ArrayList()
        root!!.confirmRide.setOnClickListener {
            bookRide()
        }
        return root
    }

    private fun bookRide() {
        showDialog(getString(R.string.finding_driver))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, BOOK_DRIVER_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "ok status")
                    showAlertDialog(jsonObjects.getString("data"))
                } else if (jsonObjects.getInt("status") == 0) {
                    showToast(jsonObjects.getString("data"))
                    hideDialog()
                }
                hideDialog()
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "error: " + error!!.message)
                hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["uid"] =
                    SharedPrefUtils.getStringData(requireActivity(), "id").toString()
                params["longi"] = sourceLoc!!.longitude.toString()
                params["lati"] = sourceLoc!!.latitude.toString()
                params["dlati"] = descLoc!!.latitude.toString()
                params["dlongi"] = descLoc!!.longitude.toString()
                params["type"] = rideType.toString()

                params["isOnline"] = "true"
                params["rideFare"] = p.toString()
                params["name"] =
                    SharedPrefUtils.getStringData(requireActivity(), "name").toString()
                params["deviceToken"] =
                    SharedPrefUtils.getStringData(requireActivity(), "deviceToken").toString()
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

    private fun showAlertDialog(s: String) {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        findNavController().navigate(R.id.homeFragment)
                        dialog.dismiss()
                    }
                }
            }

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setMessage(s).setPositiveButton(getString(R.string.ok), dialogClickListener)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun setEstimatePrice() {
        when {
            rideType.equals(getString(R.string.bike), true) -> {
                val rateD = getString(R.string.bike_per_km).toFloat() * kms!!
                p = (getString(R.string.start_bike).toFloat() + rateD).toInt()
                root!!.ratePrice.text = "PKR $p"
            }
            rideType.equals(getString(R.string.go_mini), true) -> {
                val rateD = getString(R.string.go_mini_km).toFloat() * kms!!
                p = (getString(R.string.start_go_mini).toFloat() + rateD).toInt()
                root!!.ratePrice.text = "PKR $p"
            }
            rideType.equals(getString(R.string.go_), true) -> {
                val rateD = getString(R.string.go_plus_km).toFloat() * kms!!
                p = (getString(R.string.go_plus_start).toFloat() + rateD).toInt()
                root!!.ratePrice.text = "PKR $p"
            }
            rideType.equals(getString(R.string.go), true) -> {
                val rateD = getString(R.string.go_per_km).toFloat() * kms!!
                p = (getString(R.string.go_start).toFloat() + rateD).toInt()
                root!!.ratePrice.text = "PKR $p"
            }
        }
    }


    private fun getTimeTaken(source: Location, dest: Location): String? {
        val meter: Float = source.distanceTo(dest)
        kms = meter / 1000
        val kms_per_min = 0.5
        val mins_taken = kms!! / kms_per_min
        val totalMinutes = mins_taken.toInt()
        Log.d(TAGI, "meter :$meter kms : $kms mins :$mins_taken")
        return if (totalMinutes < 60) {
            "$totalMinutes mins"
        } else {
            var minutes = (totalMinutes % 60).toString()
            minutes = if (minutes.length == 1) "0$minutes" else minutes
            (totalMinutes / 60).toString() + " hour " + minutes + "mins"
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0!!
        p0.mapType = GoogleMap.MAP_TYPE_NORMAL

        p0.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        sourceLoc!!.latitude,
                        sourceLoc!!.longitude
                    )
                )
                .title("PickUp Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        p0.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        descLoc!!.latitude,
                        descLoc!!.longitude
                    )
                )
                .title("DropOff Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        p0.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    sourceLoc!!.latitude,
                    sourceLoc!!.longitude
                ), 16.0f
            )
        )
        val position = LatLng(
            sourceLoc!!.latitude,
            sourceLoc!!.longitude
        )
        val position1 = LatLng(
            descLoc!!.latitude,
            descLoc!!.longitude
        )
        points?.add(position)
        points?.add(position1)
        options?.addAll(points)
        options!!.width(5F)
        if (options != null) {
            p0.addPolyline(options)
        }
        hideDialog()
    }


}