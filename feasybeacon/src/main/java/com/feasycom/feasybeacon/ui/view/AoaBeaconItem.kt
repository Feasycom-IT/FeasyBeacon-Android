package com.feasycom.feasybeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.feasycom.bean.AoaBeacon
import com.feasycom.feasybeacon.databinding.SearchAoabeaconInfoBinding

/**
 * 自定义视图，用于显示 AOA Beacon 数据
 */
class AoaBeaconItem(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    fun setAoaBeaconValue(aoaBeacon: AoaBeacon?) {
        aoaBeacon?.let {
            try {

                mBinding.tvAoaCompanyId.text = "0x${String.format("%04x", it.companyId)}"
                mBinding.tvAoaSubType.text = "0x${String.format("%02x", it.subType)}"
                mBinding.tvAoaCteInfo.text = "0x${String.format("%02x", it.cteInfo)}"
                mBinding.tvAoaBeaconId.text = it.beaconId ?: "null"
                mBinding.tvAoaBeaconType.text = "0x${String.format("%02x", it.beaconType)}"
                mBinding.tvAoaBatteryLevel.text = "${it.batteryLevel}%"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val mBinding: SearchAoabeaconInfoBinding =
        SearchAoabeaconInfoBinding.inflate(LayoutInflater.from(context), this)
}