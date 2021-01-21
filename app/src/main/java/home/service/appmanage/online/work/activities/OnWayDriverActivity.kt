package home.service.appmanage.online.work.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants
import kotlinx.android.synthetic.main.activity_on_way_driver.*
import org.json.JSONObject
import java.util.*

class OnWayDriverActivity : BaseActivity(), OnMapReadyCallback {

    private var options: PolylineOptions? = null
    private lateinit var googleMap: GoogleMap
    private var sourceLoc: Location? = null
    private var descLoc: Location? = null
    private var points: ArrayList<LatLng>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_way_driver)
        title = getString(R.string.on_the_way)
        if (intent.getStringExtra("type").equals("driver_accept", true)) {

            initView()
        }
        val itemsString = intent.getStringExtra("data").toString().split(",")
        fetchDriverInfo(intent.getStringExtra("data").toString())
        sourceLoc = Location("")
        sourceLoc!!.latitude = gpsTracker!!.getLatitude()
        sourceLoc!!.longitude = gpsTracker!!.getLongitude()
        descLoc = Location("")
        descLoc!!.latitude = itemsString[3].toDouble()
        descLoc!!.longitude = itemsString[4].toDouble()
        Handler(Looper.getMainLooper()).postDelayed({

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment!!.getMapAsync(this)

        }, 2000)
        options = PolylineOptions()
        points = ArrayList()
    }

    override fun onBackPressed() {
        Log.d(Constants.TAGI, "onBackPressed: ")
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        val itemsString = intent.getStringExtra("data").toString().split(",")
        requestName.text = itemsString[1]

        workLoc.text = fetchLocationName(itemsString[3].toDouble(), itemsString[4].toDouble())
        yourLoc.text = fetchLocationName(gpsTracker!!.getLatitude(), gpsTracker!!.getLongitude())
        if (gpsTracker!!.canGetLocation()) {
            workDistance.text =
                roundOffDecimal(
                    distance(
                        itemsString[3].toDouble(),
                        itemsString[4].toDouble(),
                        gpsTracker!!.getLatitude(),
                        gpsTracker!!.getLongitude()
                    )
                ).toString() + " km"
        }
        viewMap.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "http://maps.google.com/maps?saddr=" + itemsString[3].toDouble()
                            + "," + itemsString[4].toDouble() + "&daddr=" + gpsTracker!!.getLatitude() + "," + gpsTracker!!.getLongitude()
                )
            )
            startActivity(intent)
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
                .title("Your Location")
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
                .title("Driver Location")
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
//        hideDialog()
    }

    private fun fetchDriverInfo(values: String) {
        val itemsString = values.split(",")
        showDialog(getString(R.string.loading))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, Constants.FETCH_DRIVER_INFO_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(Constants.TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(Constants.TAGI, "ok status")
                    val response_data = JSONObject(response!!)
                    Log.d(Constants.TAGI, "fetchUserInfo: " + jsonObjects.getString("data"))
                    val profilePic = response_data.getJSONObject("data").getString("profilepic")
                    Glide.with(this)
                        .load(Constants.UPLOAD_DIRECTORY + profilePic)
                        .into(profileImage)

                    val phone = response_data.getJSONObject("data").getString("phone")
                    driverNum.text = phone
                    val carNum = response_data.getJSONObject("data").getString("car_no")
                    carNUm.text = carNum
                    val carColor1 = response_data.getJSONObject("data").getString("car_color")
                    carColor.text = carColor1
                    hideDialog()
                } else if (jsonObjects.getInt("status") == 0) {
                    hideDialog()
                }
//                hideDialog()
            },
            Response.ErrorListener { error -> // error
                Log.d(Constants.TAGI, "error: " + error!!.message)
                hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["uid"] = itemsString[2]
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