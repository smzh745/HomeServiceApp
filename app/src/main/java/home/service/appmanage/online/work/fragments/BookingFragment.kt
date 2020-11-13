package home.service.appmanage.online.work.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.models.Booking
import home.service.appmanage.online.work.utils.Constants.FETCH_BOOKING_URL
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.SharedPrefUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class BookingFragment : BaseFragment() {
    private var bookList: ArrayList<Booking>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_booking, container, false)
        bookList = ArrayList()
        return root
    }

    override fun onResume() {
        super.onResume()
        bookList!!.clear()
        intiData()
    }

    private fun intiData() {
        showDialog(getString(R.string.loading))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, FETCH_BOOKING_URL,
            Response.Listener<String?> { response ->
                // response
                Log.d(TAGI, response.toString())
                val jsonObjects = JSONObject(response.toString())

                if (jsonObjects.getInt("status") == 1) {
                    Log.d(TAGI, "ok status")

                    val jsonArray =
                        JSONArray(jsonObjects.getString("data"))
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        bookList!!.add(
                            Booking(
                                jsonObject.getString("book_id"),
                                jsonObject.getString("u_lat"),
                                jsonObject.getString("u_longi"),
                                jsonObject.getString("booked_at"),
                                jsonObject.getString("fare"),
                                jsonObject.getString("currency"),
                                jsonObject.getString("type"),
                                jsonObject.getString("sub_type"),
                                jsonObject.getString("worked_type")
                            )
                        )

                    }

                } else if (jsonObjects.getInt("status") == 0) {
                    Log.d(TAGI, "intiData: " + jsonObjects.getString("data"))

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
                return params
            }
        }
        queue!!.add(postRequest)
    }

}