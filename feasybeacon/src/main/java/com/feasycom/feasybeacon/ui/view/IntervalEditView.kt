package com.feasycom.feasybeacon.ui.view

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.feasycom.controler.beacon.FscBeaconCentralApi
import com.feasycom.controler.beacon.FscBeaconCentralApiImp
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.IntervalEditViewBinding

class IntervalEditView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {

    private var mBinding: IntervalEditViewBinding =
        IntervalEditViewBinding.inflate(LayoutInflater.from(context), this)

    private val fscBeaconCentralApi: FscBeaconCentralApi = FscBeaconCentralApiImp.getInstance()

    var intervalMax = 10 * 1000
    var intervalMin = 100

    private var first = true

    fun setRange(intervalRange: Array<String>) {
        intervalMin = intervalRange[0].toInt()
        intervalMax = intervalRange[1].toInt()
    }

    fun setInterval(interval: String?) {
        mBinding.intervalEdit.setText(interval)
    }

    val interval: Int
        get() = mBinding.intervalEdit.text.toString().toInt()

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LabelEditView)
        val label = typedArray.getString(R.styleable.LabelEditView_labelText)
        mBinding.intervalLabel.text = label
        typedArray.recycle()
        mBinding.intervalEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val input = s.toString().toIntOrNull() ?: return
                when {
                    input == 0 || (input in intervalMin..10000) -> {
                        mBinding.intervalEdit.setTextColor(Color.BLACK)
                        if (!first) {
                            fscBeaconCentralApi.setBroadcastInterval(input.toString())
                        } else {
                            first = false
                        }
                    }
                    input > 10000 -> {
                        mBinding.intervalEdit.setTextColor(Color.RED)
                    }
                    else -> {
                        mBinding.intervalEdit.setTextColor(Color.RED)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        mBinding.intervalEdit.onFocusChangeListener = OnFocusChangeListener { v1: View?, hasFocus: Boolean ->
            if (!hasFocus) {
                if (mBinding.intervalEdit.text.toString().isEmpty()) {
                    return@OnFocusChangeListener
                }
                if (mBinding.intervalEdit.text.toString().isEmpty() || mBinding.intervalEdit.text.toString().toInt() < intervalMin && mBinding.intervalEdit.text.toString().toInt() != 0) {
                    mBinding.intervalEdit.setText(intervalMin.toString())
                } else {
                    fscBeaconCentralApi.setBroadcastInterval(mBinding.intervalEdit.text.toString())
                }
            } else {
                fscBeaconCentralApi.setBroadcastInterval(mBinding.intervalEdit.text.toString())
            }
        }
    }

}