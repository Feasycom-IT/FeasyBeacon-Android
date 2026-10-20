package com.feasycom.feasybeacon.ui.view

import android.R
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.feasycom.bean.BeaconBean
import com.feasycom.feasybeacon.databinding.ConditionEddystoneUrlParameterSettingBinding
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import com.feasycom.util.FileUtil
import okhttp3.internal.notify
import okhttp3.internal.notifyAll

class Condition_Eddystone_URLView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private var mBeacon: BeaconBean? = null

    private var sensorTypeList = arrayListOf("KEY", "G-Sensor")
    private var keyList = arrayListOf("Click On", "Click Off")
    private var gSensorList = arrayListOf("Motion")

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon

            mBinding.conditionEddystoneUrlBeaconEnable.isChecked = true
            mBeacon?.isEnable = true
            mBinding.conditionEddystoneUrlBeaconEnable.setOnCheckedChangeListener { _, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {

        mBeacon = beacon
        setUrl(beacon.url)
        setRssi(beacon.rssi)
        setEnable(beacon.isEnable)

        mBinding.conditionEddystoneUrlBeaconEnable.setOnCheckedChangeListener { _, isChecked ->
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

    private fun setUrl(temp: String?) {
        mBinding.conditionEddystoneUrl.setText(temp)
    }

    private fun setRssi(temp: String?) {
        mBinding.conditionEddystoneUrlRssi.setText(temp)
    }

    private fun setEnable(flag: Boolean) {
        mBinding.conditionEddystoneUrlBeaconEnable.isChecked = flag
    }

    private fun setSensor(sensorType: String?) {
        sensorType?.let { type ->
            val position = sensorTypeList.indexOf(type)
            if (position >= 0) {
                mBinding.conditionEddystoneUrlSensorType.setSelection(position)
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
                mBinding.conditionEddystoneUrlActionType.setSelection(position)
            }
        }
    }

    private fun setInterval(intervals: String?) {
        mBinding.conditionEddystoneUrlInterval.setText(intervals)
    }

    private fun setTimeout(timeout: String?) {
        mBinding.conditionEddystoneUrlTimeout.setText(timeout)
    }

    private var mBinding: ConditionEddystoneUrlParameterSettingBinding =
        ConditionEddystoneUrlParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    val sensorTypeAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, sensorTypeList)

    init {

        sensorTypeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionEddystoneUrlSensorType.adapter = sensorTypeAdapter


        val actionAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, keyList)
        actionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionEddystoneUrlActionType.adapter = actionAdapter

        mBinding.conditionEddystoneUrlSensorType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val selectedSensorType = sensorTypeList[p2]
                    mBeacon?.sensorType = selectedSensorType
                    updateActionSpinner(selectedSensorType)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

        mBinding.conditionEddystoneUrlActionType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val selectedActionType = when (mBeacon?.sensorType) {
                        "KEY" -> keyList[p2]
                        "G-Sensor" -> gSensorList[p2]
                        else -> keyList[2]
                    }
                    mBeacon?.actionType = selectedActionType
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

        mBinding.conditionEddystoneUrl.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.conditionEddystoneUrlRssi.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.conditionEddystoneUrlInterval.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.conditionEddystoneUrlTimeout.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.conditionEddystoneUrl.addTextChangedListener(afterTextChanged = { s ->
            val header_url: String
            var header_url_foot: String
            val value = s.toString()
            try {
                if (value.contains("https://") || value.contains("https://www") || value.contains("http://www") || value.contains(
                        "http://"
                    )
                ) {
                    header_url = FileUtil.getHeadByHex(value) // 提取 URL 头部
                    header_url_foot = FileUtil.getFootByHex(header_url) // 提取 URL 尾部
                    if (header_url == header_url_foot) {
                        if (header_url_foot.length <= 19) {
                            ViewUtil.setLabelEditBlock(
                                mBinding.conditionEddystoneUrl, mBinding.conditionEddystoneUrlLabel
                            )
                            mBeacon?.url = value
                        } else {
                            ViewUtil.setLabelEditRed(
                                mBinding.conditionEddystoneUrl, mBinding.conditionEddystoneUrlLabel
                            )
                            mBeacon?.url = ""
                        }
                    } else {
                        var len = 0
                        header_url_foot = header_url_foot.replace("00", "0e")
                        val foots = header_url_foot.split("0").toTypedArray()
                        for (i in 1 until foots.size) {
                            foots[i] = "0" + foots[i]
                        }
                        for (i in 1 until foots.size) {
                            len = len + foots[i].length - 2
                        }
                        len += foots.size
                        if (len <= 18) {
                            ViewUtil.setLabelEditBlock(
                                mBinding.conditionEddystoneUrl, mBinding.conditionEddystoneUrlLabel
                            )
                            mBeacon?.url = value
                        } else {
                            ViewUtil.setLabelEditRed(
                                mBinding.conditionEddystoneUrl, mBinding.conditionEddystoneUrlLabel
                            )
                            mBeacon?.url = ""
                        }
                    }
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionEddystoneUrl,
                        mBinding.conditionEddystoneUrlLabel
                    )
                    mBeacon?.url = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneUrl,
                    mBinding.conditionEddystoneUrlLabel
                )
                mBeacon?.url = ""
            }
        })

        mBinding.conditionEddystoneUrlRssi.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() >= -128 && value.toInt() <= 127) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionEddystoneUrlRssi, mBinding.conditionEddystoneUrlRssiLabel
                    )
                    mBeacon?.rssi = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionEddystoneUrlRssi,
                        mBinding.conditionEddystoneUrlRssiLabel
                    )
                    mBeacon?.rssi = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneUrlRssi,
                    mBinding.conditionEddystoneUrlRssiLabel
                )
                mBeacon?.rssi = ""
            }
        })

        mBinding.conditionEddystoneUrlInterval.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() == 0||value.toInt() in 20..10000) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionEddystoneUrlInterval,
                        mBinding.conditionEddystoneUrlIntervalLabel
                    )
                    mBeacon?.intervals = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionEddystoneUrlInterval,
                        mBinding.conditionEddystoneUrlIntervalLabel
                    )
                    mBeacon?.intervals = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneUrlInterval,
                    mBinding.conditionEddystoneUrlIntervalLabel
                )
                mBeacon?.intervals = ""
            }
        })

        mBinding.conditionEddystoneUrlTimeout.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() in 100..65535) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionEddystoneUrlTimeout,
                        mBinding.conditionEddystoneUrlTimeoutLabel
                    )
                    mBeacon?.timeout = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionEddystoneUrlTimeout,
                        mBinding.conditionEddystoneUrlTimeoutLabel
                    )
                    mBeacon?.timeout = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionEddystoneUrlTimeout,
                    mBinding.conditionEddystoneUrlTimeoutLabel
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
        mBinding.conditionEddystoneUrlActionType.adapter = actionAdapter

        if (currentActionType != null && actionList.contains(currentActionType)) {
            val position = actionList.indexOf(currentActionType)
            mBinding.conditionEddystoneUrlActionType.setSelection(position)
        } else if (actionList.isNotEmpty()) {
            mBinding.conditionEddystoneUrlActionType.setSelection(0)
            mBeacon?.actionType = actionList[0]
        }
    }

}