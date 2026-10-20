package com.feasycom.feasybeacon.ui.suota.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.suotalib.dialogs.SupportCustomDialogFragment
import com.feasycom.suotalib.global.SuotaProfile
import com.feasycom.suotalib.interfaces.callbacks.ISuotaManagerCallback
import com.feasycom.suotalib.suota.SuotaFile
import com.feasycom.suotalib.suota.SuotaManagerCallback
import com.feasycom.feasybeacon.ui.suota.activities.SuotaActivity
import com.feasycom.feasybeacon.ui.suota.fragments.DeviceInfoFragment
import com.feasycom.feasybeacon.ui.suota.fragments.UpdateFirmwareFragment
import java.lang.ref.WeakReference

class SuotaCallback(@NonNull private val suotaActivityRef: SuotaActivity): SuotaManagerCallback() {
    // 使用弱引用防止内存泄漏
    private val weakReferenceSuotaActivity: WeakReference<SuotaActivity> = WeakReference(suotaActivityRef)

    override fun onConnectionStateChange(newStatus: SuotaProfile.SuotaManagerStatus) {
        val suotaActivity = getSuotaActivity() ?: return
        if (newStatus == SuotaProfile.SuotaManagerStatus.DISCONNECTED) {
            // 如果OTA未完成且未失败，则结束Activity
            if (!suotaActivity.isSuotaCompleted() && !suotaActivity.isSuotaFailed()) {
                suotaActivity.finish()
            }
            val name = suotaActivity.getSuotaManager().deviceName
//            ToastUtils.showShort((name ?: "Device2") + " disconnected")
        }
    }

    override fun onServicesDiscovered() {
        LogUtils.d(TAG, "Services discovered") // 记录服务发现日志
    }

    override fun onCharacteristicRead(characteristicGroup: SuotaProfile.CharacteristicGroup, characteristic: BluetoothGattCharacteristic) {
        val suotaActivity = getSuotaActivity() ?: return

        // 检查特征值所属的特征组
        if (characteristicGroup == SuotaProfile.CharacteristicGroup.DEVICE_INFO) {
            // 如果特征组是设备信息（DEVICE_INFO），则进行以下处理

            // 查找名为 "DeviceInfoFragment" 的 Fragment
            val fragment = suotaActivity.supportFragmentManager.findFragmentByTag("DeviceInfoFragment")
            // 确保找到的 Fragment 是 DeviceInfoFragment 类型
            if (fragment is DeviceInfoFragment) {
                // 调用 DeviceInfoFragment 的 deviceInfoUpdate 方法更新设备信息
                // 将特征值的 UUID 和特征值的字符串表示作为参数传递
                fragment.deviceInfoUpdate(characteristic.uuid, String(characteristic.value))
            }
        }
    }

    override fun onDeviceInfoReadCompleted(status: SuotaProfile.DeviceInfoReadStatus) {
    }

    override fun onDeviceReady() {
        getSuotaActivity()?.dismissCustomDialog() // 关闭自定义对话框
    }

    override fun onSuotaLog(state: SuotaProfile.SuotaProtocolState, type: ISuotaManagerCallback.SuotaLogType, log: String) {
        // 更新固件更新Fragment的日志
//        getUpdateFirmwareFragment()?.updateLogCat(log)
    }

    override fun onChunkSend(chunkCount: Int, totalChunks: Int, chunk: Int, block: Int, blockChunks: Int, totalBlocks: Int) {
        // 更新固件更新Fragment的chunk和block信息
//        getUpdateFirmwareFragment()?.apply {
//            updateChunk(chunkCount, totalChunks)
//            updateBlock(block, totalBlocks)
//        }
    }

    // 更新进度条
    override fun onUploadProgress(percent: Float) {
        // 更新固件更新Fragment的上传进度
        getUpdateFirmwareFragment()?.updateProgressValue(percent.toInt())
    }

    // 升级成功
    override fun onSuccess(totalElapsedSeconds: Double, imageUploadElapsedSeconds: Double) {
        // OTA成功，更新Fragment的UI
        getUpdateFirmwareFragment()?.apply {
            makeCloseButtonVisible()
            notifySuotaFinished(totalElapsedSeconds, imageUploadElapsedSeconds)
        }
    }

    override fun onFailure(errorCode: Int) {
        LogUtils.e(TAG,"onFailure errorCode => $errorCode")
        // 获取 SuotaActivity 实例，如果获取失败则返回
        val suotaActivity = getSuotaActivity() ?: return

        // 根据错误代码处理不同的错误情况
        if (errorCode == SuotaProfile.Errors.INVALID_FIRMWARE_CRC) {
            // 如果错误代码是 INVALID_FIRMWARE_CRC（无效的固件 CRC），则显示错误对话框
            suotaActivity.displayErrorDialog(
                SuotaProfile.Errors.suotaErrorCodeList[errorCode], // 错误信息
                false // 是否显示重试按钮
            )
        } else {
            // 其他 OTA 失败情况
            // 通知 SuotaActivity OTA 失败
            suotaActivity.notifySuotaFailed()

            // 如果 Activity 没有完成或被销毁，则显示错误对话框
            if (!suotaActivity.isFinishing && !suotaActivity.isDestroyed) {
                suotaActivity.displayErrorDialog(
                    SuotaProfile.Errors.suotaErrorCodeList[errorCode], // 错误信息
                    true // 是否显示重试按钮
                )
            }
        }
    }

    override fun onRebootSent() {
        // OTA失败，显示错误对话框
//        getUpdateFirmwareFragment()?.updateLogCat("Send reboot command")
    }

    override fun updateSpeedStatistics(current: Double, max: Double, min: Double, avg: Double) {
        // 更新固件更新Fragment的速度统计信息
//        getUpdateFirmwareFragment()?.updateSpeedStatistics(current, max, min, avg)
    }

    override fun updateCurrentSpeed(currentSpeed: Double) {
        // 更新固件更新Fragment的当前速度
//        getUpdateFirmwareFragment()?.updateCurrentSpeed(currentSpeed)
    }

    override fun pendingRebootDialog(rebootDialog: SupportCustomDialogFragment) {
        val suotaActivity = getSuotaActivity() ?: return
        if (suotaActivity.hasWindowFocus()) {
            // 如果Activity有焦点，则显示重启对话框
            rebootDialog.showDialog(suotaActivity.supportFragmentManager)
        } else {
            // 否则，将对话框保存以待稍后显示
            suotaActivity.setPendingRebootDialog(rebootDialog)
        }
    }

    @Nullable
    private fun getSuotaActivity(): SuotaActivity? = weakReferenceSuotaActivity.get()

    @Nullable
    private fun getUpdateFirmwareFragment(): UpdateFirmwareFragment? {
        val suotaActivity = getSuotaActivity() ?: return null
        return suotaActivity.supportFragmentManager.findFragmentByTag("UpdateFirmwareFragment") as? UpdateFirmwareFragment
    }

    @Nullable
    private fun getSuotaFile(): SuotaFile? {
        val suotaActivity = getSuotaActivity() ?: return null
        val suotaManager = suotaActivity.getSuotaManager()
        return suotaManager.suotaFile
    }

    companion object {
        private const val TAG = "SuotaCallback"
    }
}