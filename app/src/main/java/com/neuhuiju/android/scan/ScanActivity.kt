package com.neuhuiju.android.scan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.neuhuiju.android.scan.Constants.SCAN_ACTIVITY_RESULT_CODE

import com.neusoft.qrcode.core.QRCodeView
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.activity_scan.view.*
import kotlinx.android.synthetic.main.toolbar.*


class ScanActivity : AppCompatActivity(), QRCodeView.Delegate {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        toolbar.title = "扫描二维码"
        var textView = toolbar.getChildAt(0) as TextView
        textView.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        textView.gravity = Gravity.CENTER_HORIZONTAL
    }

    override fun onStart() {
        super.onStart()
        zxingview.apply {
            setDelegate(this@ScanActivity)
            startCamera() // 打开后置摄像头开始预览，但是并未开始识别
//            startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
            startSpotAndShowRect() // 显示扫描框，并开始识别
            scanBoxView.isOnlyDecodeScanBoxArea = true // 仅识别扫描框中的码
//            scanBoxView.isOnlyDecodeScanBoxArea = false; // 识别整个屏幕中的码
        }
    }

    override fun onStop() {
        zxingview.stopCamera() // 关闭摄像头预览，并且隐藏扫描框
        super.onStop()
    }

    override fun onDestroy() {
        zxingview.onDestroy() // 销毁二维码扫描控件
        super.onDestroy()
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(200)
    }

    override fun onScanQRCodeSuccess(result: String) {
        Log.i(TAG, "result:$result")
        title = "扫描结果为：$result"
        vibrate()
        intent = Intent().apply { putExtra("scanResult", result) }
        setResult(SCAN_ACTIVITY_RESULT_CODE, intent)
        finish()
    }

    override fun onCameraAmbientBrightnessChanged(isDark: Boolean) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        var tipText = zxingview.scanBoxView.tipText
        val ambientBrightnessTip = "\n环境过暗，请打开闪光灯"
        if (isDark) {
            zxingview.openFlashlight()
            when {
                !tipText.contains(ambientBrightnessTip) -> {
                    zxingview.scanBoxView.tipText = tipText + ambientBrightnessTip
                }
            }
        } else {
            when {
                tipText.contains(ambientBrightnessTip) -> {
                    tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip))
                    zxingview.scanBoxView.tipText = tipText
                }
            }
        }
    }

    override fun onScanQRCodeOpenCameraError() {
        Toast.makeText(this, "打开相机出错", Toast.LENGTH_SHORT).show()
    }

    fun onClick(v: View) {
        when (v.id) {
            //            case R.id.start_spot:
            //                zxingview.startSpot(); // 开始识别
            //                break;
            //            case R.id.stop_spot:
            //                zxingview.stopSpot(); // 停止识别
            //                break;
            //            case R.id.start_spot_showrect:
            //                zxingview.startSpotAndShowRect(); // 显示扫描框，并且开始识别
            //                break;
            //            case R.id.stop_spot_hiddenrect:
            //                zxingview.stopSpotAndHiddenRect(); // 停止识别，并且隐藏扫描框
            //                break;
            //            case R.id.decode_scan_box_area:
            //                zxingview.getScanBoxView().setOnlyDecodeScanBoxArea(true); // 仅识别扫描框中的码
            //                break;
            //            case R.id.decode_full_screen_area:
            //                zxingview.getScanBoxView().setOnlyDecodeScanBoxArea(false); // 识别整个屏幕中的码
            //                break;
            R.id.open_flashlight -> zxingview.openFlashlight() // 打开闪光灯
            R.id.close_flashlight -> zxingview.closeFlashlight() // 关闭闪光灯
        }
    }

    companion object {
        private val TAG = ScanActivity::class.java.simpleName
    }
}