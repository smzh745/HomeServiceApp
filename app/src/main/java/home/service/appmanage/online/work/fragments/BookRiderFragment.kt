package home.service.appmanage.online.work.fragments

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.TAGI


class BookRiderFragment : BaseFragment() {
    private var dropUpLoc: String? = null
    private var picLoc: String? = null
    private var rideType: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_book_rider, container, false)
        rideType = requireArguments().getString("type")
        dropUpLoc = requireArguments().getString("dropOfflocation")
        picLoc = requireArguments().getString("droppiclocation")

        val sourceLoc = Location("")
        val splitterSource = picLoc!!.split(",")
        sourceLoc.latitude = splitterSource[0].toDouble()
        sourceLoc.longitude = splitterSource[1].toDouble()
        val descLoc = Location("")
        val splitterDesc = dropUpLoc!!.split(",")
        descLoc.latitude = splitterDesc[0].toDouble()
        descLoc.longitude = splitterDesc[1].toDouble()
        Log.d(TAGI, "onCreateView: " + getTimeTaken(sourceLoc, descLoc))

        return root
    }


    fun getTimeTaken(source: Location, dest: Location): String? {
        val meter: Float = source.distanceTo(dest)
        val kms = meter / 1000
        val kms_per_min = 0.5
        val mins_taken = kms / kms_per_min
        val totalMinutes = mins_taken.toInt()
        Log.d(TAGI, "meter :$meter kms : $kms mins :$mins_taken")
        return if (totalMinutes < 60) {
            "$totalMinutes mins"
        } else {
            var minutes = Integer.toString(totalMinutes % 60)
            minutes = if (minutes.length == 1) "0$minutes" else minutes
            (totalMinutes / 60).toString() + " hour " + minutes + "mins"
        }
    }

   
}