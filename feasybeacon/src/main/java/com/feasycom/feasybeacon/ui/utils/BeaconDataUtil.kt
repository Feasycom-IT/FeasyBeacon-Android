package com.feasycom.feasybeacon.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasybeacon.logic.dao.DeviceDao
import com.feasycom.feasybeacon.logic.model.*
import com.feasycom.feasybeacon.logic.network.BeaconNetwork
import com.feasycom.feasybeacon.logic.utils.putStr
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object BeaconDataUtil {

    const val TAG = "BeaconDataManager"
    private const val BEACON_DEVICE = 2
    private const val ALL_FUNCTIONS = 0

    suspend fun fetchAndStoreBeaconData(
        context: Context,
        storedHash: String,
        deviceDao: DeviceDao
    ) {
        try {
            LogUtils.e(TAG, "fetchAndStoreBeaconData storedHash => $storedHash")

            // 1.检查网络状态
            if (!isNetworkAvailable(context)) {
                LogUtils.e(
                    TAG,
                    "FeasyBeacon APP fetchAndStoreBeaconData 网络不可用，直接从本地获取数据"
                )
                handleLocalDeviceDataAssets(context, storedHash, deviceDao)
                return
            }

            val beaconParameter =
                BeaconParameter(BEACON_DEVICE, ALL_FUNCTIONS, true, 0, 100)

            LogUtils.e(TAG, "FeasyBeacon APP 开始网络请求......")

            val beaconDevices =
                BeaconNetwork.getAllBeacon(beaconParameter)


            if (beaconDevices?.data != null) {
           /*     val deviceList = beaconDevices.data.list
                val deviceDataList = mutableListOf<DeviceData>()

                for (device in deviceList) {
                    val deviceData = DeviceData(
                        beaconType = device.beaconType?:"",
                        createTime = device.createTime.toLong(),
                        defaultInterval = device.defaultInterval,
                        deviceId = device.deviceId?:"",
                        deviceType = device.deviceType,
                        funcType = device.funcType,
                        gsensorDuration = device.gsensorDuration,
                        gsensorInterval = device.gsensorInterval,
                        gsensorSensitivity = device.gsensorSensitivity,
                        id = device.id.toInt(),
                        keyDuration = device.keyDuration,
                        keyInterval = device.keyInterval,
                        modifyTime = device.modifyTime.toLong(),
                        name = device.name,
                        number = device.number,
                        txPower = device.txPower
                    )
                    deviceDataList.add(deviceData)
                }

                withContext(Dispatchers.IO) {
                    deviceDao.apply {
                        deleteAllBeacons()
                        addDevices(deviceList)
                    }
                }

                LogUtils.e(
                    TAG,
                    "FeasyBeacon APP fetchAndStoreBeaconData beaconDevices.data.hash => ${beaconDevices.data.hash}"
                )
                context.putStr("hash", beaconDevices.data.hash)

                val deviceDataWrapper = DeviceDataWrapper(
                    hash = beaconDevices.data.hash,
                    list = deviceDataList,
                    page = 0,
                    size = deviceDataList.size
                )
                val deviceJson = DeviceJson(
                    code = 200,
                    data = deviceDataWrapper,
                    msg = "Success"
                )
                val gson: Gson = GsonBuilder().setPrettyPrinting().create()
                val jsonString = gson.toJson(deviceJson)
                JsonFileManager(context).writeJsonToInternalStorage("Device.json", jsonString)
                LogUtils.d(
                    TAG,
                    "FeasyBeacon APP fetchAndStoreBeaconData 服务器获取的数据成功写入缓存文件..."
                )*/
            } else {
                LogUtils.e(TAG, "FeasyBeacon APP fetchAndStoreBeaconData 服务器返回数据为空或异常")
                if (beaconDevices != null) {
                    LogUtils.e(
                        TAG,
                        "FeasyBeacon APP fetchAndStoreBeaconData 响应状态: code=${beaconDevices.code}, msg=${beaconDevices.msg}"
                    )
                }
                LogUtils.e(TAG, "FeasyBeacon APP fetchAndStoreBeaconData 从缓存里面获取数据...")
                handleLocalDeviceDataCache(context, storedHash, deviceDao)
            }
        } catch (e: Exception) {

            LogUtils.e(TAG, "FeasyBeacon APP fetchAndStoreBeaconData 异常堆栈:", e)

            handleLocalDeviceDataAssets(context, storedHash, deviceDao)
        }
    }

    // 5. 添加网络状态检查方法
    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        } catch (e: Exception) {
            LogUtils.e(TAG, "FeasyBeacon APP isNetworkAvailable 检查网络状态异常: ${e.message}")
            false
        }
    }

    private fun handleLocalDeviceDataCache(
        context: Context,
        storedHash: String,
        deviceDao: DeviceDao
    ) {
        try {
            val devices = JsonFileManager(context).parseLocalDeviceData("Device.json")
            devices?.data?.let { data ->
                LogUtils.d(
                    TAG,
                    "FeasyBeacon APP handleLocalDeviceDataCache 111111111111111111111111111"
                )
                deviceDao.apply {
                    deleteAllBeacons()
                    addDevices(data.list)
                }
                context.putStr("hash", data.hash)
            }
        } catch (e: Exception) {
            LogUtils.e(
                TAG,
                "FeasyBeacon APP handleLocalDeviceDataCache e => ${e.printStackTrace()}"
            )
        }
    }

    private fun handleLocalDeviceDataAssets(
        context: Context,
        storedHash: String,
        deviceDao: DeviceDao
    ) {
        val devices = parseLocalDeviceData(context, "Device.json")
        devices?.data?.let { data ->
            LogUtils.d(
                TAG,
                "FeasyBeacon APP handleLocalDeviceDataAssets 111111111111111111111111111"
            )
            deviceDao.apply {
                deleteAllBeacons()
                addDevices(data.list)
            }
            context.putStr("hash", data.hash)
        }
    }

    private fun parseLocalDeviceData(context: Context, fileName: String): Devices? {
        return Gson().fromJson(readJsonFromFile(context, fileName), Devices::class.java)
    }

    private fun readJsonFromFile(context: Context, fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            LogUtils.e(TAG, "FeasyBeacon APP readJsonFromFile Error reading file: $fileName", e)
            ""
        }
    }

}
