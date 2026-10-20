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
import com.blankj.utilcode.util.LogUtils
import com.feasycom.bean.BeaconBean
import com.feasycom.feasybeacon.databinding.ConditionIbeaconParameterSettingBinding
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import java.lang.Exception
import java.util.*

class Condition_IBeaconView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private var mBeacon: BeaconBean? = null

    private var sensorTypeList = arrayListOf("KEY", "G-Sensor")
    private var keyList = arrayListOf("Click On", "Click Off")
    private var gSensorList = arrayListOf("Motion")

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon

            mBinding.conditionIbeaconEnable.isChecked = true
            mBeacon?.isEnable = true
            mBinding.conditionIbeaconEnable.setOnCheckedChangeListener { _, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {

        mBeacon = beacon
        setUuid(beacon.uuid)
        setMajor(beacon.major)
        setMinor(beacon.minor)
        setRssi(beacon.rssi)
        setEnable(beacon.isEnable)

        mBinding.conditionIbeaconEnable.setOnCheckedChangeListener { _, isChecked ->
            mBeacon?.isEnable = isChecked
        }

        if (!beacon.isHaveGsensor){

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

    private fun setUuid(temp: String?) {
        mBinding.conditionIbeaconUuid.setText(temp)
    }

    private fun setMajor(temp: String?) {
        mBinding.conditionIbeaconMajor.setText(temp)
    }

    private fun setMinor(temp: String?) {
        mBinding.conditionIbeaconMinor.setText(temp)
    }

    private fun setRssi(temp: String?) {
        mBinding.conditionIbeaconRssi.setText(temp)
    }

    private fun setEnable(flag: Boolean) {
        mBinding.conditionIbeaconEnable.isChecked = flag
    }

    private fun setSensor(sensorType: String?) {
        sensorType?.let { type ->
            val position = sensorTypeList.indexOf(type)
            if (position >= 0) {
                mBinding.conditionIbeaconSensorType.setSelection(position)
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
                mBinding.conditionIbeaconActionType.setSelection(position)
            }
        }
    }

    private fun setInterval(intervals: String?) {
        mBinding.conditionIbeaconInterval.setText(intervals)
    }

    private fun setTimeout(timeout: String?) {
        mBinding.conditionIbeaconTimeout.setText(timeout)
    }

    private var mBinding: ConditionIbeaconParameterSettingBinding =
        ConditionIbeaconParameterSettingBinding.inflate(LayoutInflater.from(context), this)
    val sensorTypeAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, sensorTypeList)

    init {

        sensorTypeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionIbeaconSensorType.adapter = sensorTypeAdapter

        val actionAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, keyList)
        actionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionIbeaconActionType.adapter = actionAdapter

        mBinding.conditionIbeaconSensorType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedSensorType = sensorTypeList[p2]
                mBeacon?.sensorType = selectedSensorType
                updateActionSpinner(selectedSensorType)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        mBinding.conditionIbeaconActionType.onItemSelectedListener = object :
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

        mBinding.conditionIbeaconUuid.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionIbeaconMajor.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionIbeaconMinor.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionIbeaconRssi.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionIbeaconInterval.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionIbeaconTimeout.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.conditionIbeaconUuid.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 32) {
                ViewUtil.setLabelEditBlock(mBinding.conditionIbeaconUuid, mBinding.conditionIbeaconUuidLabel)
                mBeacon?.uuid = value
            } else {
                ViewUtil.setLabelEditRed(mBinding.conditionIbeaconUuid, mBinding.conditionIbeaconUuidLabel)
                mBeacon?.uuid = ""
            }
        })

        mBinding.conditionIbeaconMajor.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            if (value.length in 1..5 && Integer.valueOf(value) <= 65535) {
                ViewUtil.setLabelEditBlock(mBinding.conditionIbeaconMajor, mBinding.conditionIbeaconMajorLabel)
                mBeacon?.major = value
            } else {
                ViewUtil.setLabelEditRed(mBinding.conditionIbeaconMajor, mBinding.conditionIbeaconMajorLabel)
                mBeacon?.major = ""
            }
        })

        mBinding.conditionIbeaconMinor.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            if (value.length in 1..5 && value.toInt() <= 65535) {
                ViewUtil.setLabelEditBlock(mBinding.conditionIbeaconMinor, mBinding.conditionIbeaconMinorLabel)
                mBeacon?.minor = value
            } else {
                ViewUtil.setLabelEditRed(mBinding.conditionIbeaconMinor, mBinding.conditionIbeaconMinorLabel)
                mBeacon?.minor = ""
            }
        })

        mBinding.conditionIbeaconRssi.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() >= -128 && value.toInt() <= 127) {
                    ViewUtil.setLabelEditBlock(mBinding.conditionIbeaconRssi, mBinding.conditionIbeaconRssiLabel)
                    mBeacon?.rssi = value
                } else {
                    ViewUtil.setLabelEditRed(mBinding.conditionIbeaconRssi, mBinding.conditionIbeaconRssiLabel)
                    mBeacon?.rssi = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(mBinding.conditionIbeaconRssi, mBinding.conditionIbeaconRssiLabel)
                mBeacon?.rssi = ""
            }
        })

        // 20ms ~ 10000ms
        mBinding.conditionIbeaconInterval.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() == 0||value.toInt() in 20 .. 10000) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionIbeaconInterval, mBinding.conditionIbeaconIntervalLabel
                    )
                    mBeacon?.intervals = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionIbeaconInterval, mBinding.conditionIbeaconIntervalLabel
                    )
                    mBeacon?.intervals = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionIbeaconInterval, mBinding.conditionIbeaconIntervalLabel
                )
                mBeacon?.intervals = ""
            }
        })

        // 100ms ~ 65535ms
        mBinding.conditionIbeaconTimeout.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() in 100..65535) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionIbeaconTimeout, mBinding.conditionIbeaconTimeoutLabel
                    )
                    mBeacon?.timeout = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionIbeaconTimeout, mBinding.conditionIbeaconTimeoutLabel
                    )
                    mBeacon?.timeout = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionIbeaconTimeout, mBinding.conditionIbeaconTimeoutLabel
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

        val actionAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, actionList)
        actionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionIbeaconActionType.adapter = actionAdapter

        if (currentActionType != null && actionList.contains(currentActionType)) {
            val position = actionList.indexOf(currentActionType)
            mBinding.conditionIbeaconActionType.setSelection(position)
        } else if (actionList.isNotEmpty()) {
            mBinding.conditionIbeaconActionType.setSelection(0)
            mBeacon?.actionType = actionList[0]
        }
    }


}