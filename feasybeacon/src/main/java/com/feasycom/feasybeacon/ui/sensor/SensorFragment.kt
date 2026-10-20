package com.feasycom.feasybeacon.ui.sensor

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.feasybeacon.App
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.FootBinding
import com.feasycom.feasybeacon.databinding.FragmentDeviceBinding
import com.feasycom.feasybeacon.databinding.HeaderBinding
import com.feasycom.feasybeacon.logic.BluetoothRepository
import com.feasycom.feasybeacon.logic.DeviceType
import com.feasycom.feasybeacon.logic.interfaces.FscBleCallback
import com.feasycom.feasybeacon.logic.utils.getInt
import com.feasycom.feasybeacon.logic.utils.getStr
import com.feasycom.feasybeacon.ui.base.BaseFragment
import com.feasycom.feasybeacon.ui.adapter.SensorAdapter
import com.feasycom.feasybeacon.ui.filter.FilterActivity
import com.feasycom.feasybeacon.ui.utils.PermissionUtil
import com.feasycom.feasybeacon.ui.utils.isLocationEnabled
import java.lang.Exception

/**
 * 传感器Fragment，用于显示传感器设备列表和相关操作
 */
class SensorFragment : BaseFragment<FragmentDeviceBinding>(), FscBleCallback {

    // 存储检测到的
    private val mDevices = mutableListOf<BluetoothDeviceWrapper>()

    private var isAutoRefresh = false

    private lateinit var mFilterSensorName: String
    private var mFilterSensorRssi: Int = -100

    private lateinit var headerbinding: HeaderBinding
    private lateinit var footBinding: FootBinding

    private val mDeviceAdapter: SensorAdapter by lazy {
        SensorAdapter(requireContext(), mDevices).apply {
            mOnItemClickListener = {}
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentDeviceBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
        isAutoRefresh = true
        binding.refreshableView.autoRefresh()
    }

    override fun initView() {
        headerbinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))
        footBinding = FootBinding.bind(binding.root.findViewById(R.id.foot))

        with(headerbinding) {
            headerTitle.text = getString(R.string.sensor)
            headerLeft.text = getString(R.string.sort)
            headerRight.text = getString(R.string.filter)
            binding.recycler.layoutManager = LinearLayoutManager(context)
            binding.recycler.adapter = mDeviceAdapter
            // 设置 RecyclerView 间距
            binding.recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.set(20, 20, 20, 20)
                }
            })
        }
    }


    private fun checkEnvironment(): Int {
        if (!PermissionUtil.hasRequiredPermissions(requireContext())) {

            return -3
        }
        return 0
    }

    override fun initEvent() {
        // 底部导航栏点击事件
        with(footBinding) {
            run {
                beaconNav.setOnClickListener { findNavController().navigate(R.id.beaconFragment) }
                settingNav.setOnClickListener { findNavController().navigate(R.id.settingFragment) }
                storeNav.setOnClickListener { findNavController().navigate(R.id.storeFragment) }
                forumNav.setOnClickListener { findNavController().navigate(R.id.forumFragment) }
                aboutNav.setOnClickListener { findNavController().navigate(R.id.about_nav) }
                navigationBar.check(R.id.sensor_nav)
            }
        }

        // 排序按钮点击事件
        headerbinding.headerLeft.setOnClickListener {
            try {
                mDevices.sortByDescending { it.rssi }
                mDeviceAdapter.notifyItemRangeChanged(0, mDevices.size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 过滤按钮点击事件
        headerbinding.headerRight.setOnClickListener {
            startActivity(Intent(requireActivity(), FilterActivity::class.java).apply {
                putExtra("device_type", DeviceType.SENSOR)
            })
        }

        // 下拉刷新监听事件
        binding.refreshableView.setOnRefreshListener {
            if (checkEnvironment() == -3) {

                binding.refreshableView.closeHeaderOrFooter()
                ToastUtils.showShort(getString(R.string.insufficient_permissions))
                return@setOnRefreshListener
            }
            if (!BluetoothRepository.isBluetoothEnabled()) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
                return@setOnRefreshListener
            }
            if (!isLocationEnabled(requireContext())) {
                startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 2)
                return@setOnRefreshListener
            }

            if (!isAutoRefresh) {
                val itemCount = mDevices.size
                mDevices.clear()
                mDeviceAdapter.notifyItemRangeRemoved(0, itemCount)
            } else {
                isAutoRefresh = false
            }
            mainHandler.postDelayed({
                BluetoothRepository.startScan()
                it.closeHeaderOrFooter()
            },500)

        }
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onSensor(device: BluetoothDeviceWrapper) {
        // 检查是否符合过滤条件，符合则更新UI
        if (device.rssi > mFilterSensorRssi) {
            if (mFilterSensorName.isNotEmpty()) {
                if (device.name == null) return
                if (!device.name.uppercase().contains(mFilterSensorName.uppercase())) return
            }
            requireActivity().runOnUiThread {
                val index = mDevices.indexOf(device)
                if (index == -1) {
                    mDevices.add(device)
                    mDeviceAdapter.notifyItemChanged(mDevices.size)
                } else {
                    mDevices[index] = device
                    mDeviceAdapter.notifyItemChanged(index, "")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFilterSensorName = App.sContext.getStr("device_${DeviceType.SENSOR}_name")
        mFilterSensorRssi = App.sContext.getInt("device_${DeviceType.SENSOR}_rssi", -100)
        val itemCount = mDevices.size
        mDevices.clear()
        mDeviceAdapter.notifyItemRangeRemoved(0, itemCount)
        binding.refreshableView.autoRefresh()
        footBinding.navigationBar.check(R.id.sensor_nav)
    }


    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacksAndMessages(null)
        BluetoothRepository.stopScan()
        binding.refreshableView.closeHeaderOrFooter()
        Log.e("vvvv", "=======SensorFragment    onpause ========");
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDevices.clear()
        BluetoothRepository.unRegisterViewCallback(this)
    }

}
