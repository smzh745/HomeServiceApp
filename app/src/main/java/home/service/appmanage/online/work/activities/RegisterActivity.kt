@file:Suppress("DEPRECATION", "UNUSED_ANONYMOUS_PARAMETER")

package home.service.appmanage.online.work.activities

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import home.service.appmanage.online.work.utils.SharedPrefUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants.PICK_CAM
import home.service.appmanage.online.work.utils.Constants.PICK_CAM1
import home.service.appmanage.online.work.utils.Constants.PICK_IMAGE
import home.service.appmanage.online.work.utils.Constants.PICK_IMAGE1
import home.service.appmanage.online.work.utils.Constants.REGISTER_USER
import home.service.appmanage.online.work.utils.Constants.TAGI
import home.service.appmanage.online.work.utils.Constants.WORKER_ADD_URL
import home.service.appmanage.online.work.utils.VolleyMultipartRequest
import kotlinx.android.synthetic.main.activity_login.loginTitle
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.add_image_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException


class RegisterActivity : BaseActivity() {
    private var thumbnail: Bitmap? = null
    private var thumbnailProfle: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        queue = Volley.newRequestQueue(this)
        isUserLogin = intent.getBooleanExtra("isUserLogin", false)
        if (isUserLogin) {
            loginTitle.text = getString(R.string.signup_user)
        } else {
            loginTitle.text = getString(R.string.signup_worker)
            workerType.visibility = View.VISIBLE
            imageLayout.visibility = View.VISIBLE
            imageLayoutProfile.visibility = View.VISIBLE
            cnic_number_text_input.visibility = View.VISIBLE
            setSpinner(R.array.worker_array, workerType)
            imageText.text = getString(R.string.cinic_image)
        }
        loginAccount.setOnClickListener {
            openActivity(LoginActivity(), isUserLogin,false)
        }
        register.setOnClickListener {
            if (isUserLogin) {
                registerUser()
            } else {
                registerWorker()
            }
        }

        imagePick.setOnClickListener {
            showDialog(PICK_CAM, PICK_IMAGE)
        }
        imagePickProfile.setOnClickListener {
            showDialog(PICK_CAM1, PICK_IMAGE1)

        }
    }

    private fun registerWorker() {
        val workerType: String = workerType.selectedItem.toString()

        if (number.text.isNullOrEmpty() || password1.text.isNullOrEmpty() ||
            email1.text.isNullOrEmpty() || name1.text.isNullOrEmpty() || workerType == "Worker Type" ||
            thumbnail == null || thumbnailProfle == null || cnic_number.text.isNullOrEmpty()
        ) {
            showToast(getString(R.string.choose_all_options))
        } else {
            showDialog(getString(R.string.registering_user))
            //our custom volley request
            val volleyMultipartRequest: VolleyMultipartRequest = object :
                VolleyMultipartRequest(
                    Method.POST, WORKER_ADD_URL,
                    { response ->
                        try {
                            val resultResponse = String(response.data)
                            val obj = JSONObject(resultResponse)
                            Log.d(TAGI, "registerWorker: " + obj.getString("message"))
                            showToast(obj.getString("message"))
                            openActivity(LoginActivity(),
                                isUserLogin = false,
                                isDriverLogin = false
                            )

                            finish()
                            showToast("Use the same email account for login")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        hideDialog()
                    },
                    { error ->
                        showToast(error.message.toString())
                        hideDialog()
                    }) {
                /*
                     * If you want to add more parameters with the image
                     * you can do it here
                     * here we have only one parameter with the image
                     * which is tags
                     * */
                override fun getParams(): MutableMap<String, String?> {
                    val params: MutableMap<String, String?> = HashMap()
                    params["name"] = name1.text.toString()
                    params["email"] = email1.text.toString()
                    params["password"] = password1.text.toString()
                    params["phonenum"] = number.text.toString()
                    params["type"] = workerType
                    params["cnicnumber"] = cnic_number.text.toString()
                    params["token"] =
                        SharedPrefUtils.getStringData(this@RegisterActivity, "deviceToken")
                            .toString()
                    return params
                }

                /*
                     * Here we are passing image by renaming it with a unique name
                     * */
                override fun getByteData(): MutableMap<String, DataPart> {
                    val params: MutableMap<String, DataPart> = HashMap()
                    val imagename = System.currentTimeMillis()
                    params["pic"] = DataPart("$imagename.png", getFileDataFromDrawable(thumbnail!!))
                    params["profile"] = DataPart(
                        imagename.toString() + "_profile.png",
                        getFileDataFromDrawable(thumbnailProfle!!)
                    )
                    return params
                }
            }
            volleyMultipartRequest.retryPolicy = object : RetryPolicy {
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
            //adding the request to volley
            Volley.newRequestQueue(this).add(volleyMultipartRequest)
        }
    }

    private fun getFileDataFromDrawable(bitmap: Bitmap): ByteArray? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun registerUser() {
        if (number.text.isNullOrEmpty() || password1.text.isNullOrEmpty() ||
            email1.text.isNullOrEmpty() || name1.text.isNullOrEmpty()
        ) {
            showToast(getString(R.string.please_fill_field))
        } else {
            showDialog(getString(R.string.registering_user))
            val postRequest: StringRequest = object : StringRequest(
                Method.POST, REGISTER_USER,
                Response.Listener<String?> { response ->
                    // response
                    Log.d(TAGI, response.toString())
                    val jsonObjects = JSONObject(response.toString())
                    showToast(jsonObjects.getString("data"))
                    SharedPrefUtils.saveData(
                        this@RegisterActivity,
                        "uid",
                        jsonObjects.getString("uid")
                    )
                    if (jsonObjects.getInt("status") == 1) {
                        Log.d(TAGI, "ok status")
                        openActivity(LoginActivity(), isUserLogin = true, isDriverLogin = false)

                        finish()
                        showToast("Use the same email account for login")
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
                    params["name"] = name1.text.toString()
                    params["phone"] = number.text.toString()
                    params["email"] = email1.text.toString()
                    params["password"] = password1.text.toString()
                    params["token"] =
                        SharedPrefUtils.getStringData(this@RegisterActivity, "deviceToken")
                            .toString()
                    return params
                }
            }
            queue!!.add(postRequest)
        }
    }

    //choose dialog
    private fun showDialog(cam: Int, gallery: Int) {
        val pictureDialog =
            MaterialAlertDialogBuilder(this@RegisterActivity)
        pictureDialog.setTitle(getString(R.string.select_action))
        val pictureDialogItems = arrayOf(
            getString(R.string.pick_image_gallery),
            getString(R.string.photo_from_camera)
        )
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog: DialogInterface?, which: Int ->
            when (which) {
                0 -> {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        gallery
                    )
                }
                1 -> try {
                    val intent1 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(
                        intent1,
                        cam
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        pictureDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE -> if (resultCode == Activity.RESULT_OK) {
                val path = Uri.parse(
                    data!!.data.toString()
                )
                try {
                    thumbnail = MediaStore.Images.Media.getBitmap(
                        contentResolver, path
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imagePick.setImageURI(path)
            }
            PICK_CAM -> try {
                thumbnail = data!!.extras?.get("data") as Bitmap
                imagePick.setImageBitmap(thumbnail)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            PICK_IMAGE1 -> if (resultCode == Activity.RESULT_OK) {
                val path = Uri.parse(
                    data!!.data.toString()
                )
                try {
                    thumbnailProfle = MediaStore.Images.Media.getBitmap(
                        contentResolver, path
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imagePickProfile.setImageURI(path)
            }
            PICK_CAM1 -> try {
                thumbnailProfle = data!!.extras?.get("data") as Bitmap
                imagePickProfile.setImageBitmap(thumbnailProfle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            else -> {
                Activity.RESULT_CANCELED
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}