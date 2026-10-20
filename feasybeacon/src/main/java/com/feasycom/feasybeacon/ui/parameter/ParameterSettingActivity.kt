package com.feasycom.feasybeacon.ui.parameter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.feasycom.bean.BeaconBean
import com.feasycom.bean.BeaconData
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.bean.CommandBean
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivityParmeterBinding
import com.feasycom.feasybeacon.databinding.HeaderBinding
import com.feasycom.feasybeacon.logic.BluetoothRepository
import com.feasycom.feasybeacon.logic.interfaces.FscBleCallback
import com.feasycom.feasybeacon.logic.model.DeviceInfo
import com.feasycom.feasybeacon.logic.model.Devices
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import com.feasycom.feasybeacon.ui.suota.activities.SuotaActivity
import com.feasycom.feasybeacon.ui.adapter.SettingBeaconParameterAdapter
import com.feasycom.feasybeacon.ui.adapter.SettingConditionParameterAdapter
import com.feasycom.feasybeacon.ui.addbeacon.AddBeaconActivity
import com.feasycom.feasybeacon.ui.addbeacon.AddConditionActivity
import com.feasycom.feasybeacon.ui.base.BaseActivity
import com.feasycom.feasybeacon.ui.batch.config.BatchConfigActivity
import com.feasycom.feasybeacon.ui.dialog.InfoDialog
import com.feasycom.feasybeacon.ui.dialog.InfoDialog.Companion.INFO_DIALOG_SHOW_TIME
import com.feasycom.feasybeacon.ui.ota.OtaActivity
import com.feasycom.feasybeacon.ui.utils.assetsToJsonString
import com.feasycom.feasybeacon.ui.utils.isActivityTop
import com.feasycom.feasybeacon.ui.view.LabelSpinnerCallBack
import com.feasycom.util.BeaconDataManager
import com.feasycom.util.OtaUtils
import com.feasycom.util.SharedPreferencesUtil
import com.google.gson.Gson
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.security.Permissions

class ParameterSettingActivity : BaseActivity<ActivityParmeterBinding>(), FscBleCallback {


    private var popupMenu: PopupWindow? = null
    private var isMenuExpanded = false
    private var conditionbeaconhaveGsensor = false

    // BeaconBean 实例列表
    private val mBeaconBeans = ArrayList<BeaconBean>()

    private val mConditionBeans = ArrayList<BeaconBean>()

    private lateinit var headerBinding: HeaderBinding

    // 懒加载 ViewModel
    private val mViewModel by lazy {
        ViewModelProviders.of(this)[ParameterSettingViewModel::class.java]
    }

    private val mBluetoothDevice: BluetoothDevice by lazy { intent.getParcelableExtra("BluetoothDevice")!! }

    // 懒加载 BluetoothDeviceWrapper 实例
    private val mBeaconDevice: BluetoothDeviceWrapper by lazy { intent.getSerializableExtra("device") as BluetoothDeviceWrapper }

    // 懒加载设备 PIN
    private val mDevicePin: String by lazy { intent.getStringExtra("pin") ?: "000000" }


    // 连接对话框
    private lateinit var mConnectDialog: InfoDialog


    // 设备信息
    private var mDeviceInfo: DeviceInfo? = null

    // IntervalSpinnerAdapter 实例
    private lateinit var mIntervalSpinnerAdapter: ArrayAdapter<String>

    // TX 功率列表
    private var mTxPowerList: List<String> = mutableListOf()

    private val mTxPower671 = listOf(
        -200,
        -107,
        -95,
        -82,
        -70,
        -65,
        -5,
        30,
        45,
        53,
        60,
        71,
        80,
        92,
        102,
        110,
        120,
        130,
        141,
        150,
        160,
        170,
        181,
        190,
        200
    )

    private val mTxPower104DL = listOf(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
    )

    private lateinit var mAdapter: SettingBeaconParameterAdapter
    private lateinit var mConditionAdapter: SettingConditionParameterAdapter

    private val mDeviceInfoMap = mutableMapOf<String, Any>()

    private val mHandler = Handler(Looper.getMainLooper())

    override fun getViewBinding() = ActivityParmeterBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 注册 ViewModel 的回调
        mViewModel.registerViewCallback(this)

        // 初始化连接对话框
        mConnectDialog = InfoDialog(this, "")

        // 如果未连接
        if (!mViewModel.isConnect()) {
            var flag = false

            // 如果设备 PIN 码长度不为 6
            if (mDevicePin.length != 6) {
                flag = true
                // 弹出提示框：PIN 码错误
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.pin_error_info))
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener { finish() }
                    .show()
            }

            // 如果蓝牙未开启
            if (!flag && !BluetoothRepository.isBluetoothEnabled()) {
                flag = true
                // 弹出提示框：蓝牙未开启
                AlertDialog.Builder(this)
                    .setMessage(getString(R.string.bt_enabled))
                    .setPositiveButton(android.R.string.ok, null)
                    .setOnDismissListener { finish() }
                    .show()
            }

            // 如果未出现错误
            if (!flag) {
                // 显示连接对话框
                mConnectDialog.setInfo(getString(R.string.connecting))
                mConnectDialog.show()

                if (getBitFromByteArray(
                        mBeaconDevice.scanRecord,
                        mBeaconDevice.scanRecord.size - 23,
                        6
                    ) == 0
                ) {
                    conditionbeaconhaveGsensor = false
                } else {
                    conditionbeaconhaveGsensor = true
                }

                // 进行连接
                mViewModel.connect(mBeaconDevice, mDevicePin)

                BluetoothRepository.mConnectPin = mDevicePin

            }
        }
    }

    fun toHexString(bytes: ByteArray, uppercase: Boolean = true, separator: String = ""): String {
        val format = if (uppercase) "%02X" else "%02x"
        return bytes.joinToString(separator) { format.format(it) }
    }

    fun getBitFromByteArray(byteArray: ByteArray, byteIndex: Int, bitPosition: Int): Int {
        if (byteIndex !in byteArray.indices || bitPosition !in 0..7) {
            Log.e("vvv","======getBitFromByteArray====异常===")
            return 0 // 或抛出异常
        }

        val byteValue = byteArray[byteIndex].toInt() and 0xFF
        return (byteValue ushr bitPosition) and 1
    }

    override fun initView() {
        headerBinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))


        // 设置标题为设备名称或未知名称
        headerBinding.headerTitle.text =
            mBeaconDevice.completeLocalName ?: mBeaconDevice.name
                    ?: getString(R.string.unknown_name)

        // 设置标题栏右侧文本为“保存”，左侧文本为“返回设置”
        headerBinding.headerRight.text = getString(R.string.save)
        headerBinding.headerLeft.text = getString(R.string.to_setting)

        // 初始化适配器并设置 RecyclerView 布局管理器和适配器
        mAdapter = SettingBeaconParameterAdapter(this, mBeaconBeans)
        mConditionAdapter = SettingConditionParameterAdapter(this, mConditionBeans)
        with(binding.beaconSlotParameterListview) {
            layoutManager = LinearLayoutManager(this@ParameterSettingActivity)
            adapter = mAdapter
        }
        with(binding.conditionSlotParameterListview) {
            layoutManager = LinearLayoutManager(this@ParameterSettingActivity)
            adapter = mConditionAdapter
        }

        // 异步获取设备信息
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val typeModule = mBeaconDevice.feasyBeacon.module.toInt()
                LogUtils.e(TAG, "FeasyBeacon APP initView typeModule => $typeModule")
                if (mBeaconDevice.feasyBeacon.module.toInt() == TYPE_MODEL_CONDITION_ENABLED) {
                    binding.conditionSlotRl.visibility = View.VISIBLE
                }else{
                    binding.conditionSlotRl.visibility = View.GONE
                }
                mDeviceInfo = run {
                    // 直接从 JSON 文件中加载设备信息
                    val json = assetsToJsonString("Device.json")
                    val gson = Gson()
                    val devices = gson.fromJson(json, Devices::class.java)
                    devices?.data?.list?.find { it.number == typeModule }
                }


                // 更新 UI
                mDeviceInfo?.let { deviceInfo ->
                    withContext(Dispatchers.Main) {
                        LogUtils.e(
                            TAG,
                            "FeasyBeacon APP initView deviceInfo.name => ${deviceInfo.name}    ${deviceInfo.deviceType}    ${deviceInfo.number}"
                        )
                        // 设置模块名称
                        binding.moduleLabelEdit.setText(deviceInfo.name, false)

                        // 设置默认间隔
                        if (deviceInfo.defaultInterval.isNotEmpty() && "~" in deviceInfo.defaultInterval) {
                            binding.intervalLabelEdit.setRange(
                                deviceInfo.defaultInterval.split("~").toTypedArray()
                            )
                        //    LogUtils.e(TAG, "Feasybeacon APP defaultInterval => ${deviceInfo.defaultInterval}")
                        }

                        // 设置 G-Sensor 间隔
                        if (deviceInfo.gsensorInterval.isNotEmpty()) {
                            binding.gsensorEdit.setRange(deviceInfo)
                        //    LogUtils.e(TAG, "Feasybeacon APP gsensorInterval => ${deviceInfo.gsensorInterval}")
                        }

                        // 设置按键配置间隔
                        if (deviceInfo.keyInterval.isNotEmpty()) {
                            binding.keycfgEdit.setRange(deviceInfo)
                        //    LogUtils.e(TAG, "Feasybeacon APP keyInterval => ${deviceInfo.keyInterval}")
                        }
                    }
                }

            } catch (e: Exception) {
                LogUtils.d(TAG, "Feasybeacon APP initView exception => ${e.message}")
            }


        }

        // 添加文本监听器
        binding.nameLabelEdit.setTextWatcher(ViewUtil.NameTextWatcher(binding.nameLabelEdit))
        binding.pinLabelEdit.setTextWatcher(ViewUtil.PinTextWatcher(binding.pinLabelEdit))
    }

    override fun initEvent() {
        // 点击标题栏左侧按钮
        headerBinding.headerLeft.setOnClickListener {

            finish()

        }

        // 点击标题栏右侧按钮（保存）
        headerBinding.headerRight.setOnClickListener {
            // 隐藏键盘
            hideInput()

            // 检查设备名称是否为空
            if (!binding.nameLabelEdit.check()) {
                showDialogInterval(getString(R.string.name_null_info))
                return@setOnClickListener
            }

            // 检查 PIN 码是否正确
            if (!binding.pinLabelEdit.check()) {
                showDialogInterval(getString(R.string.pin_error_info))
                return@setOnClickListener
            }

            try {
                if (binding.intervalLabelEdit.interval < binding.intervalLabelEdit.intervalMin && binding.intervalLabelEdit.interval != 0) {
                    binding.intervalLabelEdit.setInterval(binding.intervalLabelEdit.intervalMin.toString())
                    showDialog(
                        getString(
                            R.string.interval_advin_info,
                            binding.intervalLabelEdit.intervalMin,
                            binding.intervalLabelEdit.intervalMax
                        )
                    )
                    return@setOnClickListener
                } else if (binding.intervalLabelEdit.interval > binding.intervalLabelEdit.intervalMax) {
                    binding.intervalLabelEdit.setInterval(binding.intervalLabelEdit.intervalMax.toString())
                    showDialog(
                        getString(
                            R.string.interval_advin_info,
                            binding.intervalLabelEdit.intervalMin,
                            binding.intervalLabelEdit.intervalMax
                        )
                    )
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                showDialog(getString(R.string.interval_not_null))
                return@setOnClickListener
            }

            try {
                if (binding.gsensorEdit.visibility != View.GONE) {
                    if (binding.gsensorEdit.getAdvin() < binding.gsensorEdit.advinMin && binding.gsensorEdit.getAdvin() != 0) {
                        binding.gsensorEdit.setAdvin(binding.gsensorEdit.advinMin)
                        showDialog(
                            getString(
                                R.string.gsensor_advin_info,
                                binding.gsensorEdit.advinMin,
                                binding.gsensorEdit.advinMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.gsensorEdit.getAdvin() > binding.gsensorEdit.advinMax) {
                        binding.gsensorEdit.setAdvin(binding.gsensorEdit.advinMax)
                        showDialog(
                            getString(
                                R.string.gsensor_advin_info,
                                binding.gsensorEdit.advinMin,
                                binding.gsensorEdit.advinMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.gsensorEdit.getDuration() < binding.gsensorEdit.durationMin) {
                        binding.gsensorEdit.setDuration(binding.gsensorEdit.durationMin)
                        showDialog(
                            getString(
                                R.string.gsensor_duration_info,
                                binding.gsensorEdit.durationMin,
                                binding.gsensorEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.gsensorEdit.getDuration() > binding.gsensorEdit.durationMax) {
                        binding.gsensorEdit.setDuration(binding.gsensorEdit.durationMax)
                        showDialog(
                            getString(
                                R.string.gsensor_duration_info,
                                binding.gsensorEdit.durationMin,
                                binding.gsensorEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                }
            } catch (e: Exception) {
                showDialog(getString(R.string.gsensor_interval_not_null))
                return@setOnClickListener
            }

            try {
                if (binding.keycfgEdit.visibility != View.GONE) {
                    if (binding.keycfgEdit.getInterval() < binding.keycfgEdit.intervalMin && binding.keycfgEdit.getInterval() != 0) {
                        binding.keycfgEdit.setInterval(binding.keycfgEdit.intervalMin.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_advin_info,
                                binding.keycfgEdit.intervalMin,
                                binding.keycfgEdit.intervalMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.keycfgEdit.getInterval() > binding.keycfgEdit.intervalMax) {
                        binding.keycfgEdit.setInterval(binding.keycfgEdit.intervalMax.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_advin_info,
                                binding.keycfgEdit.intervalMin,
                                binding.keycfgEdit.intervalMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.keycfgEdit.getDuration() < binding.keycfgEdit.durationMin) {
                        binding.keycfgEdit.setDuration(binding.keycfgEdit.durationMin.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_duration_info,
                                binding.keycfgEdit.durationMin,
                                binding.keycfgEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                    if (binding.keycfgEdit.getDuration() > binding.keycfgEdit.durationMax) {
                        binding.keycfgEdit.setDuration(binding.keycfgEdit.durationMax.toString())
                        showDialog(
                            getString(
                                R.string.keycfg_duration_info,
                                binding.keycfgEdit.durationMin,
                                binding.keycfgEdit.durationMax
                            )
                        )
                        return@setOnClickListener
                    }
                }
            } catch (e: Exception) {
                showDialog(getString(R.string.key_interval_not_null))
                return@setOnClickListener
            }

            // 检查设置是否通过验证
            if (verify()) {
                // 显示保存状态
                mConnectDialog.setInfo(getString(R.string.status_save))
                mConnectDialog.show()
                // 保存设置
                BluetoothRepository.setBeaconInfo(mBeaconBeans,mConditionBeans)
                Log.e("vvvv","==========mBeaconBeans===========${mBeaconBeans.size}       ${mConditionBeans.size}");

            } else {
                // 显示间隔信息提示
                showDialogInterval(getString(R.string.interval_message_4))
            }
        }

        // 点击“添加”按钮
        binding.addButtonContainer.setOnClickListener {
            togglePopupMenu(it)

            if (!isViewVisibleInScrollView(binding.batchButton, binding.nestedscrollview)) {
                binding.nestedscrollview.smoothScrollTo(
                    0,
                    binding.nestedscrollview.getChildAt(0).height
                )
            }

        }

        // 点击“OTA”按钮
        binding.otaButton.setOnClickListener {
            // 跳转到OTA页面


            val typeMode = mBeaconDevice.feasyBeacon.module.toInt()
            LogUtils.e(
                TAG,
                "FeasyBeacon APP ParameterSettingActivity otaButton typeMode => $typeMode"
            )

            if (OtaUtils.needsSuotaUpgrade(typeMode)) {
                val intent = Intent(this, SuotaActivity::class.java)
                intent.putExtra("BluetoothDevice", mBluetoothDevice)
                intent.putExtra("pin", mDevicePin)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, OtaActivity::class.java)
                intent.putExtra("device", mBeaconDevice)
                intent.putExtra("pin", mDevicePin)
                startActivity(intent)
            }


            /*   XXPermissions.with(this)
                   .permission(PermissionLists.getManageExternalStoragePermission())
                   .request(OnPermissionCallback{grantedList, deniedList ->
                       val allGranted = deniedList.isEmpty()
                       if (allGranted) {




                       } else {
                           // 权限被拒绝
                           val dialog = AlertDialog.Builder(this@ParameterSettingActivity)
                               .setMessage(getString(R.string.permission_denied))
                               .setPositiveButton(android.R.string.ok, null)
                               .show()
                           dialog.setCanceledOnTouchOutside(false)
                       }

                   })*/



        }

        // 点击“批量配置”按钮
        binding.batchButton.setOnClickListener {
            Intent(this, BatchConfigActivity::class.java).apply {
                // 传递设备信息到批量配置页面
                val mBundle = Bundle()
                mBundle.putSerializable("device_info_map", mDeviceInfoMap as Serializable)
                mBundle.putSerializable("device_info", mDeviceInfo as Serializable)
                putExtras(mBundle)
                startActivity(this)
            }
        }

        // 点击“恢复出厂设置”按钮
        binding.resettingButton.setOnClickListener {
            // 弹出确认恢复对话框
            AlertDialog.Builder(this).setMessage(resources.getString(R.string.reset_the_device))
                .setPositiveButton(
                    resources.getString(R.string.reset)
                ) { _, _ ->
                    // 显示恢复状态
                    mConnectDialog.setInfo(getString(R.string.status_resting))
                    mConnectDialog.show()
                    // 恢复出厂设置
                    BluetoothRepository.restore()
                }.setNegativeButton(resources.getString(R.string.cancel), null).show()
        }

        // AT指令调试
        binding.atCommandDebuggingButton.setOnClickListener {
            intoatcmd = true
            // 跳转到指令设置页面
            val intent = Intent(this, AtCommandDebugging::class.java)
            startActivity(intent)

        }
    }

    var intoatcmd: Boolean = false

    private fun isViewVisibleInScrollView(childView: View, scrollView: NestedScrollView): Boolean {
        val visibleRect = Rect()
        // 获取子视图在父容器中的可见区域
        val isVisible = childView.getLocalVisibleRect(visibleRect)

        // 计算可见比例
        val visiblePercentage = if (isVisible) {
            (visibleRect.height() * 1.0f / childView.height) * 100
        } else {
            0f
        }

        // 可以设置阈值，比如可见面积超过50%才算可见
        return isVisible && visiblePercentage >= 50
    }


    private fun togglePopupMenu(anchorView: View) {
        if (isMenuExpanded) {
            popupMenu?.dismiss()
            return
        }

        // 初始化PopupWindow
        val popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu, null)
        popupMenu = PopupWindow(
            popupView,
            binding.addButtonContainer.width,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            animationStyle = R.style.PopupAnimation
            elevation = 8f
            isOutsideTouchable = true

            setOnDismissListener {
                isMenuExpanded = false
                binding.arrowIcon.setImageResource(R.drawable.ic_arrow_up)
            }
        }

        isMenuExpanded = true
        binding.arrowIcon.setImageResource(R.drawable.ic_arrow_down)

        val menuBeaconSlot = popupView.findViewById<TextView>(R.id.menu_beacon_slot)
        val menuConditionSlot = popupView.findViewById<TextView>(R.id.menu_condition_slot)

        fun setMenuItemStyle(textView: TextView, enabled: Boolean) {
            textView.isEnabled = enabled
            if (enabled) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.black))
                textView.alpha = 1.0f
            } else {
                textView.setTextColor(ContextCompat.getColor(this, R.color.grey))
                textView.alpha = 0.5f
            }
        }

        LogUtils.e(
            TAG,
            "FeasyBeacon APP beacon count => ${getBeaconCount()}   ConditionCount => ${getConditionCount()}"
        )

        setMenuItemStyle(menuBeaconSlot, getBeaconCount() < 10)

        // 根据 typeModel 判断是否显示 Condition Slot
        if (mBeaconDevice.feasyBeacon.module.toInt() == TYPE_MODEL_CONDITION_ENABLED) {
            // 当 typeModel 等于 120 时，显示 Condition Slot 并根据条件设置样式
            setMenuItemStyle(menuConditionSlot, getConditionCount() < 10)
            menuConditionSlot.visibility = View.VISIBLE

            menuConditionSlot.setOnClickListener {
                if (menuConditionSlot.isEnabled) handleMenuItemClick("Condition")
                popupMenu?.dismiss()
            }
        } else {
            // 当 typeModel 不等于 120 时，隐藏 Condition Slot
            menuConditionSlot.visibility = View.GONE
        }

        menuBeaconSlot.setOnClickListener {
            if (menuBeaconSlot.isEnabled) handleMenuItemClick("Beacon")
            popupMenu?.dismiss()
        }

        popupMenu?.showAsDropDown(anchorView, 0, 0, Gravity.END)
    }

    private fun handleMenuItemClick(item: String) {
        when (item) {
            "Beacon" -> {
                val intent = Intent(this, AddBeaconActivity::class.java)
                intent.putExtra("dev_", mBeaconDevice)
                intent.putExtra("type", "Beacon")
                startActivity1.launch(intent)
            }

            "Condition" -> {
                val intent = Intent(this, AddConditionActivity::class.java)
                intent.putExtra("haveGsensor", conditionbeaconhaveGsensor)
                startActivity2.launch(intent)
            }
        }
    }

    private fun getBeaconCount(): Int {
        return mBeaconBeans.count()
    }

    private fun getConditionCount(): Int {
        return mConditionBeans.count()
    }

    private fun verify(): Boolean {
        // 返回 true 表示验证通过
        // TODO 给TrueSpot客户定制最大间隔时长12秒 modify by cjg: 2023-07-27
        // return if (binding.intervalLabelEdit.interval <= 13000 && binding.intervalLabelEdit.interval != 0) {
        // TODO 这里是公板代码

        if (binding.intervalLabelEdit.interval in 100..10000) {
            return true
        }

        if (binding.gsensorEdit.visibility != View.GONE && (binding.gsensorEdit.getAdvin() <= 10000 && binding.gsensorEdit.getAdvin() != 0)) {
            return true
        }

        if (binding.keycfgEdit.visibility != View.GONE && (binding.keycfgEdit.getInterval() <= 10000 && binding.keycfgEdit.getInterval() != 0)) {
            return true
        }

        if (binding.intervalLabelEdit.interval <= 10000 && mConditionBeans.size > 0) {
            return true
        }

        return false
    }

    private val startActivity1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 此处是跳转到result回调方法
            if (it.resultCode == AddBeaconActivity.REQUEST_BEACON_ADD_OK) {
                val beaconBean = it.data?.getSerializableExtra("beaconBean") as BeaconBean?
                beaconBean?.let {
                    beaconBean.index = (mBeaconBeans.size + 1).toString()
                    BluetoothRepository.addBeaconInfo(beaconBean)
                    mBeaconBeans.add(beaconBean)
                    addBeaconEnable(!BluetoothRepository.isBeaconInfoFull())
                    mAdapter.notifyItemChanged(mBeaconBeans.size)
                }
            }
        }

    private val startActivity2 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 此处是跳转到result回调方法
            if (it.resultCode == AddConditionActivity.REQUEST_CONDITION_ADD_OK) {
                val conditionBeaconBean =
                    it.data?.getSerializableExtra("conditionBeaconBean") as BeaconBean?
                conditionBeaconBean?.let {
                    LogUtils.d(
                        TAG,
                        "FeasyBeacon APP startActivity2 beaconType => ${conditionBeaconBean.beaconType}"
                    )
                    conditionBeaconBean.index = (mConditionBeans.size + 1).toString()
                    conditionBeaconBean.isHaveGsensor = conditionbeaconhaveGsensor
                    BluetoothRepository.addConditionBeaconInfo(conditionBeaconBean)
                    mConditionBeans.add(conditionBeaconBean)
                    addBeaconEnable(!BluetoothRepository.isConditionBeaconInfoFull())
                    mConditionAdapter.notifyItemChanged(mConditionBeans.size)
                }
            }
        }

    override fun onConnectProgressUpdate(status: Int) {
        super.onConnectProgressUpdate(status)
        // 根据连接状态更新界面提示信息
        Log.e(
            TAG,
            "FeasyBeacon APP ParameterSettingActivity onConnectProgressUpdate status => $status"
        )
        when (status) {
            CommandBean.PASSWORD_CHECK -> {
                // 正在检查密码
                mConnectDialog.setInfo(getString(R.string.check_password))
            }

            CommandBean.PASSWORD_SUCCESSFUL -> {
                // 密码验证成功
                mConnectDialog.setInfo(getString(R.string.password_success))
                // 获取设备信息
                mViewModel.getDeviceInfo(mBeaconDevice.feasyBeacon, true)
            }

            CommandBean.PASSWORD_FAILED -> {
                // 密码验证失败
                mConnectDialog.setInfo(getString(R.string.password_failed))


            }

            CommandBean.PASSWORD_TIME_OUT -> {
                // 连接超时
                mConnectDialog.setInfo(getString(R.string.timeout))

                // 断开连接
                BluetoothRepository.disconnect("======ParameterSettingActivity1==========")
                Log.e("vvvv","======断开   11111===========");

                mHandler.postDelayed({
                    // 如果连接对话框正在显示，则关闭对话框
                    if (mConnectDialog.isShowing) {
                        mConnectDialog.dismiss()
                    }
                    // 关闭当前页面
                    finish()
                }, INFO_DIALOG_SHOW_TIME.toLong())
            }
        }
    }

    override fun onConnectedSuccess(device: BluetoothDevice) {
        var count = SharedPreferencesUtil.getInt(this, "connect_count")
        count++
        val address = device.address
        @SuppressLint("MissingPermission") val name = device.name
        val beaconData = BeaconData(count, address, name)
        // 保存到文件
        SharedPreferencesUtil.putInt(this, "connect_count", count)
        BeaconDataManager.saveBeaconDataToFile(this, beaconData)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onDeviceInfo(parameterName: String?, parameter: Any?) {

        if (intoatcmd){
            return
        }

        // 如果参数名和参数都不为空，则将参数添加到设备信息映射中
        if (parameterName != null && parameter != null) {
            mDeviceInfoMap[parameterName] = parameter
        }
        // 在主线程中处理设备信息
        LogUtils.d(TAG, "FeasyBeacon APP onDeviceInfo parameterName => $parameterName")
        lifecycleScope.launch(Dispatchers.Main) {
            when (parameterName) {
                CommandBean.COMMAND_BEGIN -> {
                    // 开始连接
                    mConnectDialog.setInfo(getString(R.string.status_connected))
                }

                CommandBean.COMMAND_VERSION -> {
                    // 版本信息
                    binding.versionLabelEdit.setText(parameter as String, false)
                }

                CommandBean.COMMAND_ADVIN -> {
                    // 广播间隔
                    val intervalTime = parameter as String
                    binding.intervalLabelEdit.setInterval(intervalTime)
                }

                CommandBean.COMMAND_KEYCFG -> {
                    // 按键配置
                    val s = parameter as Array<String>
                    binding.keyGroup.visibility = View.VISIBLE
                    binding.keycfgEdit.setInterval(s[0])
                    binding.keycfgEdit.setDuration(s[1])
                }

                CommandBean.COMMAND_GSCFG -> {
                    // 重力传感器配置
                    binding.gsensorGroup.visibility = View.VISIBLE
                    val s = parameter as Array<*>
                    binding.gsensorEdit.setAdvin((s[0] as String).toInt())
                    binding.gsensorEdit.setDuration((s[1] as String).toInt())
                    if (s.size == 3) {
                        binding.gsensorEdit.setSensitivityEdit((s[2] as String).toInt())
                    }
                }

                CommandBean.COMMAND_BUZ, CommandBean.COMMAND_LED -> {
                    // 蜂鸣器和LED灯配置
                    // 不做处理
                }

                CommandBean.COMMAND_BWMODE -> {
                    // 蓝牙模式
                    // 不做处理
                }

                CommandBean.COMMAND_PIN -> {
                    // PIN码
                    val temp = parameter as String
                    binding.pinLabelEdit.setText(
                        when (temp.length) {
                            in 1..5 -> "0".repeat(5 - temp.length) + temp
                            else -> temp
                        },
                        true
                    )
                }

                CommandBean.COMMAND_TX_POWER -> {
                    // 发射功率
                    try {
                        // 将发射功率列表设置到下拉框适配器中
                        mTxPowerList = mDeviceInfo?.txPower?.split(",") ?: emptyList()
                        mIntervalSpinnerAdapter = ArrayAdapter<String>(
                            this@ParameterSettingActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            mTxPowerList
                        )
                        binding.txPowerLabelSpinner.spinnerInit(
                            mIntervalSpinnerAdapter,
                            mDeviceInfo!!.name,
                            object : LabelSpinnerCallBack {
                                override fun selectCallback(p: Int) {
                                    // 下拉框选中回调
                                }
                            }
                        )

                        val hexPosition = parameter as String
                        LogUtils.e(
                            TAG,
                            "FeasyBeacon APP ParameterSettingActivity COMMAND_TX_POWER hexPosition => $hexPosition"
                        )

                        val selectIndex = when (mDeviceInfo?.name) {
                            "BP106Q" -> {
                                when (hexPosition) {
                                    "1F" -> 0
                                    "23" -> 1
                                    "26" -> 2
                                    else -> 0
                                }

                            }

                            "BP671" -> mTxPower671.indexOf(hexPosition.toInt())
                            "BP104DL" -> mTxPower104DL.indexOf(hexPosition.toInt())
                            else -> hexPosition.toInt()
                        }
                        LogUtils.e(
                            TAG,
                            "FeasyBeacon APP ParameterSettingActivity COMMAND_TX_POWER selectIndex => $selectIndex"
                        )
                        binding.txPowerLabelSpinner.setSelect(selectIndex)

                        // 延迟两秒后更新广播参数
                        delay(2000)
                        BluetoothRepository.beaconBroadcastParameters()
                    } catch (e: NullPointerException) {
                        LogUtils.e(
                            TAG,
                            "FeasyBeacon APP ParameterSettingActivity COMMAND_TX_POWER 设置发射功率空指针异常 => ${e.printStackTrace()}"
                        )
                    }
                }

                CommandBean.COMMAND_BADVDATA -> {

                    (parameter as ArrayList<BeaconBean>).forEach { beaconBeacon ->
                        LogUtils.d(
                            TAG,
                            "FeasyBeacon APP ParameterSettingActivity COMMAND_BADVDATA beaconType => ${beaconBeacon.beaconType}"
                        )
                        if (beaconBeacon.beaconType.isNotBlank())
                            mBeaconBeans.add(beaconBeacon)

                    }

                    // 更新适配器
                    if (mViewModel.isBeaconInfoFull() && mViewModel.isConditionBeaconInfoFull()) {
                        addBeaconEnable(false)
                    }
                    mAdapter.notifyItemRangeInserted(0, mBeaconBeans.size)
                }

                CommandBean.COMMAND_SADV -> {
                    LogUtils.e(
                        TAG,
                        "FeasyBeacon APP COMMAND_SADV parameter  => ${parameter.toString()}"
                    )

                    (parameter as ArrayList<BeaconBean>).forEach { beaconBeacon ->

                        if (beaconBeacon.beaconType.isNotBlank()) {
                            LogUtils.e(
                                TAG,
                                "FeasyBeacon APP ParameterSettingActivity COMMAND_SADV beaconType => ${beaconBeacon.nameSpace}   ${beaconBeacon.beaconType}"
                            )
                            beaconBeacon.isHaveGsensor = conditionbeaconhaveGsensor
                            mConditionBeans.add(beaconBeacon)
                        }
                    }

                    // 更新适配器
                    if (mViewModel.isBeaconInfoFull() && mViewModel.isConditionBeaconInfoFull()) {
                        addBeaconEnable(false)
                    }
                    mConditionAdapter.notifyItemRangeInserted(0, mConditionBeans.size)

                }

                CommandBean.COMMAND_BADVPARM -> {
                    // 广播参数
                    for (i in 0 until mBeaconBeans.size) {
                        val basdvs = (parameter as String).trim().split("BADVPARM=")

                        LogUtils.e(
                            TAG,
                            "FeasyBeacon APP ParameterSettingActivity COMMAND_BADVPARM interval => ${
                                basdvs[i + 1].split(",")[2].toInt()
                            }  phy => ${basdvs[i + 1].split(",")[1]}  txPower => ${
                                basdvs[i + 1].split(
                                    ","
                                )[3]
                            }"
                        )

                        mBeaconBeans[i].interval = basdvs[i + 1].split(",")[2].toInt()
                        mBeaconBeans[i].phy = basdvs[i + 1].split(",")[1]
                        mBeaconBeans[i].txpower = basdvs[i + 1].split(",")[3]
                        if (mDeviceInfo?.name.equals("BP104EL")) {
                            mBeaconBeans[i].parentName = "BP104EL"
                        } else if (mDeviceInfo?.name.equals("BP104EA")) {
                            mBeaconBeans[i].parentName = "BP104EA"
                        } else if (mDeviceInfo?.name.equals("BP104E 2nd")) {
                            mBeaconBeans[i].parentName = "BP104E 2nd"
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                }

                CommandBean.COMMAND_EXTEND -> {
                    // 扩展信息
                    // 不做处理
                }

                CommandBean.COMMAND_TLM -> {
                    // TLM信息
                    binding.tlmGroup.visibility = View.VISIBLE
                    binding.tlmLabelButton.visibility = View.VISIBLE
                    binding.tlmLabelButton.setCheck(parameter == "1")
                    binding.tlmLabelButton.setOnToggleChanged { _, isChecked ->
                        BluetoothRepository.setTlm(isChecked)
                    }
                }
                CommandBean.COMMAND_AOA -> {
                    // AOA信息
                    binding.aoaGroup.visibility = View.VISIBLE
                    binding.aoaLabelButton.visibility = View.VISIBLE
                    binding.aoaLabelButton.setCheck(parameter == "1")
                    binding.aoaLabelButton.setOnToggleChanged { _, isChecked ->
                        BluetoothRepository.setAoa(isChecked)
                    }
                }

                CommandBean.COMMAND_ADVEXT -> {
                    // 广播扩展
                    binding.longRandGroup.visibility = View.VISIBLE
                    binding.longRangeLabelButton.visibility = View.VISIBLE
                    binding.longRangeLabelButton.setCheck(parameter == "1")
                    binding.longRangeLabelButton.setOnToggleChanged { _, isChecked ->
                        BluetoothRepository.setBroadcastExtensions(isChecked)
                    }
                }

                CommandBean.COMMAND_NAME -> {
                    // 设备名称
                    LogUtils.e(TAG, "COMMAND_NAME NAME => ${parameter as String}")
                    binding.nameLabelEdit.setText(parameter as String, true)
                }
            }
        }
    }

    override fun onAtCommandCallBack(command: String?, param: String?, status: String?) {
        // 检查命令执行状态是否为完成，并且连接状态为保存时
        LogUtils.d(
            TAG,
            "FeasyBeacon APP ParameterSettingActivity nAtCommandCallBack command => $command  param => $param  status => $status"
        )
        if (CommandBean.COMMAND_FINISH == status && applicationContext.getString(R.string.status_save) == mConnectDialog.getInfo()) {
            // 设置信息为完成状态
            mConnectDialog.setInfo(applicationContext.getString(R.string.status_finish))
            // 断开连接
            BluetoothRepository.disconnect("=======ParameterSettingActivity2======")

            Log.e("vvvv","======断开   22222===========");

            mHandler.postDelayed({
                // 如果连接对话框正在显示，则关闭对话框
                if (mConnectDialog.isShowing) {
                    mConnectDialog.dismiss()
                }
                // 关闭当前页面
                finish()
            }, INFO_DIALOG_SHOW_TIME.toLong())
        }
        // 如果命令执行状态为超时
        if (CommandBean.COMMAND_TIME_OUT == status) {
            Log.e("vvvvv","=======被调用7777==========")
            // 设置信息为超时状态
            mConnectDialog.setInfo(applicationContext.getString(R.string.status_timeout))
            // 断开连接
            BluetoothRepository.disconnect("=======ParameterSettingActivity3======")
            Log.e("vvvv","======断开   33333===========");
            mHandler.postDelayed({
                // 如果连接对话框正在显示，则关闭对话框
                if (mConnectDialog.isShowing) {
                    mConnectDialog.dismiss()
                }
                // 关闭当前页面
                finish()
            }, INFO_DIALOG_SHOW_TIME.toLong())
        }
        // 如果命令为重置设备
        if (command.equals("AT+RESTORE")) {
            mHandler.post(kotlin.run {
                Runnable {
                    try {
                        // 如果连接对话框正在显示，则关闭对话框
                        if (mConnectDialog.isShowing) {
                            mConnectDialog.dismiss()
                        }
                        // 如果当前页面处于栈顶，则显示重置成功的提示信息
                        if (isActivityTop(ParameterSettingActivity::class.java, this)) {
                            mHandler.postDelayed({
                                try {
                                    BluetoothRepository.disconnect("======ParameterSettingActivity4=========")
                                    Log.e("vvvv","======断开   444444===========");
                                    finish()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, INFO_DIALOG_SHOW_TIME.toLong())
                        }
                    } catch (_: Exception) {
                        // 异常捕获
                    }
                }
            })
        }
        // 如果命令执行状态为完成，并且连接状态为已连接
        if (CommandBean.COMMAND_FINISH == status && applicationContext.getString(R.string.status_connected) == mConnectDialog.getInfo()) {
            // 关闭连接对话框
            mConnectDialog.dismiss()
            // 移除检查连接的回调

        } else {
            // 处理其他状态
            when {
                CommandBean.COMMAND_TIME_OUT == status -> {
                    // 超时状态
                }

                CommandBean.COMMAND_FAILED == status -> {
                    // 失败状态
                }

                CommandBean.COMMAND_SUCCESSFUL == status -> {
                    // 成功状态
                }

                CommandBean.COMMAND_NO_NEED == status -> {
                    // 不需要状态
                }
            }
        }
    }

    override fun onDeleteBeaconInfo(beaconBean: BeaconBean) {
        mBeaconBeans.remove(beaconBean)
        mAdapter.notifyDataSetChanged()
        addBeaconEnable(true)
    }

    override fun onConditionDeleteBeaconInfo(beaconBean: BeaconBean) {
        mConditionBeans.remove(beaconBean)
        mConditionAdapter.notifyDataSetChanged()
        addBeaconEnable(true)
    }

    private fun addBeaconEnable(enable: Boolean) {
    //    binding.addButton.visibility = if (enable) View.VISIBLE else View.GONE
    }

    /**
     * 显示间隔错误对话框
     * @param message 错误信息
     */
    private fun showDialogInterval(message: String) {
        // 创建并显示错误对话框
        AlertDialog.Builder(this).setTitle(R.string.error)
            .setMessage(message)
            .setIcon(R.mipmap.ic_launcher)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    /**
     * 显示警告对话框
     * @param message 警告信息
     */
    private fun showDialog(message: String) {
        // 创建并显示警告对话框
        AlertDialog.Builder(this).setTitle(R.string.error)
            .setMessage(message)
            .setIcon(R.mipmap.ic_launcher)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    /**
     * 隐藏键盘
     */
    private fun hideInput() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val v = window.peekDecorView()
        if (null != v) {
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }


    override fun onDisconnect() {
        super.onDisconnect()
        if (mConnectDialog.isShowing) {

        } else {
            mHandler.post {

                if (!isonpause) {
                    mConnectDialog = InfoDialog(this, getString(R.string.disconnect))
                    mConnectDialog.show()
                }

            }

        }

        mHandler.postDelayed({
            mConnectDialog.dismiss()
            finish()
        }, 1500)

    }

    var isonpause: Boolean = false

    override fun onResume() {
        super.onResume()
        isonpause = false
    }

    override fun onPause() {
        super.onPause()
        isonpause = true
    }

    override fun onDestroy() {
        super.onDestroy()
        mConnectDialog.dismiss()
        BluetoothRepository.disconnect("======ParameterSettingActivity5========")
        mHandler.removeCallbacksAndMessages(null)
        Log.e("vvvv","======断开   55555===========");
        mViewModel.unRegisterViewCallback(this)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        private const val TAG = "ParameterSettingActivity"
        private const val TYPE_MODEL_CONDITION_ENABLED = 120
    }

}