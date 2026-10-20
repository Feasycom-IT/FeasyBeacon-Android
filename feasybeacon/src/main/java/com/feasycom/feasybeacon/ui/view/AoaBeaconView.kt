package com.feasycom.feasybeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.feasycom.bean.BeaconBean
import com.feasycom.feasybeacon.databinding.AoabeaconParameterSettingBinding
import com.feasycom.feasybeacon.logic.utils.ViewUtil
import java.util.Locale

class AoaBeaconView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val TAG = "AoaBeaconView"

    private var mBeacon: BeaconBean? = null
    private var showType: Int = 0

    private val binding: AoabeaconParameterSettingBinding =
        AoabeaconParameterSettingBinding.inflate(LayoutInflater.from(context), this)

    fun setShowType(type: Int) {
        showType = type
    }

    fun setBeaconBean(beacon: BeaconBean) {
        if (mBeacon == null) {
            mBeacon = beacon
            binding.beaconEnable.isChecked = true
            mBeacon?.isEnable = true
            binding.beaconEnable.setOnCheckedChangeListener { _, isChecked ->
                mBeacon?.isEnable = isChecked
            }
        }
    }

    fun setBeaconInfo(beacon: BeaconBean) {
        mBeacon = beacon

        Log.v(TAG,"aoaCteInfo:"+beacon.aoaCteInfo)
        Log.v(TAG,"aoaBeaconId:"+beacon.aoaBeaconId)
        Log.v(TAG,"aoaBeaconType:"+beacon.aoaBeaconType)
        binding.aoaCteInfo.setText(beacon.aoaCteInfo)
        binding.aoaBeaconId.setText(beacon.aoaBeaconId)   // 假设存储的是十六进制字符串，如 "11229999"
        binding.aoaBeaconType.setText(beacon.aoaBeaconType)
        binding.beaconEnable.isChecked = beacon.isEnable

    }

    init {

        // 绑定监听
        binding.aoaCteInfo.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length == 2 && value.matches(Regex("[0-9a-f]{2}"))) {
                Log.v(TAG,"cte block")
                ViewUtil.setLabelEditBlock(binding.aoaCteInfo, binding.aoaCteInfoLabel)
                mBeacon?.aoaCteInfo = value
            } else {
                Log.v(TAG,"cte red")
                ViewUtil.setLabelEditRed(binding.aoaCteInfo, binding.aoaCteInfoLabel)
                mBeacon?.aoaCteInfo = ""
            }
        })

        binding.aoaBeaconId.addTextChangedListener(afterTextChanged = { s ->
            val hex = s.toString().lowercase(Locale.getDefault())
            if (hex.length == 8 && hex.matches(Regex("[0-9a-f]{8}"))) {
                ViewUtil.setLabelEditBlock(binding.aoaBeaconId, binding.aoaBeaconIdLabel)
                mBeacon?.aoaBeaconId = hex
            } else {
                ViewUtil.setLabelEditRed(binding.aoaBeaconId, binding.aoaBeaconIdLabel)
                mBeacon?.aoaBeaconId = ""
            }
        })

        binding.aoaBeaconType.addTextChangedListener(afterTextChanged = { s ->
            val value = s.toString().lowercase(Locale.getDefault())
            if (value.length == 2 && value.matches(Regex("[0-9a-f]{2}"))) {
                ViewUtil.setLabelEditBlock(binding.aoaBeaconType, binding.aoaBeaconTypeLabel)
                mBeacon?.aoaBeaconType = value
            } else {
                ViewUtil.setLabelEditRed(binding.aoaBeaconType, binding.aoaBeaconTypeLabel)
                mBeacon?.aoaBeaconType = ""
            }
        })

        binding.beaconEnable.setOnCheckedChangeListener { _, isChecked ->
            mBeacon?.isEnable = isChecked
        }

        binding.aoaCteInfo.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        binding.aoaBeaconId.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
        binding.aoaBeaconType.setOnClickListener {
            it as EditText
            it.isCursorVisible = false
            it.isCursorVisible = true
        }
    }
}