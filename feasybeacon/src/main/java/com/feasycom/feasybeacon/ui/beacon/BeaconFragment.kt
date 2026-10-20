package com.feasycom.feasybeacon.ui.beacon

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
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
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.feasybeacon.App
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.FragmentBeaconBinding
import com.feasycom.feasybeacon.logic.BluetoothRepository
import com.feasycom.feasybeacon.logic.DeviceType
import com.feasycom.feasybeacon.logic.interfaces.FscBleCallback
import com.feasycom.feasybeacon.logic.model.Beacon
import com.feasycom.feasybeacon.logic.utils.*
import com.feasycom.feasybeacon.ui.adapter.BeaconAdapter
import com.feasycom.feasybeacon.ui.base.BaseFragment
import com.feasycom.feasybeacon.ui.filter.FilterActivity
import com.feasycom.feasybeacon.ui.permission.PermissionDescription
import com.feasycom.feasybeacon.ui.permission.PermissionInterceptor
import com.feasycom.feasybeacon.ui.utils.BeaconDataUtil
import com.feasycom.feasybeacon.ui.utils.PermissionUtil
import com.feasycom.feasybeacon.ui.utils.isLocationEnabled
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BeaconFragment : BaseFragment<FragmentBeaconBinding>(), FscBleCallback {

    companion object {
        private const val TAG: String = "BeaconFragment"
    }

    // 存储检测到的 Beacon 列表
    private val beaconList = mutableListOf<Beacon>()

    // 是否自动刷新标志
    private var isAutoRefresh = false

    // 过滤器中的 Beacon 名称
    private var filterBeaconName = ""

    // 过滤器中的 Beacon 信号强度
    private var filterBeaconRssi: Int = -100

    // Beacon 适配器
    private val deviceAdapter by lazy { BeaconAdapter(requireContext(), beaconList) }

    private val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.feasycom.feasybeacon"

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentBeaconBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
    }

    override fun initView() {
        val storedHash = requireActivity().getStr("hash", "")
        MainScope().launch(Dispatchers.IO) {
            BeaconDataUtil.fetchAndStoreBeaconData(App.sContext, storedHash, App.mBeaconItemDao)
        }
        // 设置界面元素
        with(binding) {
            header.headerTitle.text = getString(R.string.beacon)
            header.headerLeft.text = getString(R.string.sort)
            header.headerRight.text = getString(R.string.filter)
            header.customBroadcastTv.visibility = View.VISIBLE
            header.customBroadcastTv.text = getString(R.string.custom_broadcast)
            header.customBroadcastTv.setOnClickListener {
                // 跳转到自定义广播界面
                startActivity(Intent(requireActivity(), CustomBroadcastActivity::class.java).apply {
//                    putExtra("device_type", DeviceType.BEACON)
                })
            }
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = deviceAdapter
            // 设置 RecyclerView 间距
            recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.set(20, 20, 20, 20)
                }
            })
            broadcastStatus.text =
                getString(if (deviceAdapter.defaultState == View.GONE) R.string.expansion else R.string.fold)
        }
        checkAppVersion()
    }

    override fun onResume() {
        super.onResume()

        // 获取过滤器中的设置
        if (checkEnvironment() == 0) {
            binding.tipsRl.visibility = View.GONE
            filterBeaconName = App.sContext.getStr("device_${DeviceType.BEACON}_name")
            filterBeaconRssi = App.sContext.getInt("device_${DeviceType.BEACON}_rssi", -100)

            binding.refreshableView.autoRefresh()
        } else if (checkEnvironment() == -3) {
            binding.tipsRl.visibility = View.VISIBLE
        }
        binding.foot.navigationBar.check(R.id.beacon_nav)
    }

    private fun checkAppVersion() {
        try {
            // 获取本地应用版本号
            val packageInfo: PackageInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            val localVersionCode = packageInfo.versionCode

            // 初始化 AppUpdateManager
            val appUpdateManager = AppUpdateManagerFactory.create(requireActivity())

            // 获取 Google Play 更新信息
            val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo

            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

                when (appUpdateInfo.updateAvailability()) {
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        val latestVersionCode: Int = appUpdateInfo.availableVersionCode()
                        LogUtils.d(
                            TAG,
                            "checkAppVersion local version => $localVersionCode  Google Play Latest version number => $latestVersionCode"
                        )
                        if (localVersionCode < latestVersionCode) {
                            showUpdateDialog()
                        }
                    }

                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        LogUtils.d(TAG, "checkAppVersion No update available")
                    }

                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        LogUtils.d(TAG, "checkAppVersion Update in progress")
                    }

                    UpdateAvailability.UNKNOWN -> {
                        LogUtils.d(TAG, "checkAppVersion Update availability unknown")
                    }

                    else -> {
                        LogUtils.d(TAG, "checkAppVersion Unhandled update availability state")
                    }
                }
            }

            appUpdateInfoTask.addOnFailureListener { e ->
                LogUtils.e(TAG, "checkAppVersion Failed to obtain update information: $e")
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, "checkAppVersion Failed to obtain local version information: $e")
        }
    }

    private fun showUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.version_update))
        builder.setMessage(getString(R.string.update_message))
        builder.setPositiveButton(getString(R.string.Update)) { _, _ ->
            // 跳转到应用商店
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(PLAY_STORE_URL))
            startActivity(intent)
        }
        builder.setNegativeButton(getString(R.string.talk_later)) { dialog, _ ->
            // 用户选择稍后再说
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun initEvent() {
        // 设置界面事件监听
        with(binding) {
            foot.run {
                sensorNav.setOnClickListener { findNavController().navigate(R.id.sensorFragment) }
                settingNav.setOnClickListener { findNavController().navigate(R.id.settingFragment) }
                storeNav.setOnClickListener { findNavController().navigate(R.id.storeFragment) }
                forumNav.setOnClickListener { findNavController().navigate(R.id.forumFragment) }
                aboutNav.setOnClickListener { findNavController().navigate(R.id.about_nav) }
                navigationBar.check(R.id.beacon_nav)
            }

            fab.setOnClickListener { recycler.scrollToPosition(0) }

            applyTv.setOnClickListener {
                if (checkEnvironment() == -3) {
                    initPermission()
                }
            }

            header.headerLeft.setOnClickListener {
                // 按信号强弱排序
                beaconList.sortByDescending { it.device.rssi }
                deviceAdapter.notifyItemRangeChanged(0, beaconList.size)
            }

            header.headerRight.setOnClickListener {
                // 跳转到过滤界面
                startActivity(Intent(requireActivity(), FilterActivity::class.java).apply {
                    putExtra("device_type", DeviceType.BEACON)
                })
            }

            refreshableView.setOnRefreshListener {
                // 执行刷新操作
                handleRefresh()
            }

            broadcastStatus.setOnClickListener {
                // 切换展开/折叠状态
                toggleBroadcastStatus()
            }
        }
    }

    private fun checkEnvironment(): Int {
        if (!PermissionUtil.hasRequiredPermissions(requireContext())) {
            binding.tipsRl.visibility = View.VISIBLE
            return -3
        }
        binding.tipsRl.visibility = View.GONE
        return 0
    }


    private val mainHandler = Handler(Looper.getMainLooper())

    // 处理刷新操作
    private fun handleRefresh() {

        if (checkEnvironment() == -3) {
            // 关闭刷新动画
            if (binding == null) return
            binding.refreshableView.closeHeaderOrFooter()
            ToastUtils.showShort(getString(R.string.insufficient_permissions))
            return
        }
        if (!BluetoothRepository.isBluetoothEnabled()) {
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
            binding.refreshableView.closeHeaderOrFooter()
            return
        }
        if (!isLocationEnabled(requireContext())) {
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 2)
            binding.refreshableView.closeHeaderOrFooter()
            return
        }

        if (checkEnvironment() == 0) {
            val itemCount = deviceAdapter.itemCount
            beaconList.clear()
            deviceAdapter.notifyItemRangeRemoved(0, itemCount)
            // 关闭刷新动画
            if (binding == null) return
            mainHandler.postDelayed( {
                BluetoothRepository.startScan()
                binding.refreshableView.closeHeaderOrFooter()
            },500)


        }
    }


    // 切换展开/折叠状态
    private fun toggleBroadcastStatus() {
        val (isFold, statusText) = if (binding.broadcastStatus.text == getString(R.string.fold)) {
            ShowBroadcastStatus.FOLD to R.string.expansion
        } else {
            ShowBroadcastStatus.EXPANSION to R.string.fold
        }
        deviceAdapter.isFold = isFold
        beaconList.forEach { it.foldStatus = isFold }
        binding.broadcastStatus.text = getString(statusText)
        deviceAdapter.notifyItemRangeChanged(0, beaconList.size, " ")
    }

    // 初始化权限
    private fun initPermission() {
        XXPermissions.with(this)
            .permission(PermissionLists.getAccessFineLocationPermission())
            .permission(PermissionLists.getBluetoothConnectPermission())
            .permission(PermissionLists.getBluetoothScanPermission())
            .permission(PermissionLists.getBluetoothAdvertisePermission())
            .interceptor(PermissionInterceptor())
            .description(PermissionDescription())
            .request(OnPermissionCallback { grantedList, deniedList ->
                val allGranted = deniedList.isEmpty()
                if (!allGranted) {
                    return@OnPermissionCallback
                }
                binding.refreshableView.autoRefresh()
            })
    }

    override fun onBeacon(device: BluetoothDeviceWrapper) {
        // 处理检测到的 Beacon
        handleDetectedBeacon(device)
    }

    // 处理检测到的 Beacon
    private fun handleDetectedBeacon(device: BluetoothDeviceWrapper) {
        val beacon = Beacon(device, 0)
        val index = beaconList.indexOf(beacon)
        var currentDevice: Beacon? = null
        if (index!=-1){
            currentDevice = beaconList[index]
        }

        var currentDeviceName: String? = null

        if (device.name!=null){
            currentDeviceName = device.name
        }
        if (currentDevice?.device?.name!=null){
            currentDeviceName = currentDevice.device.name
        }

        if (device.rssi <= filterBeaconRssi || (filterBeaconName.isNotEmpty() && (currentDeviceName == null || !currentDeviceName.uppercase()
                .contains(filterBeaconName.uppercase())))
        ) {
            return
        }


        requireActivity().runOnUiThread {

            if (device.aoaBeacon!=null && currentDevice?.device?.feasyBeacon?.module != "-125"){
                return@runOnUiThread
            }


            if (index == -1) {
                // 新增 Beacon
                beaconList.add(beacon)
                deviceAdapter.notifyItemChanged(beaconList.size)
            } else {
                // 更新已存在的 Beacon
                val existingBeacon = beaconList[index]
                existingBeacon.advNub++
                existingBeacon.count++
                val timeDifference = device.timestampNanos - existingBeacon.lastTimestamp

                if (existingBeacon.lastTimestamp != 0L) {
                    existingBeacon.updateInterval(timeDifference)
                }
                existingBeacon.lastTimestamp = device.timestampNanos
                existingBeacon.device = device

                deviceAdapter.notifyItemChanged(index, " ")
            }

        }
    }


    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacksAndMessages(null)
        BluetoothRepository.stopScan()
        binding.refreshableView.closeHeaderOrFooter()
        Log.e("vvvv", "=======BeaconFragment    onpause ========");


    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 清空列表并取消注册回调
        beaconList.clear()
        BluetoothRepository.unRegisterViewCallback(this)
    }

}