package com.feasycom.feasybeacon.ui.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.feasycom.feasybeacon.logic.utils.putStr
import com.feasycom.feasybeacon.ui.adapter.SettingAdapter
import com.feasycom.feasybeacon.ui.base.BaseFragment
import com.feasycom.feasybeacon.ui.filter.FilterActivity
import com.feasycom.feasybeacon.ui.parameter.ParameterSettingActivity
import com.feasycom.feasybeacon.ui.setting.ScanCallback.Scall
import com.feasycom.feasybeacon.ui.utils.PermissionUtil
import com.feasycom.feasybeacon.ui.utils.isLocationEnabled
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.king.keyboard.KingKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 设置Fragment，用于显示设置设备列表和相关操作
 */
class SettingFragment : BaseFragment<FragmentDeviceBinding>(), FscBleCallback {

    // 过滤设置的名称和RSSI
    private lateinit var mFilterSettingName: String
    private var mFilterSettingRssi: Int = -100

    private var isPinDialogShowing = false
    private var isKeyDialogShowing = false
    private var isDeviceNotFoundDialogShowing = false

    private lateinit var headerBinding: HeaderBinding
    private lateinit var footBinding: FootBinding

    // 设备列表
    private val mDevices = mutableListOf<BluetoothDeviceWrapper>()
    private var mDevice: BluetoothDeviceWrapper? = null
    private val bluetoothDevices = mutableListOf<BluetoothDevice>()
    private var mBluetoothDevice: BluetoothDevice? = null
    private val mDeviceAdapter: SettingAdapter by lazy {
        SettingAdapter(mDevices).apply {
            mOnItemClickListener = {
                mDevice = mDevices[it]
                showPinEditDialog() // 显示带输入的对话框，用于设置设备参数
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentDeviceBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BluetoothRepository.registerViewCallback(this)

    }

    override fun initView() {
        headerBinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))
        footBinding = FootBinding.bind(binding.root.findViewById(R.id.foot))

        with(headerBinding) {
            footBinding.navigationBar.check(R.id.setting_nav)
            headerTitle.text = getString(R.string.setting)
            headerLeft.text = getString(R.string.sort)
            headerSetKey.text = getString(R.string.set_key)
            headerRight.text = getString(R.string.filter)
            binding.recycler.layoutManager = LinearLayoutManager(context)
            binding.recycler.adapter = mDeviceAdapter // 设置设备列表适配器
            headerBinding.headerItems.visibility = View.VISIBLE
            qrcodeScanIv.visibility = View.VISIBLE
            qrcodeScanIv.setOnClickListener {
                toCustomization()
            }
        }

        ScanCallback.getInstance().setScall(object : Scall {
            override fun result(str: String?) {

                if (!TextUtils.isEmpty(str)) {

                    lifecycleScope.launch {
                        val matchingDevice =
                            mDevices.firstOrNull { it.address.replace(":", "") == str }
                        if (matchingDevice != null) {
                            mDevice = matchingDevice
                            showPinEditDialog()
                        } else {
                            showDeviceNotFoundDialog()
                        }
                    }

                }


            }
        })


    }

    @SuppressLint("CheckResult")
    private fun toCustomization() {

        XXPermissions.with(this)
            .permission(PermissionLists.getCameraPermission())
            .request(OnPermissionCallback { grantedList, deniedList ->
                val allGranted = deniedList.isEmpty()
                if (!allGranted) {
                    return@OnPermissionCallback
                }

                Intent(requireActivity(), ScanQrActivity::class.java).apply {
                    startActivity(this)
                }


                /*    ScanCodeConfig.create(
                        requireActivity(),
                        this@SettingFragment
                    ) //设置扫码页样式 ScanStyle.NONE：无  ScanStyle.QQ ：仿QQ样式   ScanStyle.WECHAT ：仿微信样式  ScanStyle.CUSTOMIZE ： 自定义样式
                        .setStyle(ScanStyle.CUSTOMIZE) //扫码成功是否播放音效  true ： 播放   false ： 不播放
                        .setPlayAudio(true) //设置音效音频
                        .setAudioId(R.raw.beep)
                        //以下配置 在style为 ScanStyle.CUSTOMIZE 时生效
                        //设置扫码框位置  left ： 边框左边位置   top ： 边框上边位置   right ： 边框右边位置   bottom ： 边框下边位置   单位/dp
                        //                                    .setScanRect(new ScanRect(50, 200, 300, 450), false)
                        //是否限制识别区域为设定扫码框大小  true:限制  false：不限制   默认false：识别区域为整个屏幕
                        .setLimitRect(true) //设置扫码框位置 scanSize : 扫码框大小   offsetX ： x轴偏移量    offsetY ：y轴偏移量   单位 /px
                        .setScanSize(600, 0, 0) //是否显示边框上四个角标 true ： 显示  false ： 不显示
                        .setShowFrame(true) //设置边框上四个角标颜色
                        .setFrameColor(R.color.white) //设置边框上四个角标圆角  单位 /dp
                        .setFrameRadius(2) //设置边框上四个角宽度 单位 /dp
                        .setFrameWith(4) //设置边框上四个角长度 单位 /dp
                        .setFrameLength(15) //设置是否显示边框外部阴影 true ： 显示  false ： 不显示
                        .setShowShadow(true) //设置边框外部阴影颜色
                        .setShadeColor(R.color.black_tran30) //设置扫码条运动方式   ScanMode.REVERSE : 往复运动   ScanMode.RESTART ：重复运动    默认ScanMode.RESTART
                        .setScanMode(ScanMode.REVERSE) //设置扫码条扫一次时间  单位/ms  默认3000
                        .setScanDuration(3000) //设置扫码条图片
                        .setScanBitmapId(R.drawable.scan_wechatline)
                        //以下配置在 setIdentifyMultiple 为 true 时生效
                        //设置是否开启识别多个二维码 true：开启 false：关闭   开启后识别到多个二维码会停留在扫码页 手动选择需要解析的二维码后返回结果
                        .setIdentifyMultiple(false) //设置 二维码提示按钮的宽度 单位：px
                        .setQrCodeHintDrawableWidth(120) //设置 二维码提示按钮的高度 单位：px
                        .setQrCodeHintDrawableHeight(120) //设置 二维码提示按钮的Drawable资源
                        //                                    .setQrCodeHintDrawableResource(R.mipmap.in)
                        //设置 二维码提示Drawable 是否开启缩放动画效果
                        .setStartCodeHintAnimation(true) //设置 二维码选择页 背景透明度
                        .setQrCodeHintAlpha(0.5f)
                        .buidler() //跳转扫码页   扫码页可自定义样式
                        .start(ScanCodeActivity::class.java)*/

            })


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("vvvvvv", "=========扫一扫============${data?.getStringExtra("str_data")}")
        if (resultCode == Activity.RESULT_OK && data != null) {
            val str_data: String? = data.getStringExtra("str_data")

            /*   if (!TextUtils.isEmpty(str_data)){

                   lifecycleScope.launch {
                       val matchingDevice =
                           mDevices.firstOrNull { it.address.replace(":", "") == str_data }
                       if (matchingDevice != null) {
                           mDevice = matchingDevice
                           showPinEditDialog()
                       } else {
                           showDeviceNotFoundDialog()
                       }
                   }

           }*/

            /*       when (requestCode) {
                       ScanCodeConfig.QUESTCODE -> {
                           val extras = data.extras
                           extras?.getString(ScanCodeConfig.CODE_KEY)?.let { code ->
                               lifecycleScope.launch {
                                   val matchingDevice =
                                       mDevices.firstOrNull { it.address.replace(":", "") == code }
                                   if (matchingDevice != null) {
                                       mDevice = matchingDevice
                                       showPinEditDialog()
                                   } else {
                                       showDeviceNotFoundDialog()
                                   }
                               }
                           }
                       }
                   }*/
        }
    }

    private fun showDeviceNotFoundDialog() {
        if (isDeviceNotFoundDialogShowing) {
            return
        }
        isDeviceNotFoundDialogShowing = true

        val dialog = Dialog(requireContext(), R.style.BaseDialogStyle)
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_device_not_found, null)
        val btnDialogConfirm = view.findViewById<AppCompatButton>(R.id.yes)
        btnDialogConfirm.setOnClickListener {
            isDeviceNotFoundDialogShowing = false
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.window?.apply {
            val lp = attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes = lp
        }
        dialog.show()
    }


    private fun checkEnvironment(): Int {
        if (!PermissionUtil.hasRequiredPermissions(requireContext())) {

            return -3
        }
        return 0
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun initEvent() {
        // 底部导航栏点击事件
        footBinding.beaconNav.setOnClickListener {
            findNavController().navigate(R.id.beaconFragment)
        }
        footBinding.sensorNav.setOnClickListener {
            findNavController().navigate(R.id.sensorFragment)
        }
        footBinding.storeNav.setOnClickListener {
            findNavController().navigate(R.id.storeFragment)
        }
        footBinding.forumNav.setOnClickListener {
            findNavController().navigate(R.id.forumFragment)
        }
        footBinding.aboutNav.setOnClickListener {
            findNavController().navigate(R.id.about_nav)
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


            val itemCount = mDevices.size
            headerBinding.headerItems.setText("")
            mDevices.clear() // 清空设备列表
            bluetoothDevices.clear()
            mDeviceAdapter.notifyItemRangeRemoved(0, itemCount)

            mainHandler.postDelayed({
                BluetoothRepository.startScan()
                it.closeHeaderOrFooter()
            }, 500)

        }

        // 排序按钮点击事件
        headerBinding.headerLeft.setOnClickListener {
            try {
                // 按信号强度对设备列表进行降序排序
                mDevices.sortByDescending {
                    it.rssi
                }
                mDeviceAdapter.notifyItemRangeChanged(0, mDevices.size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 设置密钥
        headerBinding.headerSetKey.setOnClickListener {
            showKeyEditDialog()
        }

        // 过滤按钮点击事件
        headerBinding.headerRight.setOnClickListener {
            Intent(requireActivity(), FilterActivity::class.java).apply {
                putExtra("device_type", DeviceType.SETTING)
                startActivity(this)
            }
        }
    }

    /**
     * 显示带输入的对话框
     */
    private fun showPinEditDialog() {
        if (isPinDialogShowing) {
            return
        }
        isPinDialogShowing = true

        val dialog = Dialog(requireContext(), R.style.BaseDialogStyle)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pin_input, null)
        val keyboardParent = view.findViewById<ViewGroup>(R.id.keyboardParent)
        val etDialogContent = view.findViewById<EditText>(R.id.input_pin)
        val showName = view.findViewById<TextView>(R.id.show_name)
        showName.text = mDevice?.name
        val showMac = view.findViewById<TextView>(R.id.show_mac)
        showMac.text = mDevice?.address
        val btnDialogConfirm = view.findViewById<AppCompatButton>(R.id.yes)
        val btnDialogCancel = view.findViewById<AppCompatButton>(R.id.cancel)
        btnDialogConfirm.setOnClickListener {
            isPinDialogShowing = false

            for (bluetoothDevice in bluetoothDevices) {
                if (bluetoothDevice.address.equals(mDevice?.address)) {
                    mBluetoothDevice = bluetoothDevice
                }
            }
            mDevice?.let {

                Intent(requireContext(), ParameterSettingActivity::class.java).apply {
                    this.putExtra("BluetoothDevice", mBluetoothDevice)
                    this.putExtra("device", it)
                    this.putExtra("pin", etDialogContent.text.toString())
                    startActivity(this)
                }
            }
            dialog.dismiss()
        }
        btnDialogCancel.setOnClickListener {
            isPinDialogShowing = false
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.window?.apply {
            val lp = attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes = lp
        }
        // 初始化KingKeyboard
        val kingKeyboard = KingKeyboard(requireActivity(), keyboardParent)
        kingKeyboard.register(etDialogContent, KingKeyboard.KeyboardType.NUMBER)
        BluetoothRepository.stopScan()
        dialog.show()
        lifecycleScope.launch(Dispatchers.IO) {
            delay(100)
            withContext(Dispatchers.Main) {
                etDialogContent.requestFocus()
            }
        }
    }

    /**
     * 显示带输入的对话框
     */
    private fun showKeyEditDialog() {
        if (isKeyDialogShowing) {
            return
        }
        isKeyDialogShowing = true

        val dialog = Dialog(requireContext(), R.style.BaseDialogStyle)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_key_input, null)
        val etDialogContent = view.findViewById<EditText>(R.id.input_key)
        val appKey = requireContext().getStr("app_key", "")
        etDialogContent.setText(appKey)
        etDialogContent.setSelection(appKey.length)
        val btnDialogConfirm = view.findViewById<Button>(R.id.yes)
        val btnDialogCancel = view.findViewById<Button>(R.id.cancel)
        btnDialogConfirm.setOnClickListener {
            isKeyDialogShowing = false
            val appKey = etDialogContent.text.toString()
            requireContext().putStr("app_key", appKey)
            BluetoothRepository.setAppKey(appKey)
            dialog.dismiss()
        }
        btnDialogCancel.setOnClickListener {
            isKeyDialogShowing = false
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.window?.apply {
            val lp = attributes
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes = lp
        }
        dialog.show()
        lifecycleScope.launch(Dispatchers.IO) {
            delay(100)
            withContext(Dispatchers.Main) {
                etDialogContent.requestFocus()
            }
        }
    }

    // 当获取到新的设备信息时调用
    override fun onSetting(device: BluetoothDevice, device1: BluetoothDeviceWrapper) {
        if (device1.rssi > mFilterSettingRssi) {
            if (mFilterSettingName.isNotEmpty()) {
                if (device1.name == null) return
                if (!device1.name.uppercase().contains(mFilterSettingName.uppercase())) return
            }

            requireActivity().runOnUiThread {
                val index = mDevices.indexOf(device1)
                val index1 = bluetoothDevices.indexOf(device)

                if (index1 == -1) {
                    bluetoothDevices.add(device)
                }

                if (index == -1) {
                    mDevices.add(device1)
                    mDeviceAdapter.notifyItemChanged(mDevices.size)
                    headerBinding.headerItems.setText("" + mDevices.size)
                } else {
                    mDevices[index] = device1
                    mDeviceAdapter.notifyItemChanged(index, "1")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 获取过滤设置的名称和RSSI，然后自动刷新设备列表
        mFilterSettingName = App.sContext.getStr("device_${DeviceType.SETTING}_name")
        mFilterSettingRssi = App.sContext.getInt("device_${DeviceType.SETTING}_rssi", -100)
        binding.refreshableView.autoRefresh()
        footBinding.navigationBar.check(R.id.setting_nav)
    }


    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacksAndMessages(null)
        BluetoothRepository.stopScan()
        binding.refreshableView.closeHeaderOrFooter()
        Log.e("vvvv", "=======SettingFragment    onpause ========");

    }


    override fun onDestroyView() {
        super.onDestroyView()
        mDevices.clear() // 清空设备列表
        bluetoothDevices.clear()
        BluetoothRepository.unRegisterViewCallback(this)
    }

}
