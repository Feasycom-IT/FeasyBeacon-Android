package com.feasycom.feasybeacon.ui.addbeacon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.feasycom.bean.BeaconBean
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.bean.FeasyBeacon
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivityAddConditionBinding
import com.feasycom.feasybeacon.databinding.HeaderBinding
import com.feasycom.feasybeacon.ui.base.BaseActivity

class AddConditionActivity: BaseActivity<ActivityAddConditionBinding>() {

    private lateinit var beaconBean: BeaconBean
    private var haveGsensor: Boolean = false
    private var beaconTypeList = arrayListOf(" ", "UID", "URL", "IBeacon", "AltBeacon", "Custom")
    private lateinit var headerBinding: HeaderBinding
    override fun getViewBinding() = ActivityAddConditionBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        haveGsensor = intent.getBooleanExtra("haveGsensor",false)

        beaconBean = BeaconBean()
        beaconBean.isHaveGsensor = haveGsensor
        with(binding) {
            settingConditionParameterIbeacon.setBeaconBean(beaconBean)
            settingConditionParameterEddystoneUid.setBeaconBean(beaconBean)
            settingConditionParameterEddystoneUrl.setBeaconBean(beaconBean)
            settingConditionParameterAltbeacon.setBeaconBean(beaconBean)
            settingParameterCustomCondition.setBeaconBean(beaconBean)
        }
    }

    override fun initView() {
        headerBinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))

        with(headerBinding) {
            headerTitle.text = resources.getString(R.string.add_condition_title)
            headerLeft.text = resources.getString(R.string.back)
            headerRight.text = resources.getString(R.string.add_beacon_right)
            binding.beaconType.adapter = ArrayAdapter(this@AddConditionActivity, android.R.layout.simple_spinner_dropdown_item, beaconTypeList)
        }
    }

    override fun initEvent() {
        with(headerBinding) {
            headerRight.setOnClickListener {
                beaconBean.customBeaconData = beaconBean.customConditionData
                val intent = Intent().apply {
                    putExtra("conditionBeaconBean",beaconBean)
                }
                setResult(REQUEST_CONDITION_ADD_OK, intent)
                finish()
            }
            headerLeft.setOnClickListener { finish() }
            binding.beaconType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    when (p2) {
                        0 -> hideAllConditionParameterViews()
                        1 -> showConditionEddystoneUidParameterView()
                        2 -> showConditionEddystoneUrlParameterView()
                        3 -> showConditionIBeaconParameterView()
                        4 -> showConditionAltBeaconParameterView()
                        5 -> showCustomConditionParameterView()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
    }

    private fun hideAllConditionParameterViews() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_NULL
            settingConditionParameterIbeacon.visibility = View.GONE
            settingConditionParameterEddystoneUid.visibility = View.GONE
            settingConditionParameterEddystoneUrl.visibility = View.GONE
            settingConditionParameterAltbeacon.visibility = View.GONE
            settingParameterCustomCondition.visibility = View.GONE
        }
    }

    private fun showConditionEddystoneUidParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.CONDITION_BEACON_TYPE_EDDYSTONE_UID
            settingConditionParameterIbeacon.visibility = View.GONE
            settingConditionParameterEddystoneUid.visibility = View.VISIBLE
            settingConditionParameterEddystoneUrl.visibility = View.GONE
            settingConditionParameterAltbeacon.visibility = View.GONE
            settingParameterCustomCondition.visibility = View.GONE
            settingConditionParameterEddystoneUid.setBeaconInfo(beaconBean)
        }
    }

    private fun showConditionEddystoneUrlParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.CONDITION_BEACON_TYPE_EDDYSTONE_URL
            settingConditionParameterIbeacon.visibility = View.GONE
            settingConditionParameterEddystoneUid.visibility = View.GONE
            settingConditionParameterEddystoneUrl.visibility = View.VISIBLE
            settingConditionParameterAltbeacon.visibility = View.GONE
            settingParameterCustomCondition.visibility = View.GONE
            settingConditionParameterEddystoneUrl.setBeaconInfo(beaconBean)
        }
    }

    private fun showConditionIBeaconParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.CONDITION_BEACON_TYPE_I_BEACON
            settingConditionParameterIbeacon.visibility = View.VISIBLE
            settingConditionParameterEddystoneUid.visibility = View.GONE
            settingConditionParameterEddystoneUrl.visibility = View.GONE
            settingConditionParameterAltbeacon.visibility = View.GONE
            settingParameterCustomCondition.visibility = View.GONE
            settingConditionParameterIbeacon.setBeaconInfo(beaconBean)
        }
    }

    private fun showConditionAltBeaconParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.CONDITION_BEACON_TYPE_ALTBEACON
            settingConditionParameterIbeacon.visibility = View.GONE
            settingConditionParameterEddystoneUid.visibility = View.GONE
            settingConditionParameterEddystoneUrl.visibility = View.GONE
            settingConditionParameterAltbeacon.visibility = View.VISIBLE
            settingParameterCustomCondition.visibility = View.GONE
            settingConditionParameterAltbeacon.setBeaconInfo(beaconBean)
        }
    }

    private fun showCustomConditionParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.CONDITION_BEACON_TYPE_CUSTOM
            settingConditionParameterIbeacon.visibility = View.GONE
            settingConditionParameterEddystoneUid.visibility = View.GONE
            settingConditionParameterEddystoneUrl.visibility = View.GONE
            settingConditionParameterAltbeacon.visibility = View.GONE
            settingParameterCustomCondition.visibility = View.VISIBLE
            settingParameterCustomCondition.setBeaconInfo(beaconBean)
        }
    }

    companion object {
        const val REQUEST_CONDITION_ADD_OK = 1002
    }

}