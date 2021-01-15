@file:Suppress("LocalVariableName")

package home.service.appmanage.online.work.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import home.service.appmanage.online.work.utils.SharedPrefUtils
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.LOGIN_DRIVER_URL
import home.service.appmanage.online.work.utils.Constants.LOGIN_USER_URL
import home.service.appmanage.online.work.utils.Constants.LOGIN_WORKER_URL
import home.service.appmanage.online.work.utils.Constants.RC_SIGN_IN
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.RequestHandler
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LoginActivity : BaseActivity() {
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        isUserLogin = intent.getBooleanExtra("isUserLogin", false)
        isDriverLogin = intent.getBooleanExtra("isDriverLogin", false)
        if (isUserLogin) {
            workerLoginlayout.visibility = View.GONE
            login_with_google.visibility = View.VISIBLE
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        login_with_google.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: Exception) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAGI, "Google sign in failed", e)
                showToast(getString(R.string.unable_to_sign_in))

            }

        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAGI, "firebaseAuthWithGoogle:" + account.id!!)
        showDialog(getString(R.string.authenticating_user))

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this@LoginActivity) { task ->
                try {
                    if (task.isSuccessful) {

                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAGI, "signInWithCredential:success")
                        val user = auth.currentUser
                        Log.d(TAGI, user?.displayName + "\n")
                        userEmail = user!!.email
                        userName = user!!.displayName
                        userProfile = user!!.photoUrl.toString()
                        Log.d(TAGI, "firebaseAuthWithGoogle: " + user?.uid)
                        loginUser()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAGI, "signInWithCredential:failure", task.exception)
                        showToast(getString(R.string.authentication_failed))
                        hideDialog()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

    }

    private fun loginDriver() {
        showDialog(getString(R.string.authenticating_user))
        val stringRequest: StringRequest =
            object :
                StringRequest(Method.POST, LOGIN_DRIVER_URL, Response.Listener { response: String ->
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
//        showDialog(getString(R.string.authenticating_user))
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
                                val intent =
                                    Intent(this@LoginActivity, RegisterActivity::class.java)
                                intent.putExtra("isUserLogin", isUserLogin)
                                intent.putExtra("isDriverLogin", false)
                                intent.putExtra("userName", userName)
                                intent.putExtra("userEmail", userEmail)
                                startActivity(intent)
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
                    params["email"] = userEmail.toString()
//                    params["password"] = password.text.toString()
                    return params
                }
            }


        RequestHandler.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext,ChooseAccountActivity::class.java))
        finish()
    }
}