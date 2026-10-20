package com.feasycom.feasybeacon.ui.suota.activities

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.suotalib.dialogs.DialogFragmentBuilder
import com.feasycom.suotalib.dialogs.DialogFragmentManager
import com.feasycom.suotalib.dialogs.SupportCustomDialogFragment
import com.feasycom.suotalib.suota.SuotaFile
import com.feasycom.suotalib.suota.SuotaManager
import com.feasycom.suotalib.utils.SuotaUtil
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivitySuotaBinding
import com.feasycom.feasybeacon.databinding.HeaderBinding
import com.feasycom.feasybeacon.ui.suota.bluetooth.SuotaCallback
import com.feasycom.feasybeacon.ui.base.BaseActivity
import com.feasycom.feasybeacon.ui.suota.fragments.AvailableFirmwareFragment
import com.feasycom.feasybeacon.ui.suota.fragments.BaseSuotaFragment
import com.feasycom.feasybeacon.ui.suota.fragments.DeviceInfoFragment
import com.feasycom.feasybeacon.ui.suota.fragments.UpdateFirmwareFragment

class SuotaActivity : BaseActivity<ActivitySuotaBinding>(),
    DeviceInfoFragment.OnDeviceInfoFragmentInteractionListener,
    AvailableFirmwareFragment.OnAvailableFirmwareFragmentInteractionListener,
    UpdateFirmwareFragment.OnUpdateFirmwareFragmentInteractionListener,
    BaseSuotaFragment.OnBaseSuotaFragmentInteractionListener {

    private lateinit var suotaFile: SuotaFile
    private lateinit var suotaManager: SuotaManager
    private var bluetoothDevice: BluetoothDevice? = null
    private lateinit var connectingTimeoutHandler: Handler
    private var supportCustomDialogFragment: SupportCustomDialogFragment? = null
    private var pendingRebootDialog: SupportCustomDialogFragment? = null
    private var suotaFragmentSaveInstance: Bundle? = null

    private var suotaFragmentId = -1
    private var isSuotaRunning = false
    private var isSuotaCompleted = false
    private var isSuotaFailed = false

    private lateinit var suotaSubtitles: Array<String>

    private lateinit var headerBinding: HeaderBinding

    fun setPendingRebootDialog(pendingRebootDialog: SupportCustomDialogFragment) {
        this.pendingRebootDialog = pendingRebootDialog
    }

    fun getSuotaManager(): SuotaManager {
        return suotaManager
    }

    fun isSuotaCompleted(): Boolean {
        return isSuotaCompleted
    }

    fun isSuotaFailed(): Boolean {
        return isSuotaFailed
    }

    override fun getViewBinding() = ActivitySuotaBinding.inflate(layoutInflater)

    override fun initView() {
        headerBinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))

        headerBinding.headerLeft.text = getString(R.string.back)
        headerBinding.headerLeft.setOnClickListener { finish() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!initializeBluetoothDevice()) {
            ToastUtils.showShort(getString(R.string.activity_communication_error))
            finish()
            return
        }

        connectingTimeoutHandler = Handler(Looper.getMainLooper())
        suotaSubtitles = resources.getStringArray(R.array.app_device_titles)

        if (savedInstanceState == null || !savedInstanceState.containsKey("Fragment")) {
            openFragment(
                DeviceInfoFragment.newInstance(SuotaUtil.getName(bluetoothDevice)),
                "DeviceInfoFragment"
            )
        }
    }

    private fun initializeBluetoothDevice(): Boolean {
        val extras = intent.extras
        return if (extras == null || !extras.containsKey("BluetoothDevice")) {
            false
        } else {
            bluetoothDevice = extras.getParcelable("BluetoothDevice")
            bluetoothDevice != null
        }
    }

    override fun onResume() {
        super.onResume()
        pendingRebootDialog?.showDialog(supportFragmentManager)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (supportCustomDialogFragment != null) {
            dismissCustomDialog()
        }

        supportCustomDialogFragment?.dismiss()
        pendingRebootDialog?.dismiss()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1 || suotaFragmentId == UPDATE_FIRMWARE_ID) {
            finish()
        }
    }

    fun displayErrorDialog(msg: String, disconnectOnAction: Boolean) {
        supportCustomDialogFragment =
            DialogFragmentBuilder().setType(ERROR).setTitle(getString(R.string.error_title))
                .setMessage(msg).setIconResourceId(R.drawable.suota_ic_error_outline_black_36dp)
                .buildV4()

        if (disconnectOnAction) {
            val onDisplayErrorResponse = DialogFragmentManager.OnResponseListener { finish() }
            supportCustomDialogFragment?.setOnPositiveClickListener(onDisplayErrorResponse)
            supportCustomDialogFragment?.setOnDismissListener(onDisplayErrorResponse)
            supportCustomDialogFragment?.setOnDialogBackPressedListener(onDisplayErrorResponse)
        }

        supportCustomDialogFragment?.showDialog(supportFragmentManager)
    }

    fun dismissCustomDialog() {
        connectingTimeoutHandler.removeCallbacksAndMessages(null)
        supportCustomDialogFragment?.dismiss()
    }

    private fun openFragment(fragment: Fragment, tag: String) {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentByTag(tag)

        if (existingFragment != null) {
            fragmentManager.popBackStackImmediate(tag, 0)
            return
        }

        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    override fun connect() {
        if (::suotaManager.isInitialized) return
        supportCustomDialogFragment = DialogFragmentBuilder().setType(CONNECTING_TO_DEVICE)
            .setLayoutResourceId(R.layout.loading_dialog_layout).setCancelable(false)
            .setCancelableOnTouchOutside(false).buildV4()
        supportCustomDialogFragment?.let { customDialogFragment ->
            customDialogFragment.setOnDialogBackPressedListener { finish() }
            customDialogFragment.showDialog(supportFragmentManager)
        }
        suotaManager = SuotaManager(lifecycle, this, bluetoothDevice, SuotaCallback(this)).apply {
            setUiContext(this@SuotaActivity)
            connect()
        }
        connectingTimeoutHandler.postDelayed({
            finish()
            ToastUtils.showShort(getString(R.string.unable_to_connect_toast))
        }, 200000)
    }

    override fun openAvailableFirmwareFragment() {
        openFragment(AvailableFirmwareFragment.newInstance(), "AvailableFirmwareFragment")
    }

    override fun startSuotaProtocol() {
        // 标记 OTA 过程正在运行并开始固件更新。
        isSuotaRunning = true
        suotaManager.startUpdate()
    }

    override fun notifySuotaFinished() {
        // 标记 OTA 过程已完成。
        isSuotaRunning = false
        isSuotaCompleted = true
    }

    override fun onSuotaFragmentIdentifierUpdate(identifier: Int) {
        // 更新当前的片段标识符并设置工具栏子标题。
        suotaFragmentId = identifier
        headerBinding.headerTitle.text = suotaSubtitles[identifier - 1]

        // 如果片段是用于更新固件，调整工具栏和抽屉设置。
        if (suotaFragmentId == UPDATE_FIRMWARE_ID) {
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    override fun onSuotaFragmentDestroy(outState: Bundle?) {
        // 如果片段被销毁，保存片段的状态。
        suotaFragmentSaveInstance = outState
    }

    fun notifySuotaFailed() {
        // 标记 OTA 过程失败。
        isSuotaRunning = false
        isSuotaFailed = true
    }

    override fun onFirmwareSelected(suotaFile: SuotaFile?) {
        this.suotaFile = suotaFile!!
        suotaManager.suotaFile = suotaFile
        if (::suotaManager.isInitialized) {
            suotaManager.initializeSuota(240, 3, 0, 1, 4, 2)
            openFragment(
                UpdateFirmwareFragment.newInstance(
                    SuotaUtil.getName(bluetoothDevice),
                    suotaFile.filename
                ), "UpdateFirmwareFragment"
            )
        } else {
            LogUtils.e(TAG, "onFirmwareSelected BeaconOtaManager is not initialized")
        }
    }

    companion object {
        private const val TAG = "SuotaActivity"
        const val DEVICE_INFO_ID = 1
        const val AVAILABLE_FIRMWARE_ID = 2
        const val UPDATE_FIRMWARE_ID = 4
        private const val DISCONNECT = -1
        private const val SUOTA = 1
        private const val INFO = 3
        private const val CONNECTING_TO_DEVICE = 1
        private const val ERROR = 2
    }

}