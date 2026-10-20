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
import com.feasycom.feasybeacon.databinding.ConditionCustomBeaconParameterSettingBinding
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import java.util.*

class Condition_CustomBeaconView(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {

    private var mBeacon: BeaconBean? = null

    private var sensorTypeList = arrayListOf("KEY", "G-Sensor")
    private var keyList = arrayListOf("Click On", "Click Off")
    private var gSensorList = arrayListOf("Motion")

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon

            mBinding.customConditionEnable.isChecked = true
            mBeacon?.isEnable = true
            mBinding.customConditionEnable.setOnCheckedChangeListener { _, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {

        mBeacon = beacon
        setCustomBeaconData(beacon.customConditionData)
        setEnable(beacon.isEnable)

        mBinding.customConditionEnable.setOnCheckedChangeListener { _, isChecked ->
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

    private fun setCustomBeaconData(temp: String?) {
        mBinding.customConditionDataEt.setText(temp)
    }

    private fun setEnable(flag: Boolean) {
        mBinding.customConditionEnable.isChecked = flag
    }

    private fun setSensor(sensorType: String?) {
        sensorType?.let { type ->
            val position = sensorTypeList.indexOf(type)
            if (position >= 0) {
                mBinding.conditionCustomSensorType.setSelection(position)
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
                mBinding.conditionCustomActionType.setSelection(position)
            }
        }
    }

    private fun setInterval(intervals: String?) {
        mBinding.conditionCustomInterval.setText(intervals)
    }

    private fun setTimeout(timeout: String?) {
        mBinding.conditionCustomTimeout.setText(timeout)
    }

    private var mBinding: ConditionCustomBeaconParameterSettingBinding =
        ConditionCustomBeaconParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    val sensorTypeAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, sensorTypeList)

    init {

        sensorTypeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionCustomSensorType.adapter = sensorTypeAdapter

        val actionAdapter = CenteredSpinnerAdapter(context!!, R.layout.simple_spinner_item, keyList)
        actionAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        mBinding.conditionCustomActionType.adapter = actionAdapter

        mBinding.conditionCustomSensorType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedSensorType = sensorTypeList[p2]
                mBeacon?.sensorType = selectedSensorType
                updateActionSpinner(selectedSensorType)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        mBinding.conditionCustomActionType.onItemSelectedListener = object :
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

        mBinding.customConditionDataEt.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionCustomInterval.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.conditionCustomTimeout.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }

        mBinding.customConditionDataEt.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())

            // 移除所有非十六进制字符
            val cleanValue = value.replace("[^0-9a-f]".toRegex(), "")
            if (cleanValue != value) {
                mBinding.customConditionDataEt.setText(cleanValue)
                mBinding.customConditionDataEt.setSelection(cleanValue.length)
                return@addTextChangedListener
            }

            // 验证格式：支持多个 AD Structure (len + tag + data)
            if (isValidCustomConditionData(cleanValue)) {
                ViewUtil.setLabelEditBlock(
                    mBinding.customConditionDataEt, mBinding.customConditionDataTv
                )
                mBeacon?.customConditionData = cleanValue
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.customConditionDataEt, mBinding.customConditionDataTv
                )
                mBeacon?.customConditionData = ""
            }

        })

        // 20ms ~ 10000ms
        mBinding.conditionCustomInterval.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() == 0||value.toInt() in 20 .. 10000) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionCustomInterval, mBinding.conditionCustomIntervalLabel
                    )
                    mBeacon?.intervals = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionCustomInterval, mBinding.conditionCustomIntervalLabel
                    )
                    mBeacon?.intervals = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionCustomInterval, mBinding.conditionCustomIntervalLabel
                )
                mBeacon?.intervals = ""
            }
        })

        // 100ms ~ 65535ms
        mBinding.conditionCustomTimeout.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString()
            try {
                if (value.toInt() in 100..65535) {
                    ViewUtil.setLabelEditBlock(
                        mBinding.conditionCustomTimeout, mBinding.conditionCustomTimeoutLabel
                    )
                    mBeacon?.timeout = value
                } else {
                    ViewUtil.setLabelEditRed(
                        mBinding.conditionCustomTimeout, mBinding.conditionCustomTimeoutLabel
                    )
                    mBeacon?.timeout = ""
                }
            } catch (e: Exception) {
                ViewUtil.setLabelEditRed(
                    mBinding.conditionCustomTimeout, mBinding.conditionCustomTimeoutLabel
                )
                mBeacon?.timeout = ""
            }
        })
    }

    /**
     * 验证自定义条件数据的格式：支持多个 AD Structure (len + tag + data)
     * 格式要求：
     * - 每个AD Structure包含：len(1字节) + tag(1字节) + data(len-1字节)
     * - len字段表示tag和data的总字节数
     * - 必须至少包含两个AD Structure
     * - 第一个AD Structure通常是固定的广播数据(如020106)
     * - 后面必须至少再跟一个包含实际数据的AD Structure
     * - 总长度6-62个字符（3-31字节），且为偶数
     */
    private fun isValidCustomConditionData(data: String): Boolean {
        // 基础验证：长度6-62且为偶数
        if (data.length < 6 || data.length > 62 || data.length % 2 != 0) {
            LogUtils.d("FeasyBeacon APP isValidCustomConditionData 基础长度验证失败: length=${data.length}")
            return false
        }

        // 验证是否为有效的十六进制字符串
        if (!data.matches("[0-9a-f]+".toRegex())) {
            LogUtils.d("FeasyBeacon APP isValidCustomConditionData 包含非十六进制字符")
            return false
        }

        var position = 0
        val totalLength = data.length
        var structureCount = 0

        try {
            // 逐个解析AD Structure
            while (position < totalLength) {
                // 检查剩余长度是否足够读取len字段
                if (position + 2 > totalLength) {
                    LogUtils.d("FeasyBeacon APP isValidCustomConditionData 剩余长度不足读取len字段: position=$position, total=$totalLength")
                    return false
                }

                // 读取len字段（1字节=2字符）
                val lenStr = data.substring(position, position + 2)
                val len = lenStr.toInt(16)

                // 验证len值的合理性（最小2，因为至少要有tag+1字节data）
                if (len < 2) {
                    LogUtils.d("FeasyBeacon APP isValidCustomConditionData len值太小: $len")
                    return false
                }

                // 计算这个AD Structure的总字符数
                // len字段本身(1字节=2字符) + len字节的数据(每个字节2字符)
                val adStructureCharCount = 2 + (len * 2)

                // 检查剩余长度是否足够读取整个AD Structure
                if (position + adStructureCharCount > totalLength) {
                    LogUtils.d("FeasyBeacon APP isValidCustomConditionData AD Structure长度不足: 需要${adStructureCharCount}字符，但只剩${totalLength - position}字符")
                    return false
                }

                // 提取tag和data进行调试
                val tag = data.substring(position + 2, position + 4)
                val dataStart = position + 4
                val dataEnd = position + adStructureCharCount
                val dataContent = data.substring(dataStart, dataEnd)

                LogUtils.d("FeasyBeacon APP isValidCustomConditionData AD Structure: len=$len($lenStr), tag=$tag, data=$dataContent, 数据字节数=${len-1}")

                // 移动到下一个AD Structure
                position += adStructureCharCount
                structureCount++
            }

            // 验证是否正好解析完所有数据
            if (position != totalLength) {
                LogUtils.d("FeasyBeacon APP isValidCustomConditionData 数据未完全解析: position=$position, totalLength=$totalLength")
                return false
            }

            // 新增验证：必须至少有两个AD Structure
            if (structureCount < 2) {
                LogUtils.d("FeasyBeacon APP isValidCustomConditionData AD Structure数量不足: 需要至少2个，实际只有${structureCount}个")
                return false
            }

            LogUtils.d("FeasyBeacon APP isValidCustomConditionData 验证通过: 共${structureCount}个AD Structure")
            return true

        } catch (e: Exception) {
            // 转换失败或其它异常
            LogUtils.d("FeasyBeacon APP isValidCustomConditionData 解析异常: ${e.message}")
            return false
        }
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
        mBinding.conditionCustomActionType.adapter = actionAdapter

        if (currentActionType != null && actionList.contains(currentActionType)) {
            val position = actionList.indexOf(currentActionType)
            mBinding.conditionCustomActionType.setSelection(position)
        } else if (actionList.isNotEmpty()) {
            mBinding.conditionCustomActionType.setSelection(0)
            mBeacon?.actionType = actionList[0]
        }
    }

}