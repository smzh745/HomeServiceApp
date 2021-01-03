@file:Suppress("LocalVariableName")

package home.service.appmanage.online.work.activities

import android.os.Bundle
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import home.service.appmanage.online.work.utils.SharedPrefUtils
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.LOGIN_DRIVER_URL
import home.service.appmanage.online.work.utils.Constants.LOGIN_USER_URL
import home.service.appmanage.online.work.utils.Constants.LOGIN_WORKER_URL
import home.service.appmanage.online.work.utils.RequestHandler
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        isUserLogin = intent.getBooleanExtra("isUserLogin", false)
        isDriverLogin = intent.getBooleanExtra("isDriverLogin", false)
        if (isUserLogin) {
            loginTitle.text = getString(R.string.login_user)
        } else {
            loginTitle.text = getString(R.string.login_partner)
        }
        if (isDriverLogin) {
            loginTitle.text = getString(R.string.login_driver)

        }
        registerAccount.setOnClickListener {
            if (isDriverLogin) {
                openActivity(RegisterDriverActivity())
            } else {
                openActivity(RegisterActivity(), isUserLogin, false)
            }
        }

        login.setOnClickListener {
            if (password.text.isNullOrEmpty() ||
                email.text.isNullOrEmpty()
            ) {
                showToast(getString(R.string.please_fill_field))
            } else {
                if (isUserLogin) {
                    loginUser()
                } else if (isDriverLogin) {
                    loginDriver()
                } else {
                    loginWorker()
                }
            }
        }
    }

    private fun loginDriver() {
        showDialog(getString(R.string.authenticating_user))
        val stringRequest: StringRequest =
            object : StringRequest(Method.POST, LOGIN_DRIVER_URL, Response.Listener { response: String ->
                    try {
                        try {
                            val response_data = JSONObject(response)
                            if (response_data.getString("status") == "1") {
                                val id: String =
                                    response_data.getJSONObject("data").getString("id")
                                val name: String =
                                    response_data.getJSONObject("data").getString("name")
                                val phone: String =
                                    response_data.getJSONObject("data").getString("phone")
                                val email: String =
                                    response_data.getJSONObject("data").getString("email")
                                val profilePic: String =
                                    response_data.getJSONObject("data").getString("profilepic")
                                val type: String =
                                    response_data.getJSONObject("data").getString("type")
                                val isActivated: Boolean =
                                    response_data.getJSONObject("data").getBoolean("isActivated")
                                SharedPrefUtils.saveData(this@LoginActivity, "id", id)
                                SharedPrefUtils.saveData(this@LoginActivity, "name", name)
                                SharedPrefUtils.saveData(this@LoginActivity, "phone", phone)
                                SharedPrefUtils.saveData(this@LoginActivity, "email", email)
                                SharedPrefUtils.saveData(
                                    this@LoginActivity,
                                    "isActivated",
                                    isActivated
                                )
                                SharedPrefUtils.saveData(
                                    this@LoginActivity,
                                    "isLoggedIn",
                                    response_data.getJSONObject("data").getBoolean("isLoggedIn")
                                )
                                SharedPrefUtils.saveData(
                                    this@LoginActivity,
                                    "profilePic",
                                    profilePic
                                )
                                SharedPrefUtils.saveData(this@LoginActivity, "type", type)
                                SharedPrefUtils.saveData(this@LoginActivity, "isDriver", true)
                                openActivity(MainActivity())
                                finish()
                            } else {
                                showToast(response_data.getString("data"))
                            }
                            hideDialog()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            hideDialog()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    try {
                        hideDialog()
                        showToast(error.message!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params["email"] = email.text.toString()
                    params["password"] = password.text.toString()
                    return params
                }
            }


        RequestHandler.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    private fun loginWorker() {
        showDialog(getString(R.string.authenticating_user))
        val stringRequest: StringRequest =
            object : StringRequest(
                Method.POST,
                LOGIN_WORKER_URL,
                Response.Listener { response: String ->
                    try {
                        try {
                            val response_data = JSONObject(response)
                            if (response_data.getString("status") == "1") {
                                val id: String =
                                    response_data.getJSONObject("data").getString("id")
                                val name: String =
                                    response_data.getJSONObject("data").getString("name")
                                val phone: String =
                                    response_data.getJSONObject("data").getString("phone")
                                val email: String =
                                    response_data.getJSONObject("data").getString("email")
                                val profilePic: String =
                                    response_data.getJSONObject("data").getString("profilepic")
                                val type: String =
                                    response_data.getJSONObject("data").getString("type")
                                val isActivated: Boolean =
                                    response_data.getJSONObject("data").getBoolean("isActivated")
                                SharedPrefUtils.saveData(this@LoginActivity, "id", id)
                                SharedPrefUtils.saveData(this@LoginActivity, "name", name)
                                SharedPrefUtils.saveData(this@LoginActivity, "phone", phone)
                                SharedPrefUtils.saveData(this@LoginActivity, "email", email)
                                SharedPrefUtils.saveData(
                                    this@LoginActivity,
                                    "isActivated",
                                    isActivated
                                )
                                SharedPrefUtils.saveData(
                                    this@LoginActivity,
                                    "isLoggedIn",
                                    response_data.getJSONObject("data").getBoolean("isLoggedIn")
                                )
                                SharedPrefUtils.saveData(
                                    this@LoginActivity,
                                    "profilePic",
                                    profilePic
                                )
                                SharedPrefUtils.saveData(this@LoginActivity, "type", type)
                                SharedPrefUtils.saveData(this@LoginActivity, "isWorker", true)
                                openActivity(MainActivity())
                                finish()
                            } else {
                                showToast(response_data.getString("data"))
                            }
                            hideDialog()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            hideDialog()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    try {
                        hideDialog()
                        showToast(error.message!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params["email"] = email.text.toString()
                    params["password"] = password.text.toString()
                    return params
                }
            }


        RequestHandler.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    private fun loginUser() {
        showDialog(getString(R.string.authenticating_user))
        val stringRequest: StringRequest =
            object : StringRequest(
                Method.POST,
                LOGIN_USER_URL,
                Response.Listener { response: String ->
                    try {
                        try {
                            val response_data = JSONObject(response)
                            if (response_data.getString("status") == "1") {
                                val id: String =
                                    response_data.getJSONObject("data").getString("id")
                                val name: String =
                                    response_data.getJSONObject("data").getString("name")
                                val phone: String =
                                    response_data.getJSONObject("data").getString("phone")
                                val email: String =
                                    response_data.getJSONObject("data").getString("email")
                                SharedPrefUtils.saveData(this@LoginActivity, "id", id)
                                SharedPrefUtils.saveData(this@LoginActivity, "name", name)
                                SharedPrefUtils.saveData(this@LoginActivity, "phone", phone)
                                SharedPrefUtils.saveData(this@LoginActivity, "email", email)
                                SharedPrefUtils.saveData(
                                    this@LoginActivity,
                                    "isLoggedIn",
                                    response_data.getJSONObject("data").getBoolean("isLoggedIn")
                                )

                                openActivity(MainActivity())
                                finish()
                            } else {
                                showToast(response_data.getString("data"))
                            }
                            hideDialog()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            hideDialog()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    try {
                        hideDialog()
                        showToast(error.message!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> =
                        HashMap()
                    params["email"] = email.text.toString()
                    params["password"] = password.text.toString()
                    return params
                }
            }


        RequestHandler.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    override fun onBackPressed() {
        finish()
    }
}