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
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import home.service.appmanage.online.work.R
import home.service.appmanage.online.work.utils.Constants
import home.service.appmanage.online.work.utils.Constants.PICK_CAM
import home.service.appmanage.online.work.utils.Constants.PICK_CAM1
import home.service.appmanage.online.work.utils.Constants.PICK_CAM2
import home.service.appmanage.online.work.utils.Constants.PICK_CAM3
import home.service.appmanage.online.work.utils.Constants.PICK_IMAGE
import home.service.appmanage.online.work.utils.Constants.PICK_IMAGE1
import home.service.appmanage.online.work.utils.Constants.PICK_IMAGE2
import home.service.appmanage.online.work.utils.Constants.PICK_IMAGE3
import home.service.appmanage.online.work.utils.SharedPrefUtils
import home.service.appmanage.online.work.utils.VolleyMultipartRequest
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register_driver.*
import kotlinx.android.synthetic.main.activity_register_driver.cnic_number
import kotlinx.android.synthetic.main.activity_register_driver.cnic_number_text_input
import kotlinx.android.synthetic.main.activity_register_driver.email1
import kotlinx.android.synthetic.main.activity_register_driver.imagePickProfile
import kotlinx.android.synthetic.main.activity_register_driver.loginAccount
import kotlinx.android.synthetic.main.activity_register_driver.name1
import kotlinx.android.synthetic.main.activity_register_driver.number
import kotlinx.android.synthetic.main.activity_register_driver.password1
import kotlinx.android.synthetic.main.activity_register_driver.register
import kotlinx.android.synthetic.main.activity_register_driver.workerType
import kotlinx.android.synthetic.main.add_image_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

@Suppress("DEPRECATION", "UNUSED_ANONYMOUS_PARAMETER")
class RegisterDriverActivity : BaseActivity() {
    private var thumbnail: Bitmap? = null
    private var thumbnailCar: Bitmap? = null
    private var thumbnailLic: Bitmap? = null
    private var thumbnailProfle: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_driver)
        workerType.visibility = View.VISIBLE
        cnic_number_text_input.visibility = View.VISIBLE
        setSpinner(R.array.driver_array, workerType)
        loginAccount!!.setOnClickListener {
            openActivity(LoginActivity(), isUserLogin = false, isDriverLogin = true)
        }
        register.setOnClickListener {
            registerDriver1()
        }
        imageCnic.setOnClickListener {
            showDialog(PICK_CAM, PICK_IMAGE)
        }
        imagePickProfile.setOnClickListener {
            showDialog(PICK_CAM1, PICK_IMAGE1)

        }
        imageCarCopy.setOnClickListener {
            showDialog(PICK_CAM2, PICK_IMAGE2)

        }
        imageLic.setOnClickListener {
            showDialog(
                PICK_CAM3, PICK_IMAGE3
            )

        }
    }

    //choose dialog
    private fun showDialog(cam: Int, gallery: Int) {
        val pictureDialog =
            MaterialAlertDialogBuilder(this@RegisterDriverActivity)
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

    private fun registerDriver1() {
        val workerType: String = workerType.selectedItem.toString()

        if (number.text.isNullOrEmpty() || password1.text.isNullOrEmpty() ||
            email1.text.isNullOrEmpty() || name1.text.isNullOrEmpty() || workerType == "Driver Type" ||
            thumbnail == null || thumbnailProfle == null || cnic_number.text.isNullOrEmpty()
        ) {
            showToast(getString(R.string.choose_all_options))
        } else {
            showDialog(getString(R.string.registering_user))
            //our custom volley request
            val volleyMultipartRequest: VolleyMultipartRequest = object :
                VolleyMultipartRequest(
                    Method.POST, Constants.DRIVER_ADD_URL,
                    { response ->
                        try {
                            val resultResponse = String(response.data)
                            val obj = JSONObject(resultResponse)
                            Log.d(Constants.TAGI, "registerWorker: " + obj.getString("message"))
                            showToast(obj.getString("message"))
                            openActivity(
                                LoginActivity(),
                                isUserLogin = false,
                                isDriverLogin = true
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
                    params["dage"] = age1.text.toString()
                    params["father_name"] = fatherName.text.toString()
                    params["address"] = addrss.text.toString()
                    params["car_color"] = car_colot.text.toString()
                    params["car_no"] = car_no.text.toString()
                    params["car_engine_no"] = car_eng.text.toString()
                    params["lic_no"] = lic_no.text.toString()
                    params["token"] =
                        SharedPrefUtils.getStringData(this@RegisterDriverActivity, "deviceToken")
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
                    params["car_copy_image"] = DataPart(
                        imagename.toString() + "_car_copy_image.png",
                        getFileDataFromDrawable(thumbnailCar!!)
                    )
                    params["lic_image"] = DataPart(
                        imagename.toString() + "_lic_image.png",
                        getFileDataFromDrawable(thumbnailLic!!)
                    )
                    return params
                }
            }
            volleyMultipartRequest.retryPolicy = object : RetryPolicy {
                override fun getCurrentTimeout(): Int {
                    return 100000
                }

                override fun getCurrentRetryCount(): Int {
                    return 100000
                }

                override fun retry(error: VolleyError) {
                    Log.d(Constants.TAGI, "retry: " + error.message)
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
                imageCnic.setImageURI(path)
            }
            PICK_CAM -> try {
                thumbnail = data!!.extras?.get("data") as Bitmap
                imageCnic.setImageBitmap(thumbnail)
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
            PICK_IMAGE2 -> if (resultCode == Activity.RESULT_OK) {
                val path = Uri.parse(
                    data!!.data.toString()
                )
                try {
                    thumbnailCar = MediaStore.Images.Media.getBitmap(
                        contentResolver, path
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imageCarCopy.setImageURI(path)
            }
            PICK_CAM2 -> try {
                thumbnailCar = data!!.extras?.get("data") as Bitmap
                imageCarCopy.setImageBitmap(thumbnailCar)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            PICK_IMAGE3 -> if (resultCode == Activity.RESULT_OK) {
                val path = Uri.parse(
                    data!!.data.toString()
                )
                try {
                    thumbnailLic = MediaStore.Images.Media.getBitmap(
                        contentResolver, path
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                imageLic.setImageURI(path)
            }
            PICK_CAM3 -> try {
                thumbnailLic = data!!.extras?.get("data") as Bitmap
                imageLic.setImageBitmap(thumbnailLic)
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