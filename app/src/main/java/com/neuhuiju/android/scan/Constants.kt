package com.neuhuiju.android.scan

import android.Manifest

object Constants {
    const val SCAN_ACTIVITY_RESULT_CODE = 100
    const val REQUEST_CODE_QRCODE_PERMISSIONS = 101
    const val REQUESTCODE = 102
    val requestPermissions = arrayOf(Manifest.permission.CAMERA)
}