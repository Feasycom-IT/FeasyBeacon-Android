package com.feasycom.feasybeacon.ui.suota.global;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * 用于操作SharedPreferences的工具类。
 */
public class SharedPreferencesUtils {

    private static SharedPreferences getPreferences(@NonNull Context context) {
        // 获取SharedPreferences对象
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public static boolean fileDirectoriesCreated(@NonNull Context context) {
        // 检查文件目录是否已创建
        return getPreferences(context).getBoolean("fileDirectoriesCreated", false);
    }

    public static void setFileDirectoriesCreated(@NonNull Context context) {
        // 标记文件目录已创建
        getPreferences(context).edit()
                .putBoolean("fileDirectoriesCreated", true)
                .apply();
    }

    public static void setFirmwareDisplaySetting(@NonNull Context context, String key, boolean value) {
        // 设置固件显示设置
        getPreferences(context).edit()
                .putBoolean(key, value)
                .apply();
    }

    public static boolean getFirmwareDisplaySetting(@NonNull Context context, String key) {
        // 获取固件显示设置
        return getPreferences(context).getBoolean(key, true);
    }
}
