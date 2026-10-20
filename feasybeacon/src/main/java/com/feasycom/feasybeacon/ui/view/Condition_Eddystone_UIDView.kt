package com.feasycom.feasybeacon.ui.view

import android.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.feasycom.bean.BeaconBean
import com.feasycom.feasybeacon.databinding.ConditionEddystoneUidParameterSettingBinding
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import java.util.*

class Condition_Eddystone_UIDView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private var mBeacon: BeaconBean? = null

    private var sensorTypeList = arrayListOf("KEY", "G-Sensor")
    private var keyList = arrayListOf("Click On", "Click Off")
    private var gSensorList = arrayListOf("Motion")

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon

            mBinding.conditionEddystoneUidBeaconEnable.isChecked = true
            mBeacon?.isEnable = true
            mBinding.conditionEddystoneUidBeaconEnable.setOnCheckedChangeListener { _, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {

        mBeacon = beacon
        setNameSpace(beacon.nameSpace)
        setInstance(beacon.instance)
        setReserved(beacon.reserved)
        setRssi(beacon.rssi)
        setEnable(beacon.isEnable)

        mBinding.conditionEddystoneUidBeaconEnable.setOnCheckedChangeListener { _, isChecked ->
            mBeacon?.isEnable = isChecked
        }

        if (!beacon.isHaveGsensor) {
            if (!sensorTypeList.isEmpty()){
                sensorTypeList.removeAt(sensorTypeList.lastIndex)
                sensorTypeAdapter.notifyDataSetChanged()
            }

        }

        setSensor(beacon.sensorType)
        setAction(beacon.actionType)
        setInterval(beacon.intervals)
        setTimeout(beacon.timeout)
    }

    /**
     * 设置Namespace
     * @param temp Namespace值
     */
    private fun setNameSpace(temp: String?) {
        mBinding.conditionEddystoneNamespace.setText(temp)
    }

    /**
     * 设置Instance
     * @param temp Instance值
     */
    private fun setInstance(temp: String?) {
        mBinding.conditionEddystoneInstance.setText(temp)
    }

    /**
     * 设置Reserved
     * @param temp Reserved值
     */
    private fun setReserved(temp: String?) {
        mBinding.conditionEddystoneReserved.setText(temp)
    }

    /**
     * 设置Power
     * @param temp Power值
     */
    private fun setRssi(temp: String?) {
        mBinding.conditionEddystoneRssi.setText(temp)
    }

    /**
     * 设置Enable状态
     * @param flag Enable状态
     */
    private fun setEnable(flag: Boolean) {
        mBinding.conditionEddystoneUidBeaconEnable.isChecked = flag
    }

    private fun setSensor(sensorType: String?) {
        sensorType?.let { type ->
            val position = sensorTypeList.indexOf(type)
            if (position >= 0) {
                mBinding.conditionEddystoneUidSensorType.setSelection(position)
            }
        }
    }

    private fun setAction(actionType: String?) {
        actionType?.let { type ->
            val currentSensorType = mBeacon?.sensorType ?: "KEY"
            val actionList = when (currentSensorType) {
                "KEY" -> keyList
                "G-Sensor" -> gSensorList
                else -> keyList
            }

            val position = actionList.indexOf(type)
            if (position >= 0) {
                mBinding.conditionEddystoneUidActionType.setSelection(position)
            }
        }
    }

    private fun setInterval(intervals: String?) {
        mBinding.conditionEddystoneUidInterval.setText(intervals)
    }

    private fun setTimeout(timeout: String?) {
        mBinding.conditionEddystoneUidTimeout.setText(timeout)
    }

    private var mBinding: ConditionEddystoneUidParameterSettingBinding =
        ConditionEddystoneUidParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    val sensorTypeAdapter =
        CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, sensorTypeList)

    init {

        sensorTypeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionEddystoneUidSensorType.adapter = sensorTypeAdapter

        val actionAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, keyList)
        actionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionEddystoneUidActionType.adapter = actionAdapter

        mBinding.conditionEddystoneUidSensorType.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val selectedSensorType = sensorTypeList[p2]
                    mBeacon?.sensorType = selectedSensorType
                    updateActionSpinner(selectedSensorType)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

        mBinding.conditionEddystoneUidActionType.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedActionType = when (mBeacon?.sensorType) {
                    "KEY" -> keyList[p2]
                    "G-Sensor" -> gSensorList[p2]
                    else -> keyList[p2]
                }
                mBeacon?.actionType = selectedActionType
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        mBinding.conditionEddystoneNamespace.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionEddystoneInstance.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionEddystoneReserved.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionEddystoneRssi.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionEddystoneUidInterval.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionEddystoneUidTimeout.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.conditionEddystoneNamespace.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 20) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionEddystoneNamespace, mBinding.conditionEddystoneNamespaceLabel
                )
                mBeacon?.nameSpace = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneNamespace, mBinding.conditionEddystoneNamespaceLabel
                )
                mBeacon?.nameSpace = ""
            }
        })

        mBinding.conditionEddystoneInstance.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 12) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionEddystoneInstance, mBinding.conditionEddystoneInstanceLabel
                )
                mBeacon?.instance = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneInstance, mBinding.conditionEddystoneInstanceLabel
                )
                mBeacon?.instance = ""
            }
        })

        mBinding.conditionEddystoneReserved.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 4) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionEddystoneReserved, mBinding.conditionEddystoneReservedLabel
                )
                mBeacon?.reserved = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneReserved, mBinding.conditionEddystoneReservedLabel
                )
                mBeacon?.reserved = ""
            }
        })

        mBinding.conditionEddystoneRssi.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() >= -128 && value.toInt() <= 127) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionEddystoneRssi, mBinding.conditionEddystoneRssiLable
                    )
                    mBeacon?.rssi = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionEddystoneRssi,
                        mBinding.conditionEddystoneRssiLable
                    )
                    mBeacon?.rssi = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneRssi,
                    mBinding.conditionEddystoneRssiLable
                )
                mBeacon?.rssi = ""
            }
        })

        // 20ms ~ 10000ms
        mBinding.conditionEddystoneUidInterval.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() == 0 || value.toInt() in 20..10000) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionEddystoneUidInterval,
                        mBinding.conditionEddystoneUidIntervalLabel
                    )
                    mBeacon?.intervals = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionEddystoneUidInterval,
                        mBinding.conditionEddystoneUidIntervalLabel
                    )
                    mBeacon?.intervals = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneUidInterval,
                    mBinding.conditionEddystoneUidIntervalLabel
                )
                mBeacon?.intervals = ""
            }
        })

        // 100ms ~ 65535ms
        mBinding.conditionEddystoneUidTimeout.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() in 100..65535) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionEddystoneUidTimeout,
                        mBinding.conditionEddystoneUidTimeoutLabel
                    )
                    mBeacon?.timeout = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionEddystoneUidTimeout,
                        mBinding.conditionEddystoneUidTimeoutLabel
                    )
                    mBeacon?.timeout = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneUidTimeout,
                    mBinding.conditionEddystoneUidTimeoutLabel
                )
                mBeacon?.timeout = ""
            }
        })
    }

    private fun updateActionSpinner(sensorType: String) {
        val actionList = when (sensorType) {
            "KEY" -> keyList
            "G-Sensor" -> gSensorList
            else -> keyList
        }

        val currentActionType = mBeacon?.actionType

        val actionAdapter =
            CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, actionList)
        actionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionEddystoneUidActionType.adapter = actionAdapter

        if (currentActionType != null && actionList.contains(currentActionType)) {
            val position = actionList.indexOf(currentActionType)
            mBinding.conditionEddystoneUidActionType.setSelection(position)
        } else if (actionList.isNotEmpty()) {
            mBinding.conditionEddystoneUidActionType.setSelection(0)
            mBeacon?.actionType = actionList[0]
        }
    }

}