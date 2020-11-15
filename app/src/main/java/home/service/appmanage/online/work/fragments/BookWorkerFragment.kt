@file:Suppress("DEPRECATION", "UNUSED_ANONYMOUS_PARAMETER")

package home.service.appmanage.online.work.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.models.TypeDetails
import home.service.appmanage.online.work.utils.Constants
import home.service.appmanage.online.work.utils.Constants.BOOK_WORKER_URL
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.fragment_book_worker.view.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

@SuppressLint("SetTextI18n")
class BookWorkerFragment : BaseFragment(), OnMapReadyCallback, ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {
    private var mLocationMarkerText: TextView? = null
    private var mMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mCenterLatLong: LatLng? = null
    private var lat: String? = null
    private var longi: String? = null
    private var serviceList: ArrayList<TypeDetails>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_book_worker, container, false)

        return root
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        val manager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
        buildGoogleApiClient()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?

        mLocationMarkerText = root!!.findViewById(R.id.locationMarkertext)


        mapFragment?.getMapAsync(this)

        geocoder = Geocoder(requireActivity(), Locale.getDefault())

        serviceList = requireArguments().getParcelableArrayList("typed")
        val position = requireArguments().getInt("position")
        root!!.serviceName.text = serviceList!![position].typeTitle
        root!!.serviceRate.text =
            serviceList!![position].currency + " " + serviceList!![position].fare
        root!!.proceed.setOnClickListener {
            bookWorker(
                serviceList!![position].typeTitle,
                serviceList!![position].type,
                serviceList!![position].subId
            )
        }
    }

    private fun bookWorker(typeTitle: String, type: String, subId: Int) {
        showDialog(getString(R.string.finding_worker))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, BOOK_WORKER_URL,
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
                params["longi"] = longi.toString()
                params["lati"] = lat.toString()
                params["type"] = type
                params["worktype"] = typeTitle
                params["subId"] = subId.toString()
                params["isOnline"] = "true"
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

    private fun buildAlertMessageNoGps() {
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

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAGI, "OnMapReady")
        mMap = googleMap
        mMap!!.setOnCameraChangeListener { cameraPosition: CameraPosition ->
            Log.d("Camera position change" + "", cameraPosition.toString() + "")
            mCenterLatLong = cameraPosition.target
            mMap!!.clear()
            try {
                val mLocation = Location("")
                mLocation.latitude = mCenterLatLong!!.latitude
                mLocation.longitude = mCenterLatLong!!.longitude
                lat = mCenterLatLong!!.latitude.toString()
                longi = mCenterLatLong!!.longitude.toString()
                setLocationTextOnMarker(mCenterLatLong!!.latitude, mCenterLatLong!!.longitude)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val mLastLocation =
            LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient
            )
        if (mLastLocation != null) {
            changeMap(mLastLocation)
            Log.d(TAGI, "ON connected")
        } else try {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            val mLocationRequest = LocationRequest()
            mLocationRequest.interval = 10000
            mLocationRequest.fastestInterval = 5000
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(TAGI, "Connection suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onLocationChanged(location: Location?) {
        try {
            location?.let { changeMap(it) }
            LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAGI, "onConnectionFailed: ")
    }


    @Synchronized
    private fun buildGoogleApiClient() {
        try {
            mGoogleApiClient = GoogleApiClient.Builder(requireActivity())
                .enableAutoManage(requireActivity(), 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
            mGoogleApiClient!!.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onStart() {
        super.onStart()
        try {
            mGoogleApiClient!!.connect()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
                mGoogleApiClient!!.disconnect()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun changeMap(location: Location) {
        Log.d(TAGI, "Reaching map$mMap")
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        // check if map is created successfully or not
        if (mMap != null) {
            mMap!!.uiSettings.isZoomControlsEnabled = false
            val latLong = LatLng(location.latitude, location.longitude)
            val cameraPosition = CameraPosition.Builder()
                .target(latLong).zoom(19f).tilt(70f).build()
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings.isMyLocationButtonEnabled = true
            mMap!!.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(cameraPosition)
            )
            lat = location.latitude.toString()
            longi = location.longitude.toString()
            setLocationTextOnMarker(location.latitude, location.longitude)
        } else {
            showToast("Sorry! unable to create maps")
        }
    }

    private fun setLocationTextOnMarker(
        latitude: Double,
        longitude: Double
    ) {
        var addresses: List<Address>? = null
        try {
            addresses = geocoder!!.getFromLocation(latitude, longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val obj = addresses?.get(0)
        var add: String? = null
        if (obj != null) {
            add = obj.getAddressLine(0)
            SharedPrefUtils.saveData(
                requireActivity(),
                "calendarLoc",
                addresses!![0].locality
            )
        }
        mLocationMarkerText!!.text = add
    }

    private fun getMyLocation() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient!!.isConnected) {
                val permissionLocation = ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    val locationRequest = LocationRequest()
                    locationRequest.interval = 3000
                    locationRequest.fastestInterval = 3000
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                    builder.setAlwaysShow(true)
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient,
                        locationRequest,
                        this
                    )
                    val result =
                        LocationServices.SettingsApi
                            .checkLocationSettings(mGoogleApiClient, builder.build())
                    result.setResultCallback { result1: LocationSettingsResult ->
                        val status =
                            result1.status
                        when (status.statusCode) {
                            LocationSettingsStatusCodes.SUCCESS -> {
                            }
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                                 // Location settings are not satisfied.
                                // But could be fixed by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    // Ask to turn on GPS automatically
                                    status.startResolutionForResult(
                                        requireActivity(),
                                        Constants.REQUEST_CHECK_SETTINGS_GPS
                                    )
                                } catch (e: SendIntentException) {
                                    // Ignore the error.
                                }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CHECK_SETTINGS_GPS) {
            when (resultCode) {
                Activity.RESULT_OK -> getMyLocation()
                Activity.RESULT_CANCELED -> findNavController().navigateUp()
            }
        }
    }

}