package com.feasycom.feasybeacon.ui.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.blankj.utilcode.util.LogUtils
import com.feasycom.controler.beacon.FscBeaconCentralApi
import com.feasycom.controler.beacon.FscBeaconCentralApiImp
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.KeycfgEditViewBinding
import com.feasycom.feasybeacon.logic.model.DeviceInfo

class KeycfgEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {

    companion object {
        private const val TAG: String = "keycfgEditView"
    }

    private val fscBeaconCentralApi: FscBeaconCentralApi = FscBeaconCentralApiImp.getInstance()

     var intervalMax = 0
     var intervalMin = 0
     var durationMax = 0
     var durationMin = 0

    fun setRange(info: DeviceInfo) {
        val intervalRange = info.keyInterval.split("~")
        intervalMin = intervalRange[0].toInt()
        intervalMax = intervalRange[1].toInt()
        val durationRange = info.keyDuration.split("~")
        durationMin = durationRange[0].toInt()
        durationMax = durationRange[1].toInt()
    }

    fun setInterval(interval: String?) {
        mBinding.interval.setText(interval)
    }

    fun setDuration(duration: String?) {
        mBinding.duration.setText(duration)
    }

    fun getInterval(): Int {
        LogUtils.e(TAG,"getInterval interval => ${mBinding.interval}")
        return Integer.valueOf(mBinding.interval.text.toString())
    }

    fun getDuration(): Int {
        return Integer.valueOf(mBinding.duration.text.toString())
    }

    private var mBinding: KeycfgEditViewBinding =
        KeycfgEditViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        val label = typedArray.getString(R.styleable.LabelEditView_labelText)
        mBinding.intervalLabel.text = label
        mBinding.interval.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (mBinding.interval.text.toString().isEmpty()) {
                    return@setOnFocusChangeListener
                }
                if (mBinding.interval.text.toString().toInt() < intervalMin && mBinding.interval.text.toString().toInt() != 0) {
                    mBinding.interval.setText(intervalMin.toString())
                } else {
                    fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
                }
            } else {
                fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
            }
        }

        mBinding.interval.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && (s.toString().toInt() >= intervalMin || s.toString().toInt() == 0) && s.toString().toInt() <= intervalMax) {
                    fscBeaconCentralApi.setKeyConfig(s.toString(), mBinding.duration.text.toString())
                } else {
                    if (s.toString().isNotEmpty() && s.toString().toInt() > intervalMax) {
                        mBinding.interval.setText(intervalMax.toString())
                        fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
                    } else {
                        fscBeaconCentralApi.setKeyConfig(s.toString(), mBinding.duration.text.toString())
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        mBinding.duration.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (mBinding.duration.text.toString().isEmpty()) {
                    return@setOnFocusChangeListener
                }
                if (mBinding.duration.text.toString().toInt() < durationMin && mBinding.duration.text.toString().toInt() != 0) {
                    mBinding.duration.setText(durationMin.toString())
                } else {
                    fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
                }
            } else {
                fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
            }
        }

        mBinding.duration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && (s.toString().toInt() >= durationMin || s.toString().toInt() == 0) && s.toString().toInt() <= intervalMax) {
                    fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
                } else {
                    if (s.toString().isNotEmpty() && s.toString().toInt() > durationMax) {
                        mBinding.duration.setText(durationMax.toString())
                        fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), mBinding.duration.text.toString())
                    } else {
                        fscBeaconCentralApi.setKeyConfig(mBinding.interval.text.toString(), s.toString())
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        typedArray.recycle()
    }
}