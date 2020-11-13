package home.service.appmanage.online.work.utils

object Constants {
    const val TAGI = "ServiceApp"
    const val ROOT_URL = "https://doormansolutions.com/"
    const val UPLOAD_DIRECTORY = ROOT_URL + "uploads/"
    const val REGISTER_USER = ROOT_URL + "register_user.php"
    const val ADD_LOCATION_USER = ROOT_URL + "add_location_user.php"
    const val ADD_LOCATION_WORKER = ROOT_URL + "add_location_worker.php"
    const val LOGIN_USER_URL = ROOT_URL + "user_login.php"
    const val LOGIN_WORKER_URL = ROOT_URL + "worker_login.php"
    const val UPDATE_TOKEN_URL = ROOT_URL + "update_token_login_status_worker.php"
    const val BOOK_WORKER_URL = ROOT_URL + "set_worker_booking.php"
    const val ACCEPT_WORKER_URL = ROOT_URL + "accept_worker_request.php"
    const val WORKER_DETAILS_URL = ROOT_URL + "fetch_all_workers_data.php"
    const val WORKER_DETAILS_SUB_URL = ROOT_URL + "fetch_all_worker_sub_data.php"
    const val WORKER_DETAILS_FARE_URL = ROOT_URL + "fetch_work_fare.php"
    const val END_WORK_URL = ROOT_URL + "end_work.php"
    const val CHANGE_PASS_USER_URL = ROOT_URL + "changePasswordByApp.php"
    const val CHANGE_PASS_WORKER_URL = ROOT_URL + "changePassWorker.php"
    const val FETCH_BOOKING_URL = ROOT_URL + "fetch_all_bookings_user.php"
    const val REQUEST_CHECK_SETTINGS_GPS = 0x1
    const val PICK_IMAGE = 101
    const val PICK_CAM = 102
    const val PICK_IMAGE1 = 201
    const val PICK_CAM1 = 202
    const val WORKER_ADD_PAGE = "register_worker.php?apicall="
    const val WORKER_ADD_URL = ROOT_URL + WORKER_ADD_PAGE + "uploadpic"
}