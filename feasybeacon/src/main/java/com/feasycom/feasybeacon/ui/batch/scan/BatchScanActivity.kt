package com.feasycom.feasybeacon.ui.batch.scan

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivityBatchSacnBinding
import com.feasycom.feasybeacon.logic.BluetoothRepository
import com.feasycom.feasybeacon.logic.DeviceType
import com.feasycom.feasybeacon.logic.interfaces.FscBleCallback
import com.feasycom.feasybeacon.logic.model.BatchDevice
import com.feasycom.feasybeacon.logic.utils.getInt
import com.feasycom.feasybeacon.logic.utils.getStr
import com.feasycom.feasybeacon.ui.adapter.BatchDeviceAdapter
import com.feasycom.feasybeacon.ui.adapter.BatchDeviceCheckBoxAdapter
import com.feasycom.feasybeacon.ui.base.BaseActivity
import com.feasycom.feasybeacon.ui.batch.modify.BatchModifyActivity
import com.feasycom.feasybeacon.ui.filter.FilterActivity

class BatchScanActivity : BaseActivity<ActivityBatchSacnBinding>(), FscBleCallback {

    private val mDevices = mutableListOf<BatchDevice>()

    // 过滤条件：批量设备名称和RSSI
    private var mFilterBatchName: String = ""
    private var mFilterBatchRssi: Int = -100

    // 适配器：用于显示扫描到的批量设备列表
    private val mDeviceAdapter: BatchDeviceAdapter by lazy {
        BatchDeviceAdapter(mDevices).apply {}
    }

    // 适配器：用于选择批量设备
    private val mDeviceCheckBoxAdapter: BatchDeviceCheckBoxAdapter by lazy {
        BatchDeviceCheckBoxAdapter(mDevices).apply {}
    }

    override fun getViewBinding() = ActivityBatchSacnBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
        BluetoothRepository.disconnect("=======BatchScanActivity========")
    }

    override fun initView() {
        // 设置RecyclerView
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = mDeviceAdapter
        // 设置标题栏文本
        binding.headerRight.text = getString(R.string.select)
        binding.rightTv.text = getString(R.string.cancel)
        binding.headerLeft.text = getString(R.string.sort)
        binding.leftTv.text = getString(R.string.filter)
        binding.headerTitle.text = getString(R.string.unconfigured_device)
    }

    override fun initEvent() {
        // 点击标题栏左侧按钮事件处理
        binding.headerLeft.setOnClickListener {
            it as TextView
            when (it.text) {
                getString(R.string.sort) -> {
                    // 按信号强度排序
                    try {
                        mDevices.sortByDescending { batchDevice ->
                            batchDevice.device.rssi
                        }
                        mDeviceAdapter.notifyItemRangeChanged(0, mDevices.size)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                getString(R.string.cancel) -> {
                    // 取消
                    finish()
                }
            }
        }
        // 点击标题栏右侧按钮事件处理
        binding.headerRight.setOnClickListener {
            it as TextView
            when (it.text) {
                getString(R.string.select) -> {
                    // 进入选择模式
                    enterSelectMode()
                }
                getString(R.string.start) -> {
                    // 开始批量修改
                    startBatchModify()
                }
            }
        }

        // 点击标题栏左侧按钮事件处理
        binding.leftTv.setOnClickListener {
            it as TextView
            when (it.text) {
                getString(R.string.filter) -> {
                    // 进入过滤界面
                    enterFilterActivity()
                }
            }
        }

        // 点击标题栏右侧按钮事件处理
        binding.rightTv.setOnClickListener {
            it as TextView
            when (it.text) {
                getString(R.string.cancel) -> {
                    // 取消
                    finish()
                }
                getString(R.string.select_all) -> {
                    // 全选
                    selectAllDevices()
                }
            }
        }

        // 下拉刷新事件处理
        binding.refreshableView.setOnRefreshListener {
            val itemCount = mDeviceAdapter.itemCount
            mDevices.clear()
            binding.leftNub.text = "0"
            mDeviceAdapter.notifyItemRangeRemoved(0, itemCount)
            BluetoothRepository.startScan()
            it.closeHeaderOrFooter()
        }
    }

    // 进入选择模式
    private fun enterSelectMode() {
        BluetoothRepository.stopScan()
        binding.recycler.adapter = mDeviceCheckBoxAdapter
        mDeviceCheckBoxAdapter.notifyItemRangeChanged(
            0, mDeviceCheckBoxAdapter.itemCount
        )
        binding.headerRight.text = getString(R.string.start)
        binding.rightTv.text = getString(R.string.select_all)
        binding.headerLeft.text = getString(R.string.cancel)
        binding.leftTv.visibility = View.GONE
    }

    // 开始批量修改
    private fun startBatchModify() {
        BluetoothRepository.mDevices = mDevices.filter {
            it.isClick
        }.map {
            it.device
        }.toMutableList()

        if (BluetoothRepository.mDevices.isNotEmpty()) {
            Intent(this, BatchModifyActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }
    }

    // 进入过滤界面
    private fun enterFilterActivity() {
        Intent(this, FilterActivity::class.java).apply {
            putExtra("device_type", DeviceType.BATCH)
            startActivity(this)
        }
    }

    // 全选
    private fun selectAllDevices() {
        mDevices.forEach {
            it.isClick = true
        }
        binding.recycler.adapter?.notifyItemRangeChanged(0, mDevices.size)
    }

    // 扫描到新设备时的回调方法
    override fun onSetting(device: BluetoothDevice, device1: BluetoothDeviceWrapper) {
        super.onSetting(device, device1)
        if (device1.feasyBeacon == null) {
            return
        }
        if (device1.feasyBeacon.module.toInt() != BluetoothRepository.mModule) {
            // 检查设备模块型号是否匹配
            LogUtils.e(TAG, "onSetting: 模块型号不符    ${device1.feasyBeacon.module.toInt()}   -     ${BluetoothRepository.mModule}")
            return
        }
        BluetoothRepository.mVersionRange?.let {
            if (device1.feasyBeacon.version.toInt() !in it) {
                return
            }
            if (device1.rssi > mFilterBatchRssi) {
                if (mFilterBatchName.isNotBlank()) {
                    if (device1.name == null && device1.completeLocalName == null) {
                        return
                    }
                    if (!device1.name.uppercase().contains(mFilterBatchName.uppercase())) {
                        return
                    }
                }
                runOnUiThread {
                    val index = mDevices.indexOf(BatchDevice(device1))
                    if (index == -1) {
                        mDevices.add(BatchDevice(device1))
                        mDeviceAdapter.notifyItemChanged(mDevices.size)
                    } else {
                        mDevices[index].device = device1
                        mDeviceAdapter.notifyItemChanged(index)
                    }
                    binding.leftNub.text = mDevices.size.toString()
                }
            }
        }
    }

    // 恢复活动状态时的回调方法
    override fun onResume() {
        super.onResume()
        if (binding.recycler.adapter == mDeviceAdapter) {
            // 刷新列表
            mFilterBatchName = getStr("device_${DeviceType.BATCH}_name")
            mFilterBatchRssi = getInt("device_${DeviceType.BATCH}_rssi", -100)
            binding.refreshableView.autoRefresh()
        }
    }

    // 销毁活动时的回调方法
    override fun onDestroy() {
        super.onDestroy()
        BluetoothRepository.stopScan()
    }

    companion object {
        private const val TAG = "BatchScanActivity"
    }

}
