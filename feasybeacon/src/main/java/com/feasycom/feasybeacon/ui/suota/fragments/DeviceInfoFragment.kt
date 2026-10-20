package com.feasycom.feasybeacon.ui.suota.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.feasycom.suotalib.global.SuotaProfile
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.ui.suota.activities.SuotaActivity
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

class DeviceInfoFragment : BaseSuotaFragment(){

    private var updateButton: Button? = null
    private var infoItemList: MutableList<TextView>? = null
    private var mListener: OnDeviceInfoFragmentInteractionListener? = null
    private var deviceName: String? = null
    private var manufacturer: String? = null
    private var modelNumber: String? = null
    private var firmwareRevision: String? = null
    private var softwareRevision: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            deviceName = requireArguments().getString("BluetoothDevice")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        baseListener!!.onSuotaFragmentIdentifierUpdate(SuotaActivity.DEVICE_INFO_ID)
        return inflater.inflate(R.layout.fragment_device_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        updateButton = view.findViewById(R.id.update)
        updateButton?.let {
            it.setOnClickListener {
                mListener!!.openAvailableFirmwareFragment()
            }
        }
        infoItemList = ArrayList(5)
        infoItemList?.let {
            it.add(view.findViewById(R.id.device_name_value))
            it.add(view.findViewById(R.id.manufacturer_value))
            it.add(view.findViewById(R.id.model_number_value))
            it.add(view.findViewById(R.id.firmware_revision_value))
            it.add(view.findViewById(R.id.software_revision_value))
            if (deviceName != null) {
                it[DEVICE_NAME_INDEX].text = deviceName
            }
            if (manufacturer != null) {
                it[MANUFACTURER_INDEX].text = manufacturer
            }
            if (modelNumber != null) {
                it[MODEL_NUMBER_INDEX].text = modelNumber
            }
            if (firmwareRevision != null) {
                it[FIRMWARE_REVISION_INDEX].text = firmwareRevision
            }
            if (softwareRevision != null) {
                it[SOFTWARE_REVISION_INDEX].text = softwareRevision
            }
        }
        mListener!!.connect()
        restoreState(arguments)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        restoreState(savedInstanceState)
    }

    override fun saveState(): Bundle {
        val bundle = Bundle()
        for (i in infoItemList!!.indices) {
            bundle.putString("info$i", infoItemList!![i].text.toString())
        }
        return bundle
    }

    override fun restoreState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        for (i in infoItemList!!.indices) {
            if (savedInstanceState.containsKey("info$i")) infoItemList!![i].text =
                savedInstanceState.getString(
                    "info$i"
                )
        }
    }

    fun deviceInfoUpdate(uuid: UUID, info: String) {
        // 将信息字符串赋值给局部变量 value
        var value = info

        // 检查特征值的 UUID 是否是制造商名称特征
        if (uuid == SuotaProfile.Uuid.CHARACTERISTIC_MANUFACTURER_NAME_STRING) {
            // 如果制造商名称是 "Dialog Semi"，则将其转换为 "Dialog Semiconductor"
            if (value == "Dialog Semi") value = "Dialog Semiconductor"
            // 更新制造商信息的 UI 元素
            infoItemList!![MANUFACTURER_INDEX].text = value
            // 记录日志，输出制造商名称
            LogUtils.i(TAG, "deviceInfoUpdate Manufacturer: $value")
            // 保存制造商名称到变量
            manufacturer = value
            return
        }

        // 检查特征值的 UUID 是否是型号编号特征
        if (uuid == SuotaProfile.Uuid.CHARACTERISTIC_MODEL_NUMBER_STRING) {
            // 更新型号编号的 UI 元素
            infoItemList!![MODEL_NUMBER_INDEX].text = value
            // 记录日志，输出型号编号
            LogUtils.i(TAG, "deviceInfoUpdate Model Number: $value")
            // 保存型号编号到变量
            modelNumber = value
            return
        }

        // 检查特征值的 UUID 是否是固件版本特征
        if (uuid == SuotaProfile.Uuid.CHARACTERISTIC_FIRMWARE_REVISION_STRING) {
            // 更新固件版本的 UI 元素
            infoItemList!![FIRMWARE_REVISION_INDEX].text = value
            // 记录日志，输出固件版本
            LogUtils.i(TAG, "deviceInfoUpdate Firmware Revision: $value")
            // 保存固件版本到变量
            firmwareRevision = value
            return
        }

        // 检查特征值的 UUID 是否是软件版本特征
        if (uuid == SuotaProfile.Uuid.CHARACTERISTIC_SOFTWARE_REVISION_STRING) {
            // 更新软件版本的 UI 元素
            infoItemList!![SOFTWARE_REVISION_INDEX].text = value
            // 记录日志，输出软件版本
            LogUtils.i(TAG, "deviceInfoUpdate Software Revision: $value")
            // 保存软件版本到变量
            softwareRevision = value
            return
        }

        // 如果 UUID 不匹配任何已知的特征值类型，记录未知信息日志
        LogUtils.i(TAG, "deviceInfoUpdate Unknown info: $value")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 保存设备信息到Bundle
        for (i in infoItemList!!.indices) {
            outState.putString("info$i", infoItemList!![i].text.toString())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 确保宿主Activity实现了监听器接口
        mListener = if (context is OnDeviceInfoFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnDeviceInfoFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null // 解除监听器引用
    }

    interface OnDeviceInfoFragmentInteractionListener {
        fun connect()
        fun openAvailableFirmwareFragment()
    }

    companion object {
        private const val TAG = "DeviceInfoFragment"
        private const val DEVICE_NAME_INDEX = 0 // 设备名称在TextView列表中的索引
        private const val MANUFACTURER_INDEX = 1 // 生产厂家在TextView列表中的索引
        private const val MODEL_NUMBER_INDEX = 2 // 型号在TextView列表中的索引
        private const val FIRMWARE_REVISION_INDEX = 3 // 固件版本在TextView列表中的索引
        private const val SOFTWARE_REVISION_INDEX = 4 // 软件版本在TextView列表中的索引

        // 创建DeviceInfoFragment实例，传递设备名称参数
        fun newInstance(deviceName: String?): DeviceInfoFragment {
            val deviceInfoFragment = DeviceInfoFragment()
            val args = Bundle()
            args.putString("BluetoothDevice", deviceName)
            deviceInfoFragment.arguments = args
            return deviceInfoFragment
        }

        // 创建DeviceInfoFragment实例，传递Bundle参数
        fun newInstance(bundle: Bundle?): DeviceInfoFragment {
            val deviceInfoFragment = DeviceInfoFragment()
            deviceInfoFragment.arguments = bundle
            return deviceInfoFragment
        }
    }
}