package home.service.appmanage.online.work.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants
import kotlinx.android.synthetic.main.activity_on_way_driver.*

class OnWayDriverActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_way_driver)
        title = getString(R.string.on_the_way)
        if (intent.getStringExtra("type").equals("driver_accept", true)) {

            initView()
        }
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
}