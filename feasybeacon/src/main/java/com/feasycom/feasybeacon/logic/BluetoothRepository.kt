package com.feasycom.feasybeacon.logic

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import android.util.Range
import com.blankj.utilcode.util.LogUtils
import com.feasycom.bean.BeaconBean
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.bean.CommandBean
import com.feasycom.bean.FeasyBeacon
import com.feasycom.controler.beacon.FscBeaconCentralApiImp
import com.feasycom.controler.beacon.FscBeaconCentralCallbacks
import com.feasycom.feasybeacon.App
import com.feasycom.feasybeacon.logic.interfaces.FscBleCallback
import java.util.*

/**
 * BluetoothRepository 负责管理蓝牙操作和相关数据。
 */

object BluetoothRepository {

    private const val TAG: String = "BluetoothRepository"

    // 选中的蓝牙设备映射
    val selectedDeviceMap = LinkedHashMap<String, Any>()

    // 模块编号
    var mModule = 0

    // 版本范围
    var mVersionRange: Range<Int>? = null

    // 已发现的蓝牙设备列表
    var mDevices = mutableListOf<BluetoothDeviceWrapper>()

    // 用于回调的列表
    private val mCallbacks = mutableListOf<FscBleCallback>()

    // 蓝牙连接 PIN 码
    var mConnectPin = "000000"

    // 指令 Bean
    val commandBean: CommandBean = CommandBean()

    // FscBeacon 中心 API 实例
    private val mFscBeaconCentralApi by lazy {
        FscBeaconCentralApiImp.getInstance(App.sContext).apply {
            initialize()
            setCallbacks(BeaconCallbacks())
        }
    }

    fun isShowLog(isShowLog: Boolean) = mFscBeaconCentralApi.isShowLog(isShowLog)

    /**
     * 检查蓝牙是否启用
     */
    fun isBluetoothEnabled() = mFscBeaconCentralApi.isBluetoothEnabled

    /**
     * 开始扫描蓝牙设备
     */
    fun startScan() = mFscBeaconCentralApi.startScan()

    /**
     * 开始指定时间的扫描
     * @param scanTime 扫描时间
     */
    fun startScanTime(scanTime: Int) = mFscBeaconCentralApi.startScan(scanTime)

    /**
     * 停止扫描
     */
    fun stopScan() = mFscBeaconCentralApi.stopScan()

    /**
     * 判断设备是否已连接
     */
    fun isConnect() = mFscBeaconCentralApi.isConnected

    /**
     * 连接指定设备
     * @param device 蓝牙设备包装器
     * @param pin PIN 码
     */
    fun connect(device: BluetoothDeviceWrapper, pin: String) =
        mFscBeaconCentralApi.connect(device, pin)

    /**
     * 断开连接
     */
    fun disconnect(loca: String) = mFscBeaconCentralApi.disconnect(loca)

    /**
     * 判断 Beacon 信息是否完整
     */
    fun isBeaconInfoFull() = mFscBeaconCentralApi.isBeaconInfoFull

    fun isConditionBeaconInfoFull() = mFscBeaconCentralApi.isConditionBeaconInfoFUll

    /**
     * 添加 Beacon 信息
     * @param beaconBean BeaconBean 实例
     */
    fun addBeaconInfo(beaconBean: BeaconBean) = mFscBeaconCentralApi.addBeaconInfo(beaconBean)

    fun addConditionBeaconInfo(conditionBeaconBean: BeaconBean) =
        mFscBeaconCentralApi.addConditionBeaconInfo(conditionBeaconBean)

    /**
     * 删除 Beacon 信息
     * @param beaconBean BeaconBean 实例
     */
    fun deleteBeaconInfo(beaconBean: BeaconBean) {
        val isDeleteSuccess = mFscBeaconCentralApi.deleteBeaconInfo(beaconBean.index)
        LogUtils.e(TAG, "deleteBeaconInfo isDeleteSuccess => $isDeleteSuccess")
        mCallbacks.forEach { it.onDeleteBeaconInfo(beaconBean) }
    }

    fun deleteConditionBeaconInfo(beaconBean: BeaconBean) {
        val isDeleteSuccess = mFscBeaconCentralApi.deleteConditionBeaconInfo(beaconBean.index)
        LogUtils.e(TAG, "deleteConditionBeaconInfo isDeleteSuccess => $isDeleteSuccess")
        mCallbacks.forEach { it.onConditionDeleteBeaconInfo(beaconBean) }
    }

    /**
     * 设置设备名称
     * @param deviceName 设备名称
     */
    fun setDeviceName(deviceName: String) = mFscBeaconCentralApi.setDeviceName(deviceName)

    /**
     * 设置设备 PIN 码
     * @param pin PIN 码
     */
    fun setDevicePin(pin: String) = mFscBeaconCentralApi.setDevicePin(pin)

    /**
     * 设置广播间隔
     * @param intervalTime 间隔时间
     */
    fun setBroadcastInterval(intervalTime: String) =
        mFscBeaconCentralApi.setBroadcastInterval(intervalTime)

    /**
     * 恢复设备
     */
    fun restore() = mFscBeaconCentralApi.restore()

    /**
     * 查询或设置固件密钥
     */
    fun setFirmwareKey(command: String) = mFscBeaconCentralApi.setFirmwareKey(command)

    /**
     * 设置应用密钥
     */
    fun setAppKey(key: String) = mFscBeaconCentralApi.setAppKey(key)

    /**
     * 获取 Beacon 广播参数
     */
    fun beaconBroadcastParameters() = mFscBeaconCentralApi.beaconBroadcastParameters();

    /**
     * 设置 TLM 状态
     * @param isTlm 是否启用 TLM
     */
    fun setTlm(isTlm: Boolean) = mFscBeaconCentralApi.setTlm(isTlm)
    /**
     * 设置 AOA 状态
     * @param isAoa 是否启用 AOA
     */
    fun setAoa(isAoa: Boolean) = mFscBeaconCentralApi.setAoa(isAoa)

    /**
     * 设置广播扩展
     * @param isBroadcastExtensions 是否启用广播扩展
     */
    fun setBroadcastExtensions(isBroadcastExtensions: Boolean) =
        mFscBeaconCentralApi.setBroadcastExtensions(isBroadcastExtensions)

    /**
     * 检查 DFU 文件
     * @param dfuFile DFU 文件字节数组
     */
    fun checkDfuFile(dfuFile: ByteArray) = mFscBeaconCentralApi.checkDfuFile(dfuFile)!!

    /**
     * 开始 OTA 更新
     * @param dfuFile DFU 文件字节数组
     * @param reset 是否重置
     */
    fun startOtaUpdate(dfuFile: ByteArray, reset: Boolean) =
        mFscBeaconCentralApi.startOtaUpdate(dfuFile, reset)

    /**
     * 设置 Beacon 信息
     */
    fun setBeaconInfo(mBeaconBe111: ArrayList<BeaconBean>, mConditionBe111: ArrayList<BeaconBean>) =
        mFscBeaconCentralApi.saveBeaconInfo(mBeaconBe111,mConditionBe111)

    /**
     * 批量设置 Beacon 信息
     */
    fun setBatchBeaconInfo() = mFscBeaconCentralApi.saveBatch(commandBean)

    /**
     * 注册视图回调
     * @param callback 回调接口
     */
    fun registerViewCallback(callback: FscBleCallback) {
        if (mCallbacks.contains(callback)) return
        try {
            mCallbacks.add(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消注册视图回调
     * @param callback 回调接口
     */
    fun unRegisterViewCallback(callback: FscBleCallback) {
        try {
            mCallbacks.remove(callback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取设备信息
     * @param feasyBeacon FeasyBeacon 实例
     * @param isTxPower 是否为发射功率
     */
    fun getDeviceInfo(feasyBeacon: FeasyBeacon, isTxPower: Boolean) {
        mFscBeaconCentralApi.getDeviceInfo(feasyBeacon, isTxPower)
    }

    /**
     * 内部回调类，用于处理蓝牙操作的回调事件
     */
    class BeaconCallbacks : FscBeaconCentralCallbacks {

        // 开始扫描
        override fun startScan() {}

        // 停止扫描
        override fun stopScan() {}

        // 连接进度更新
        override fun connectProgressUpdate(device: BluetoothDevice?, status: Int) {
            mCallbacks.forEach { it.onConnectProgressUpdate(status) }
        }

        // 发现蓝牙外设
        override fun blePeripheralFound(
            device: BluetoothDevice,
            wrapper: BluetoothDeviceWrapper?,
            rssi: Int,
            record: ByteArray?
        ) {

            // 如果设备为空，则返回
            if (wrapper == null) return

            try {
                // 检查设备是否为 iBeacon、EddystoneBeacon 或 AltBeacon
                if (wrapper.iBeacon != null || wrapper.eddystoneBeacon != null || wrapper.altBeacon != null || wrapper.aoaBeacon!=null) {


                    notifyCallbacks { it.onBeacon(wrapper) }
                }

                // 如果设备包含监控数据
                wrapper.thSensor?.let {
                    notifyCallbacks { it.onSensor(wrapper) }
                }

                wrapper.gSensor?.let {
                    notifyCallbacks { it.onSensor(wrapper) }
                }

                // 如果设备包含 FeasyBeacon 模块数据
                wrapper.feasyBeacon?.module?.let {
                    notifyCallbacks { it.onSetting(device, wrapper) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 通知所有回调函数
        private inline fun notifyCallbacks(action: (FscBleCallback) -> Unit) {
            mCallbacks.forEach { callback ->
                try {
                    action(callback)
                } catch (e: Exception) {
                    // 捕获异常并打印
                    e.printStackTrace()
                }
            }
        }

        // 蓝牙外设已连接
        override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {
            mCallbacks.forEach { it.onConnectedSuccess(device!!) }
        }

        // 蓝牙外设断开连接
        override fun blePeripheralDisconnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {
            mCallbacks.forEach { it.onDisconnect() }
        }

        // AT 命令回调
        override fun atCommandCallBack(command: String?, param: String?, status: String?) {
            mCallbacks.forEach { it.onAtCommandCallBack(command, param, status) }
        }

        // 发现服务
        override fun servicesFound(
            gatt: BluetoothGatt?,
            device: BluetoothDevice?,
            services: ArrayList<BluetoothGattService>?
        ) {
        }

        // 服务中发现特性
        override fun characteristicForService(
            gatt: BluetoothGatt?,
            device: BluetoothDevice?,
            service: BluetoothGattService?,
            characteristic: BluetoothGattCharacteristic?
        ) {
        }

        // 接收到数据包
        override fun packetReceived(
            gatt: BluetoothGatt?,
            device: BluetoothDevice?,
            service: BluetoothGattService?,
            ch: BluetoothGattCharacteristic?,
            strValue: String,
            hexString: String,
            rawValue: ByteArray,
            timestamp: String?
        ) {
            mCallbacks.forEach { it.onPacketReceived(strValue, hexString, rawValue) }
        }

        // 接收到读取响应
        override fun readResponse(
            gatt: BluetoothGatt?,
            device: BluetoothDevice?,
            service: BluetoothGattService?,
            ch: BluetoothGattCharacteristic?,
            strValue: String?,
            hexString: String?,
            rawValue: ByteArray?,
            timestamp: String?
        ) {
        }

        // 发送数据包进度
        override fun sendPacketProgress(
            gatt: BluetoothGatt?,
            device: BluetoothDevice?,
            ch: BluetoothGattCharacteristic?,
            percentage: Int,
            sendByte: ByteArray?
        ) {
        }

        // OTA 进度更新
        override fun otaProgressUpdate(percentage: Int, status: Int) {
            mCallbacks.forEach { it.onOtaProgressUpdate(percentage) }
        }

        // 接收到设备信息
        override fun onDeviceInfoReceived(parameterName: String?, parameter: Any?) {
            mCallbacks.forEach { it.onDeviceInfo(parameterName, parameter) }
        }
    }

}