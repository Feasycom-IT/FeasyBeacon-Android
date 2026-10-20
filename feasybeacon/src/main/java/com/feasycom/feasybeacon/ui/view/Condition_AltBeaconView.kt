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
import com.feasycom.feasybeacon.databinding.ConditionAltbeaconParameterSettingBinding
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import java.lang.Exception
import java.util.*

class Condition_AltBeaconView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private var mBeacon: BeaconBean? = null

    private var sensorTypeList = arrayListOf("KEY", "G-Sensor")
    private var keyList = arrayListOf("Click On", "Click Off")
    private var gSensorList = arrayListOf("Motion")

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon

            mBinding.conditionAltbeaconEnable.isChecked = true
            mBeacon?.isEnable = true
            mBinding.conditionAltbeaconEnable.setOnCheckedChangeListener { _, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {

        mBeacon = beacon
        setId1(beacon.id1)
        setId2(beacon.id2)
        setId3(beacon.id3)
        setRssi(beacon.rssi)
        setManufacturerId(beacon.manufacturerId)
        setManufacturerReserved(beacon.manufacturerReserved)
        setEnable(beacon.isEnable)

        mBinding.conditionAltbeaconEnable.setOnCheckedChangeListener { _, isChecked ->
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

    private fun setId1(temp: String?) {
        mBinding.conditionAltBeaconId1.setText(temp)
    }

    private fun setId2(temp: String?) {
        mBinding.conditionAltBeaconId2.setText(temp)
    }

    private fun setId3(temp: String?) {
        mBinding.conditionAltBeaconId3.setText(temp)
    }

    private fun setRssi(temp: String?) {
        mBinding.conditionAltBeaconRssi.setText(temp)
    }

    private fun setManufacturerId(temp: String?) {
        mBinding.conditionAltBeaconManufactureId.setText(temp)
    }

    private fun setManufacturerReserved(temp: String?) {
        mBinding.conditionAltBeaconManufactureReserved.setText(temp)
    }

    private fun setEnable(flag: Boolean) {
        mBinding.conditionAltbeaconEnable.isChecked = flag
    }

    private fun setSensor(sensorType: String?) {
        sensorType?.let { type ->
            val position = sensorTypeList.indexOf(type)
            if (position >= 0) {
                mBinding.conditionAltbeaconSensorType.setSelection(position)
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
                mBinding.conditionAltbeaconActionType.setSelection(position)
            }
        }
    }

    private fun setInterval(intervals: String?) {
        mBinding.conditionAltbeaconInterval.setText(intervals)
    }

    private fun setTimeout(timeout: String?) {
        mBinding.conditionAltbeaconTimeout.setText(timeout)
    }

    private var mBinding: ConditionAltbeaconParameterSettingBinding =
        ConditionAltbeaconParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    val sensorTypeAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, sensorTypeList)

    init {

        sensorTypeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionAltbeaconSensorType.adapter = sensorTypeAdapter

        val actionAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, keyList)
        actionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionAltbeaconActionType.adapter = actionAdapter

        mBinding.conditionAltbeaconSensorType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedSensorType = sensorTypeList[p2]
                mBeacon?.sensorType = selectedSensorType
                updateActionSpinner(selectedSensorType)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        mBinding.conditionAltbeaconActionType.onItemSelectedListener = object :
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

        mBinding.conditionAltBeaconId1.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionAltBeaconId2.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionAltBeaconId3.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionAltBeaconRssi.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionAltBeaconManufactureReserved.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionAltBeaconManufactureId.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionAltbeaconInterval.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionAltbeaconTimeout.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.conditionAltBeaconId1.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 32) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionAltBeaconId1,
                    mBinding.conditionAltBeaconUuidLabel
                )
                mBeacon?.id1 = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltBeaconId1,
                    mBinding.conditionAltBeaconUuidLabel
                )
                mBeacon?.id1 = ""
            }
        })

        mBinding.conditionAltBeaconId2.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length == 4) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionAltBeaconId2,
                    mBinding.conditionAltBeaconMajorLabel
                )
                mBeacon?.id2 = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltBeaconId2,
                    mBinding.conditionAltBeaconMajorLabel
                )
                mBeacon?.id2 = ""
            }
        })

        mBinding.conditionAltBeaconId3.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length == 4) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionAltBeaconId3,
                    mBinding.conditionAltBeaconMinorLabel
                )
                mBeacon?.id3 = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltBeaconId3,
                    mBinding.conditionAltBeaconMinorLabel
                )
                mBeacon?.id3 = ""
            }
        })

        mBinding.conditionAltBeaconRssi.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() <= 127 && value.toInt() >= -128) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionAltBeaconRssi, mBinding.conditionAltBeaconRssiLabel
                    )
                    mBeacon?.rssi = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionAltBeaconRssi,
                        mBinding.conditionAltBeaconRssiLabel
                    )
                    mBeacon?.rssi = ""
                }

            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltBeaconRssi,
                    mBinding.conditionAltBeaconRssiLabel
                )
                mBeacon?.rssi = ""
            }
        })

        mBinding.conditionAltBeaconManufactureId.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length in 1..5 && Integer.valueOf(value) <= 65535) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionAltBeaconManufactureId,
                    mBinding.conditionAltBeaconManufactureIdLabel
                )
                mBeacon?.manufacturerId = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltBeaconManufactureId,
                    mBinding.conditionAltBeaconManufactureIdLabel
                )
                mBeacon?.manufacturerId = ""
            }
        })

        mBinding.conditionAltbeaconEnable.setOnCheckedChangeListener { _, isChecked ->
            mBeacon?.isEnable = isChecked
        }

        mBinding.conditionAltBeaconManufactureReserved.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())
            if (value.length == 2) {
                ViewUtil.setLabelEditBlock(
                    mBinding.conditionAltBeaconManufactureReserved,
                    mBinding.conditionAltBeaconManufactureReservedLabel
                )
                mBeacon?.manufacturerReserved = value
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltBeaconManufactureReserved,
                    mBinding.conditionAltBeaconManufactureReservedLabel
                )
                mBeacon?.manufacturerReserved = ""
            }
        })

        // 20ms ~ 10000ms
        mBinding.conditionAltbeaconInterval.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() == 0||value.toInt() in 20 .. 10000) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionAltbeaconInterval, mBinding.conditionAltbeaconIntervalLabel
                    )
                    mBeacon?.intervals = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionAltbeaconInterval, mBinding.conditionAltbeaconIntervalLabel
                    )
                    mBeacon?.intervals = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltbeaconInterval, mBinding.conditionAltbeaconIntervalLabel
                )
                mBeacon?.intervals = ""
            }
        })

        // 100ms ~ 65535ms
        mBinding.conditionAltbeaconTimeout.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() in 100..65535) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionAltbeaconTimeout, mBinding.conditionAltbeaconTimeoutLabel
                    )
                    mBeacon?.timeout = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionAltbeaconTimeout, mBinding.conditionAltbeaconTimeoutLabel
                    )
                    mBeacon?.timeout = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionAltbeaconTimeout, mBinding.conditionAltbeaconTimeoutLabel
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
        mBinding.conditionAltbeaconActionType.adapter = actionAdapter

        if (currentActionType != null && actionList.contains(currentActionType)) {
            val position = actionList.indexOf(currentActionType)
            mBinding.conditionAltbeaconActionType.setSelection(position)
        } else if (actionList.isNotEmpty()) {
            mBinding.conditionAltbeaconActionType.setSelection(0)
            mBeacon?.actionType = actionList[0]
        }
    }

}