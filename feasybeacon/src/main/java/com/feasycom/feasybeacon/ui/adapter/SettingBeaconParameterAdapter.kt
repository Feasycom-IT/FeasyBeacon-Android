package com.feasycom.feasybeacon.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.feasycom.bean.BeaconBean
import com.feasycom.bean.FeasyBeacon
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.*
import com.feasycom.feasybeacon.ui.dialog.DeleteDialog

sealed class BeaconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class IBeaconViewHolder(val mBinding: SettingIbeaconParameterInfoBinding) :
    BeaconViewHolder(mBinding.root)

class EddystoneUrlViewHolder(val mBinding: SettingEddystoneUrlParameterInfoBinding) :
    BeaconViewHolder(mBinding.root)

class EddystoneUidViewHolder(val mBinding: SettingEddystoneUidParameterInfoBinding) :
    BeaconViewHolder(mBinding.root)

class AltBeaconViewHolder(val mBinding: SettingAltbeaconParameterInfoBinding) :
    BeaconViewHolder(mBinding.root)

class CustomBeaconViewHolder(val mBinding: SettingCustomBeaconParameterInfoBinding) :
        BeaconViewHolder(mBinding.root)

//class AoaBeaconViewHolder(val mBinding: SettingAoabeaconParameterInfoBinding) :
//    BeaconViewHolder(mBinding.root)

class NullViewHolder(val mBinding: SettingNullParameterInfoBinding) :
    BeaconViewHolder(mBinding.root)

/**
 * Copyright 2017 Shenzhen Feasycom Technology co.,Ltd
 */
class SettingBeaconParameterAdapter(
    val context: Context, private var beacons: ArrayList<BeaconBean>
) : RecyclerView.Adapter<BeaconViewHolder>() {

    private val mDeleteDialog by lazy {
        DeleteDialog(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconViewHolder {
        LogUtils.d(
            TAG,
            "FeasyBeacon APP SettingBeaconParameterAdapter onCreateViewHolder viewType => $viewType"
        )
        return when (viewType) {
            BEACON_TYPE_I_BEACON -> {
                val binding = SettingIbeaconParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                IBeaconViewHolder(binding)
            }
            BEACON_TYPE_EDDYSTONE_URL -> {
                val binding = SettingEddystoneUrlParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                EddystoneUrlViewHolder(binding)
            }
            BEACON_TYPE_EDDYSTONE_UID -> {
                val binding = SettingEddystoneUidParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                EddystoneUidViewHolder(binding)
            }
            BEACON_TYPE_ALTBEACON -> {
                val binding = SettingAltbeaconParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                AltBeaconViewHolder(binding)
            }
            BEACON_TYPE_CUSTOM_BEACON -> {
                val binding = SettingCustomBeaconParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CustomBeaconViewHolder(binding)
            }
//            BEACON_TYPE_AOA_BEACON -> {
//                val binding = SettingAoabeaconParameterInfoBinding.inflate(
//                    LayoutInflater.from(parent.context), parent, false
//                )
//                AoaBeaconViewHolder(binding)
//            }
            else -> {
                val binding = SettingNullParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                NullViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: BeaconViewHolder, position: Int) {
        val beacon = beacons[position]
        val index = (position + 1).toString()
        when (holder) {
            is IBeaconViewHolder -> {
                holder.mBinding.settingParameterIbeacon.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is EddystoneUrlViewHolder -> {
                holder.mBinding.settingParameterEddystoneUrl.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is EddystoneUidViewHolder -> {
                holder.mBinding.settingParameterEddystoneUid.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is AltBeaconViewHolder -> {
                holder.mBinding.settingParameterAltbeacon.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is CustomBeaconViewHolder -> {
                LogUtils.d(TAG, "FeasyBeacon APP CustomBeaconViewHolder customBeaconData => ${beacon.customBeaconData}")
                holder.mBinding.settingParameterCustomBeacon.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
//            is AoaBeaconViewHolder -> {
//                holder.mBinding.settingParameterAoabeacon.setBeaconInfo(beacon)
//                holder.mBinding.image.setImageResource(R.drawable.x)
//                holder.mBinding.image.setOnClickListener {
//                    deleteBeacon(position)
//                }
//                holder.mBinding.beaconIndex.text = index
//                holder.mBinding.beaconTitle.text = beacon.beaconType
//            }
            is NullViewHolder -> {
                LogUtils.e(
                    TAG,
                    "FeasyBeacon APP SettingBeaconParameterAdapter onBindViewHolder: NullViewHolder"
                )
                return
            }
            else -> {}
        }
    }

    private fun deleteBeacon(position: Int) {
        LogUtils.e(
            TAG,
            "FeasyBeacon APP SettingBeaconParameterAdapter deleteBeacon position => $position"
        )
        val beacon = beacons[position]
        mDeleteDialog.beaconBean = beacon
        mDeleteDialog.show()
    }

    override fun getItemCount(): Int {
        return beacons.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (beacons[position].beaconType) {
            FeasyBeacon.BEACON_TYPE_I_BEACON -> {
                BEACON_TYPE_I_BEACON
            }
            FeasyBeacon.BEACON_TYPE_EDDYSTONE_URL -> {
                BEACON_TYPE_EDDYSTONE_URL
            }
            FeasyBeacon.BEACON_TYPE_EDDYSTONE_UID -> {
                BEACON_TYPE_EDDYSTONE_UID
            }
            FeasyBeacon.BEACON_TYPE_ALTBEACON -> {
                BEACON_TYPE_ALTBEACON
            }
            FeasyBeacon.BEACON_TYPE_CUSTOM -> {
                BEACON_TYPE_CUSTOM_BEACON
            }
            FeasyBeacon.BEACON_TYPE_AOABEACON -> {
                BEACON_TYPE_AOA_BEACON
            }
            else -> {
                BEACON_TYPE_OTHER
            }
        }
    }

    companion object {
        private const val TAG = "SettingBeaconParameterA"
        const val BEACON_TYPE_I_BEACON = 1
        const val BEACON_TYPE_EDDYSTONE_URL = 2
        const val BEACON_TYPE_EDDYSTONE_UID = 3
        const val BEACON_TYPE_ALTBEACON = 4
        const val BEACON_TYPE_CUSTOM_BEACON = 5
        const val BEACON_TYPE_AOA_BEACON = 6
        const val BEACON_TYPE_OTHER = 7
    }

}