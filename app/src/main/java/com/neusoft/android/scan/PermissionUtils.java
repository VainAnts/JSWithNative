package com.neusoft.android.scan;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;


public class PermissionUtils {

    /**
     * 判断权限集合是否已经被申请
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean isLacksPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (isLacksPermission(context, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean isLacksPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean IsDenied = ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    permission) == PackageManager.PERMISSION_DENIED;
            return IsDenied;
        }
        return false;//默认权限是允许的
    }
}
