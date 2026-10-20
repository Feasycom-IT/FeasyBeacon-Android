package com.feasycom.feasybeacon.ui.addbeacon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.feasycom.bean.BeaconBean
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.bean.FeasyBeacon
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivityAddBeaconBinding
import com.feasycom.feasybeacon.databinding.HeaderBinding
import com.feasycom.feasybeacon.ui.base.BaseActivity

class AddBeaconActivity : BaseActivity<ActivityAddBeaconBinding>() {

    private lateinit var beaconBean: BeaconBean
    private lateinit var mDevice: BluetoothDeviceWrapper
    private lateinit var beaconTypeList: ArrayList<String>

    private lateinit var headerBinding: HeaderBinding

    override fun getViewBinding() = ActivityAddBeaconBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beaconBean = BeaconBean()
        with(binding) {
            settingParameterIbeacon.setBeaconBean(beaconBean)
            settingParameterEddystoneUid.setBeaconBean(beaconBean)
            settingParameterEddystoneUrl.setBeaconBean(beaconBean)
            settingParameterAltbeacon.setBeaconBean(beaconBean)
            settingParameterCustomBeacon.setBeaconBean(beaconBean)
//            settingParameterAoabeacon.setBeaconBean(beaconBean)
        }


    }

    override fun initView() {
        mDevice = intent.getSerializableExtra("dev_") as BluetoothDeviceWrapper
        headerBinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))

        with(headerBinding) {
            headerTitle.text = resources.getString(R.string.add_beacon_title)
            headerLeft.text = resources.getString(R.string.back)
            headerRight.text = resources.getString(R.string.add_beacon_right)

            if (mDevice.feasyBeacon.module.equals("120")) {
                beaconTypeList =
                    arrayListOf(" ", "UID", "URL", "IBeacon", "AltBeacon")
            } else {
                beaconTypeList =
                    arrayListOf(" ", "UID", "URL", "IBeacon", "AltBeacon", "Custom")
            }

            binding.beaconType.adapter = ArrayAdapter(
                this@AddBeaconActivity,
                android.R.layout.simple_spinner_dropdown_item,
                beaconTypeList
            )

        }
    }

    override fun initEvent() {
        with(headerBinding) {
            headerRight.setOnClickListener {
                val intent = Intent().apply {
                    putExtra("beaconBean", beaconBean)
                }
                setResult(REQUEST_BEACON_ADD_OK, intent)
                finish()
            }
            headerLeft.setOnClickListener { finish() }
            binding.beaconType.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (mDevice.name.contains("BP104EL")) {
                            beaconBean.parentName = "BP104EL"
                        } else if (mDevice.name.contains("BP104EA")) {
                            beaconBean.parentName = "BP104EA"
                        } else if (mDevice.name.contains("BP104E 2nd")) {
                            beaconBean.parentName = "BP104E 2nd"
                        }
                        when (position) {
                            0 -> hideAllParameterViews()
                            1 -> showEddystoneUidParameterView()
                            2 -> showEddystoneUrlParameterView()
                            3 -> showIBeaconParameterView()
                            4 -> showAltBeaconParameterView()
                            5 -> showCustomBeaconParameterView()
//                            6 -> showAoaBeaconParameterView()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }
    }

    private fun hideAllParameterViews() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_NULL
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterCustomBeacon.visibility = View.GONE
//            settingParameterAoabeacon.visibility = View.GONE
        }
    }

    private fun showEddystoneUidParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_EDDYSTONE_UID
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.VISIBLE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterCustomBeacon.visibility = View.GONE
//            settingParameterAoabeacon.visibility = View.GONE
            settingParameterEddystoneUid.setBeaconInfo(beaconBean)
            settingParameterEddystoneUid.setShowType(1)
        }
    }

    private fun showEddystoneUrlParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_EDDYSTONE_URL
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.VISIBLE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterCustomBeacon.visibility = View.GONE
//            settingParameterAoabeacon.visibility = View.GONE
            settingParameterEddystoneUrl.setBeaconInfo(beaconBean)
            settingParameterEddystoneUrl.setShowType(1)
        }
    }

    private fun showIBeaconParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_I_BEACON
            settingParameterIbeacon.visibility = View.VISIBLE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterCustomBeacon.visibility = View.GONE
//            settingParameterAoabeacon.visibility = View.GONE
            settingParameterIbeacon.setBeaconInfo(beaconBean)
            settingParameterIbeacon.setShowType(1)
        }
    }

    private fun showAltBeaconParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_ALTBEACON
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.VISIBLE
            settingParameterCustomBeacon.visibility = View.GONE
//            settingParameterAoabeacon.visibility = View.GONE
            settingParameterAltbeacon.setBeaconInfo(beaconBean)
            settingParameterAltbeacon.setShowType(1)
        }
    }

    private fun showCustomBeaconParameterView() {
        with(binding) {
            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_CUSTOM
            settingParameterIbeacon.visibility = View.GONE
            settingParameterEddystoneUid.visibility = View.GONE
            settingParameterEddystoneUrl.visibility = View.GONE
            settingParameterAltbeacon.visibility = View.GONE
            settingParameterCustomBeacon.visibility = View.VISIBLE
//            settingParameterAoabeacon.visibility = View.GONE
            settingParameterCustomBeacon.setBeaconInfo(beaconBean)
        }
    }

//    private fun showAoaBeaconParameterView() {
//        with(binding) {
//            beaconBean!!.beaconType = FeasyBeacon.BEACON_TYPE_AOABEACON
//            settingParameterIbeacon.visibility = View.GONE
//            settingParameterEddystoneUid.visibility = View.GONE
//            settingParameterEddystoneUrl.visibility = View.GONE
//            settingParameterAltbeacon.visibility = View.GONE
//            settingParameterAoabeacon.visibility = View.VISIBLE
//            settingParameterCustomBeacon.visibility = View.GONE
//            settingParameterAoabeacon.setBeaconInfo(beaconBean)
//            settingParameterAoabeacon.setShowType(1)
//        }
//    }

    companion object {
        const val REQUEST_BEACON_ADD_OK = 2
    }

}