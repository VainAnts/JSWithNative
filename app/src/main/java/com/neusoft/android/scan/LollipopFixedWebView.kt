package com.neusoft.android.scan

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import com.tencent.smtt.sdk.WebView

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class LollipopFixedWebView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : WebView(context.createConfigurationContext(Configuration()), attrs, defStyleAttr)