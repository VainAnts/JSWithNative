package com.neuhuiju.android.scan

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.neuhuiju.android.scan.Constants.REQUESTCODE
import com.neuhuiju.android.scan.Constants.REQUEST_CODE_QRCODE_PERMISSIONS
import com.neuhuiju.android.scan.Constants.SCAN_ACTIVITY_RESULT_CODE
import com.tencent.smtt.export.external.interfaces.*
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.toolbar.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    var webview: com.tencent.smtt.sdk.WebView? = null
    private var mExitTime: Long = 0
    var primary_key: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        webview = findViewById(R.id.webview)
        toolbar.title = "东本"
        val textView = toolbar.getChildAt(0) as TextView
        textView.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
        textView.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_HORIZONTAL
        webview?.apply {
            // 加载html
//            loadUrl("http://36.153.48.162:8091/neusoftEEP_web/login")
            loadUrl("file:///android_asset/web.html")
//            loadUrl("http://192.168.137.172:3008/neusoft_web/ImgTest")
            settings.javaScriptEnabled = true
            addJavascriptInterface(this@MainActivity, "android")
            //设置ChromeClient
            webChromeClient = HarlanWebChromeClient()
            webViewClient = UserClient()
            settings?.apply {
                javaScriptCanOpenWindowsAutomatically = true//设置js可以直接打开窗口，如window.open
                javaScriptEnabled = true;//是否允许JavaScript脚本运行，默认为false。设置true时，会提醒可能造成XSS漏洞
                setSupportZoom(true)//是否可以缩放，默认true
                builtInZoomControls = true//是否显示缩放按钮，默认false
                useWideViewPort = true//设置此属性，可任意比例缩放。大视图模式
                loadWithOverviewMode = true//和setUseWideViewPort(true)一起解决网页自适应问题
                setAppCacheEnabled(true)//是否使用缓存
                domStorageEnabled = true//开启本地DOM存储
                loadsImagesAutomatically = true // 加载图片
                mediaPlaybackRequiresUserGesture = false//播放音频，多媒体需要用户手动？设置为false为可自动播放
            }
//            webViewClient = WebViewClient()
        }
//        //调用js函数
//        button.setOnClickListener {
//            webview.loadUrl("javascript:javaCallJs()")
//        }
//        //调用js函数并携带参数
//        val param = "'20150102587416'"
//        button2.setOnClickListener {
//            // 传递参数调用
//            webview.loadUrl("javascript:javaCallJswithParam($param)")
//        }
    }

    //由于安全原因 需要加 @JavascriptInterface
    @JavascriptInterface
    fun scanQrcode() {
        initPermission()
    }

    /**
     * 申请本地储存权限
     */
    private fun initPermission() {
        when {
            PermissionUtils.isLacksPermissions(this, Constants.requestPermissions) -> requestCodeQRCodePermissions()
            else -> {
                //Toast.makeText(this, "权限已获取", Toast.LrequestPermissionsENGTH_SHORT).show();
                val intent = Intent(this@MainActivity, ScanActivity::class.java)
                startActivityForResult(intent, REQUESTCODE)
            }
        }
    }

    @JavascriptInterface
    fun scanQrcode(text: String) {
        //接收前端JS的传值，扫描成功之后将值再传给前端
        initPermission();
        primary_key = text
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        initPermission()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        when {
            //用户拒绝且勾选了不在询问
            EasyPermissions.somePermissionPermanentlyDenied(this, perms) ->
                AppSettingsDialog.Builder(this)
                        .setTitle("需要权限")
                        .setRationale("没有相机的权限，此功能无法正常工作。打开应用程序设置屏幕->权限，允许应用获取相机的权限")
                        .build()
                        .show()
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private fun requestCodeQRCodePermissions() {
        val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        when {
            !EasyPermissions.hasPermissions(this, *perms) -> EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和闪光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, *perms)
        }
    }

    class UserClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view?.loadUrl(request?.url?.toString())
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onReceivedSslError(p0: WebView?, handler: SslErrorHandler?, p2: SslError?) {
            // 不要使用super，否则有些手机访问不了，因为包含了一条 handler.cancel()
            // super.onReceivedSslError(view, handler, error);
            // 接受所有网站的证书，忽略SSL错误，执行访问网页
            handler?.proceed()
        }
    }

    /***
     * webChromeClient主要是将javascript中相应的方法翻译成android本地方法
     * 例如：我们重写了onJsAlert方法，那么当页面中需要弹出alert窗口时，便
     * 会执行我们的代码，按照我们的Toast的形式提示用户。
     */
    internal inner class HarlanWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            when (newProgress) {
                100 -> progressBar.visibility = View.GONE
                else -> {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }
            super.onProgressChanged(view, newProgress)
        }

        override fun onReceivedTitle(view: WebView?, str: String?) {
            toolbar.title = str
            super.onReceivedTitle(view, str)
        }

        /*此处覆盖的是javascript中的alert方法。
         *当网页需要弹出alert窗口时，会执行onJsAlert中的方法
         * 网页自身的alert方法不会被调用。
         */
        override fun onJsAlert(view: WebView, url: String, message: String,
                               result: JsResult): Boolean {
            show("onJsAlert")
            result.confirm()
            return true
        }

        /*此处覆盖的是javascript中的confirm方法。
         *当网页需要弹出confirm窗口时，会执行onJsConfirm中的方法
         * 网页自身的confirm方法不会被调用。
         */
        override fun onJsConfirm(view: WebView, url: String,
                                 message: String, result: JsResult): Boolean {
            show("onJsConfirm")
            result.confirm()
            return true
        }

        /*此处覆盖的是javascript中的confirm方法。
         *当网页需要弹出confirm窗口时，会执行onJsConfirm中的方法
         * 网页自身的confirm方法不会被调用。
         */
        override fun onJsPrompt(view: WebView, url: String,
                                message: String, defaultValue: String,
                                result: JsPromptResult): Boolean {
            show("onJsPrompt....")
            result.confirm()
            return true
        }
    }

    private fun show(warring: String) {
        Toast.makeText(this, warring, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== SCAN_ACTIVITY_RESULT_CODE){
            when (requestCode) {
                REQUESTCODE -> {
                    val result = data!!.getStringExtra("scanResult")
//                      val param = "'$result'"
//                        webview?.loadUrl("javascript:javaCallJswithParam('$result')")
                    webview?.evaluateJavascript("javascript:scanCallwithParam('$result','$primary_key')") {

                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webview?.canGoBack()!!) {
                webview?.goBack()
            } else {
                if (System.currentTimeMillis() - mExitTime > 2000) {
                    Toast.makeText(this@MainActivity, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                    mExitTime = System.currentTimeMillis()
                } else {
                    finish()
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
