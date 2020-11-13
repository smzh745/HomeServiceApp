package home.service.appmanage.online.work.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.adapters.TypeDetailsAdapter
import home.service.appmanage.online.work.models.TypeDetails
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.Constants.WORKER_DETAILS_SUB_URL
import kotlinx.android.synthetic.main.fragment_type_details.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class TypeDetailsSubFragment : BaseFragment() {
    private var typeDetailsList: ArrayList<TypeDetails>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            requireArguments().getString("subType")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_type_details, container, false)

        typeDetailsList = ArrayList()
        return root
    }

    override fun onResume() {
        super.onResume()
        intiData()
    }

    private fun intiData() {
        showDialog(getString(R.string.loading))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, WORKER_DETAILS_SUB_URL,
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

                        typeDetailsList!!.add(
                            TypeDetails(
                                jsonObject.getInt("sub_id"),
                                jsonObject.getString("worked_type")
                                ,
                                jsonObject.getString("description"),
                                jsonObject.getString("fare")
                                ,
                                jsonObject.getString("type"),
                                jsonObject.getString("currency")
                                , jsonObject.getString("video"), jsonObject.getInt("isVideo"),
                                jsonObject.getString("sub_type"), jsonObject.getInt("isSubtype")
                            )
                        )
                    }
                    val adapter = TypeDetailsAdapter(
                        requireActivity(), typeDetailsList!!
                    )
                    root!!.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
                    root!!.recyclerView.adapter = adapter
                    checkEmptyView()
                } else if (jsonObjects.getInt("status") == 0) {
                    Log.d(TAGI, "intiData: " + jsonObjects.getString("data"))
                    checkEmptyView()

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

                params["type"] =
                    requireArguments().getString("subType").toString()
                        .toLowerCase(Locale.getDefault())
                return params
            }
        }
        postRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            override fun retry(error: VolleyError) {
                Log.d(TAGI, "retry: " + error.message)
            }
        }
        queue!!.add(postRequest)
    }

    private fun checkEmptyView() {
        if (typeDetailsList!!.isEmpty()) {
            root!!.recyclerView.visibility = View.GONE
            root!!.emptyView.visibility = View.VISIBLE
        } else {
            root!!.recyclerView.visibility = View.VISIBLE
            root!!.emptyView.visibility = View.GONE
        }
    }

}