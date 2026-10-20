package com.feasycom.feasybeacon.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.LogUtils
import com.feasycom.controler.beacon.FscBeaconCentralApi
import com.feasycom.controler.beacon.FscBeaconCentralApiImp
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.GsensoSpinnerViewBinding
import com.feasycom.feasybeacon.logic.model.DeviceInfo

class GsensorEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {
    var advinMin: Int = 0
    var advinMax: Int = 0
    var durationMin: Int = 0
    var durationMax: Int = 0
    var sensitivityMin: Int = 0
    var sensitivityMax: Int = 0
    private val fscBeaconCentralApi: FscBeaconCentralApi = FscBeaconCentralApiImp.getInstance()

    fun setRange(deviceInfo: DeviceInfo) {
        val gSensorInterval: String = deviceInfo.gsensorInterval
        if (gSensorInterval.isNotEmpty()) {
            val intervalRange = gSensorInterval.split("~").toTypedArray()
            advinMin = intervalRange[0].toInt()
            advinMax = intervalRange[1].toInt()
        }
        val gSensorDuration: String = deviceInfo.gsensorDuration
        if (gSensorDuration.isNotEmpty()) {
            val durationRange = gSensorDuration.split("~").toTypedArray()
            durationMin = durationRange[0].toInt()
            durationMax = durationRange[1].toInt()
        }
        val gSensorSensitivity: String = deviceInfo.gsensorSensitivity
        if (gSensorSensitivity.isNotEmpty()) {
            val sensitivityRange = gSensorSensitivity.split("~").toTypedArray()
            sensitivityMin = sensitivityRange[0].toInt()
            sensitivityMax = sensitivityRange[1].toInt()
        }
    }

    fun setAdvin(advin: Int) {
        mBinding.advinEdit.setText(advin.toString())
    }

    fun getAdvin(): Int {
        if (mBinding.advinEdit.text.toString().isEmpty()) {
            mBinding.advinEdit.setText("0")
            return 0
        }
        return mBinding.advinEdit.text.toString().toInt()
    }

    fun setDuration(duration: Int) {
        LogUtils.d(TAG,"setDuration duration => $duration")
        mBinding.durationEdit.setText(duration.toString())
    }

    fun getDuration(): Int {
        if (mBinding.durationEdit.text.toString().isEmpty()) {
            mBinding.durationEdit.setText(durationMin!!)
            return 0
        }
        return mBinding.durationEdit.text.toString().toInt()
    }

    fun getSensitivityEdit(): Int {
        if (mBinding.sensitivityEdit.text.toString().isEmpty()) {
            mBinding.sensitivityEdit.setText(durationMin!!)
            return 0
        }
        return mBinding.sensitivityEdit.text.toString().toInt()
    }

    fun setSensitivityEdit(sensitivity: Int) {
        mBinding.sensitivityEdit.setText(sensitivity.toString())
        mBinding.sensitivityEdit.visibility = VISIBLE
    }

    private fun setGSensorConfig(broadcastInterval: String?, broadcastDuration: String?) {
        fscBeaconCentralApi.setGSensorConfig(broadcastInterval, broadcastDuration)
    }

    private fun setGSensorConfig(
        broadcastInterval: String?, broadcastDuration: String?, threshold: String?
    ) {
        fscBeaconCentralApi.setGSensorConfig(broadcastInterval, broadcastDuration, threshold)
    }

    private var mBinding: GsensoSpinnerViewBinding =
        GsensoSpinnerViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        val label = typedArray.getString(R.styleable.LabelEditView_labelText)
        mBinding.intervalLabel.text = label

        mBinding.advinEdit.addTextChangedListener(onTextChanged = { s, _, _, _ ->
            if (s.toString().isNotEmpty() && (s.toString().toInt() >= advinMin!! || s.toString()
                    .toInt() == 0) && s.toString().toInt() <= advinMax!!
            ) {
                if (mBinding.sensitivityEdit.visibility == GONE) {
                    setGSensorConfig(s.toString(), mBinding.durationEdit.text.toString())
                } else {
                    setGSensorConfig(
                        mBinding.advinEdit.text.toString(),
                        mBinding.durationEdit.text.toString(),
                        mBinding.sensitivityEdit.text.toString()
                    )
                }
            } else {
                if (s.toString().isNotEmpty() && s.toString().toInt() > advinMax!!) {
                    mBinding.advinEdit.setText(advinMax.toString())
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                    mBinding.advinEdit.setSelection(mBinding.advinEdit.text.toString().length)
                }
            }
        })

        mBinding.advinEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!mBinding.advinEdit.text.toString().isEmpty()) {
                    val integer = mBinding.advinEdit.text.toString().toInt()
                    if (integer >= 2000) {
                        gsensorSend = true
                    }
                }

                if (mBinding.advinEdit.text.toString()
                        .isEmpty() || mBinding.advinEdit.text.toString()
                        .toInt() < advinMin!! && mBinding.advinEdit.text.toString().toInt() != 0
                ) {
                    mBinding.advinEdit.setText(advinMin.toString())
                    mBinding.advinEdit.setSelection(mBinding.advinEdit.text.toString().length)
                }
            } else {
                if (mBinding.sensitivityEdit.visibility == GONE) {
                    setGSensorConfig(
                        mBinding.advinEdit.text.toString(), mBinding.durationEdit.text.toString()
                    )
                } else {
                    setGSensorConfig(
                        mBinding.advinEdit.text.toString(),
                        mBinding.durationEdit.text.toString(),
                        mBinding.sensitivityEdit.text.toString()
                    )
                }
            }
        }

        mBinding.durationEdit.addTextChangedListener(onTextChanged = { s, _, _, _ ->
            if (s.toString().isNotEmpty() && s.toString().toInt() >= durationMin!! && s.toString()
                    .toInt() <= durationMax!!
            ) {
                if (mBinding.sensitivityEdit.visibility == GONE) {
                    setGSensorConfig(mBinding.advinEdit.text.toString(), s.toString())
                } else {
                    setGSensorConfig(
                        mBinding.advinEdit.text.toString(),
                        mBinding.durationEdit.text.toString(),
                        mBinding.sensitivityEdit.text.toString()
                    )
                }
            } else {
                if (!mBinding.durationEdit.text.toString().isEmpty() && s.toString()
                        .toInt() > durationMax!!
                ) {
//                    mBinding.durationEdit.setText(durationMax.toString())
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                    mBinding.durationEdit.setSelection(mBinding.durationEdit.text.toString().length)
                }
            }
        })

        mBinding.durationEdit.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (mBinding.durationEdit.text.toString()
                        .isEmpty() || mBinding.durationEdit.text.toString().toInt() < durationMin!!
                ) {
                    mBinding.durationEdit.setText(durationMin.toString())
                    mBinding.durationEdit.setSelection(mBinding.durationEdit.text.toString().length)
                } else {
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                }
            }
        }

        mBinding.sensitivityEdit.addTextChangedListener(onTextChanged = { s, _, _, _ ->
            if (s.toString().isNotEmpty() && s.toString()
                    .toInt() >= sensitivityMin!! && s.toString().toInt() <= sensitivityMax!!
            ) {
                setGSensorConfig(
                    mBinding.advinEdit.text.toString(),
                    mBinding.durationEdit.text.toString(),
                    s.toString()
                )
            } else {
                if (mBinding.sensitivityEdit.text.toString().isNotEmpty() && s.toString()
                        .toInt() > sensitivityMin!!
                ) {
                    mBinding.sensitivityEdit.setText(sensitivityMax.toString())
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                    mBinding.durationEdit.setSelection(mBinding.durationEdit.text.toString().length)
                }
            }
        })

        mBinding.sensitivityEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (mBinding.sensitivityEdit.text.toString()
                        .isEmpty() || mBinding.sensitivityEdit.text.toString()
                        .toInt() < sensitivityMin!!
                ) {
                    mBinding.sensitivityEdit.setText(sensitivityMin.toString())
                    mBinding.sensitivityEdit.setSelection(mBinding.sensitivityEdit.text.toString().length)
                } else {
                    if (mBinding.sensitivityEdit.visibility == GONE) {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString()
                        )
                    } else {
                        setGSensorConfig(
                            mBinding.advinEdit.text.toString(),
                            mBinding.durationEdit.text.toString(),
                            mBinding.sensitivityEdit.text.toString()
                        )
                    }
                }
            }
        }
        typedArray.recycle()
    }

    companion object {
        private const val TAG: String = "GensorEditView"
        var gsensorSend = false
    }

}