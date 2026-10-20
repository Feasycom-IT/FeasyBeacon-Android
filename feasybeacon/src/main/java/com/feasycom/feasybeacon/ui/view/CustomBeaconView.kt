package com.feasycom.feasybeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.LogUtils
import com.feasycom.bean.BeaconBean
import com.feasycom.feasybeacon.databinding.CustomBeaconParameterSettingBinding
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import java.util.*

class CustomBeaconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {

    private var mBeacon: BeaconBean? = null

    private var mBinding: CustomBeaconParameterSettingBinding =
        CustomBeaconParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    init {
        mBinding.customBeaconDataEt.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        mBinding.customBeaconDataEt.addTextChangedListener(afterTextChanged = { s ->
            var value = s.toString()
            value = value.lowercase(Locale.getDefault())

            // 移除所有非十六进制字符
            val cleanValue = value.replace("[^0-9a-f]".toRegex(), "")
            if (cleanValue != value) {
                mBinding.customBeaconDataEt.setText(cleanValue)
                mBinding.customBeaconDataEt.setSelection(cleanValue.length)
                return@addTextChangedListener
            }

            // 验证格式：支持多个 AD Structure (len + tag + data)
            if (isValidCustomConditionData(cleanValue)) {
                ViewUtil.setLabelEditBlock(
                    mBinding.customBeaconDataEt, mBinding.customBeaconDataTv
                )
                mBeacon?.customConditionData = cleanValue
            } else {
                ViewUtil.setLabelEditRed(
                    mBinding.customBeaconDataEt, mBinding.customBeaconDataTv
                )
                mBeacon?.customConditionData = ""
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

    fun setBeaconBean(beacon: BeaconBean) {
        if (null == mBeacon) {
            mBeacon = beacon
            mBinding.beaconEnable.isChecked = true
            mBeacon?.isEnable = true
            mBinding.beaconEnable.setOnCheckedChangeListener { button, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {
        mBeacon = beacon
        setCustomBeaconData(beacon.customBeaconData)
        setEnable(beacon.isEnable)
        mBinding.beaconEnable.setOnCheckedChangeListener { button, isChecked ->
            mBeacon?.isEnable = isChecked
        }
    }

    private fun setCustomBeaconData(temp: String?) {
        mBinding.customBeaconDataEt.setText(temp)
    }

    private fun setEnable(flag: Boolean) {
        mBinding.beaconEnable.isChecked = flag
    }

}