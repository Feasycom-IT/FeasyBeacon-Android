package com.feasycom.feasybeacon.logic.network;

import android.content.Context;
import android.content.pm.PackageManager;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtil {

    public interface FeedbackCallback {
        void onSuccess(String response);

        void onFailure(String error);

        void onNetworkError(String errorMessage);
    }

    public static void feedback(String feedbackContent, String feedbackType, String email, Context context, FeedbackCallback feedbackCallback) {
        // 请求网址
        String urlString = "https://feasycomm.com/api/feedback/info/add";
        new Thread(() -> {
            try {
                // 网络请求
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(params(feedbackContent, feedbackType, email, context));
                outputStream.flush();
                outputStream.close();
                int code = connection.getResponseCode();
                String msg = connection.getResponseMessage();
                LogUtils.e("SDK feedback code => " + code + "\r\n" + "msg => " + msg);
                if (code == 200) {
                    feedbackCallback.onSuccess("反馈成功");
                } else {
                    feedbackCallback.onFailure("反馈失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                feedbackCallback.onNetworkError(e.getMessage());
            }
        }).start();
    }

    /**
     * 请求参数
     *
     * @param context 当前Context
     * @return 参数的字节数组
     */
    private static byte[] params(String feedbackContent, String feedbackType, String email, Context context) {
        JSONObject rootObject = new JSONObject();
        JSONObject contentObject = new JSONObject();

        PackageManager packageManager = context.getPackageManager();
        try {
            String platform = "Android";
            android.content.pm.PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0
            );
            int labelRes = packageInfo.applicationInfo.labelRes;
            String appName = context.getResources().getString(labelRes);
            String appBoundId = context.getPackageName();
            String appVersion = packageInfo.versionName;
            String systemVersion = android.os.Build.VERSION.RELEASE;
            String deviceModel = android.os.Build.BRAND + "(" + android.os.Build.MODEL + ")";
            String type = feedbackType;
            String content = feedbackContent;

            contentObject.put("platform", platform);
            contentObject.put("appName", appName);
            contentObject.put("appBoundId", appBoundId);
            contentObject.put("appVersion", appVersion);
            contentObject.put("systemVersion", systemVersion);
            contentObject.put("deviceModel", deviceModel);
            contentObject.put("type", type);
            contentObject.put("email", email);
            contentObject.put("content", content);
            rootObject.put("content", contentObject);

            return rootObject.toString().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "".getBytes();
    }

}
