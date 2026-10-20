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
import com.feasycom.feasybeacon.logic.model.DeviceInfo
import com.feasycom.feasybeacon.ui.dialog.ConditionDeleteDialog

sealed class ConditionBeaconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class ConditionIBeaconViewHolder(val mBinding: SettingConditionIbeaconParameterInfoBinding) :
    ConditionBeaconViewHolder(mBinding.root)

class ConditionEddystoneUrlViewHolder(val mBinding: SettingConditionEddystoneUrlParameterInfoBinding) :
    ConditionBeaconViewHolder(mBinding.root)

class ConditionEddystoneUidViewHolder(val mBinding: SettingConditionEddystoneUidParameterInfoBinding) :
    ConditionBeaconViewHolder(mBinding.root)

class ConditionAltBeaconViewHolder(val mBinding: SettingConditionAltbeaconParameterInfoBinding) :
    ConditionBeaconViewHolder(mBinding.root)

class ConditionCustomBeaconViewHolder(val mBinding: SettingCustomConditionParameterInfoBinding) :
    ConditionBeaconViewHolder(mBinding.root)

class ConditionNullViewHolder(val mBinding: SettingNullParameterInfoBinding) :
    ConditionBeaconViewHolder(mBinding.root)






/**
 * Copyright 2017 Shenzhen Feasycom Technology co.,Ltd
 */
class SettingConditionParameterAdapter(
    val context: Context, private var conditionBeacons: ArrayList<BeaconBean>
) : RecyclerView.Adapter<ConditionBeaconViewHolder>() {


    private val mConditionDeleteDialog by lazy {
        ConditionDeleteDialog(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConditionBeaconViewHolder {

        return when (viewType) {
            CONDITION_BEACON_TYPE_I_BEACON -> {
                val binding = SettingConditionIbeaconParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ConditionIBeaconViewHolder(binding)
            }
            CONDITION_BEACON_TYPE_EDDYSTONE_URL -> {
                val binding = SettingConditionEddystoneUrlParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ConditionEddystoneUrlViewHolder(binding)
            }
            CONDITION_BEACON_TYPE_EDDYSTONE_UID -> {
                val binding = SettingConditionEddystoneUidParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ConditionEddystoneUidViewHolder(binding)
            }
            CONDITION_BEACON_TYPE_ALTBEACON -> {
                val binding = SettingConditionAltbeaconParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ConditionAltBeaconViewHolder(binding)
            }
            CONDITION_BEACON_TYPE_CUSTOM_BEACON -> {
                val binding = SettingCustomConditionParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ConditionCustomBeaconViewHolder(binding)
            }
            else -> {
                val binding = SettingNullParameterInfoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ConditionNullViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ConditionBeaconViewHolder, position: Int) {
        val beacon = conditionBeacons[position]
        val index = (position + 1).toString()
        when (holder) {
            is ConditionIBeaconViewHolder -> {
                holder.mBinding.settingConditionParameterIbeacon.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is ConditionEddystoneUrlViewHolder -> {
                holder.mBinding.settingConditionParameterEddystoneUrl.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is ConditionEddystoneUidViewHolder -> {
                holder.mBinding.settingConditionParameterEddystoneUid.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is ConditionAltBeaconViewHolder -> {
                holder.mBinding.settingConditionParameterAltbeacon.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is ConditionCustomBeaconViewHolder -> {
                LogUtils.d(TAG, "FeasyBeacon APP ConditionCustomBeaconViewHolder customConditionData => ${beacon.customConditionData}")
                holder.mBinding.settingConditionParameterCustomBeacon.setBeaconInfo(beacon)
                holder.mBinding.image.setImageResource(R.drawable.x)
                holder.mBinding.image.setOnClickListener {
                    deleteBeacon(position)
                }
                holder.mBinding.beaconIndex.text = index
                holder.mBinding.beaconTitle.text = beacon.beaconType
            }
            is ConditionNullViewHolder -> {
                LogUtils.e(
                    TAG,
                    "FeasyBeacon APP SettingConditionParameterAdapter onBindViewHolder: NullViewHolder"
                )
                return
            }
        }
    }

    private fun deleteBeacon(position: Int) {
        LogUtils.e(
            TAG,
            "FeasyBeacon APP SettingBeaconParameterAdapter deleteBeacon position => $position"
        )
        val beacon = conditionBeacons[position]
        mConditionDeleteDialog.beaconBean = beacon
        mConditionDeleteDialog.show()
    }

    override fun getItemCount(): Int {
        return conditionBeacons.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (conditionBeacons[position].beaconType) {
            FeasyBeacon.CONDITION_BEACON_TYPE_I_BEACON -> {
                CONDITION_BEACON_TYPE_I_BEACON
            }
            FeasyBeacon.CONDITION_BEACON_TYPE_EDDYSTONE_URL -> {
                CONDITION_BEACON_TYPE_EDDYSTONE_URL
            }
            FeasyBeacon.CONDITION_BEACON_TYPE_EDDYSTONE_UID -> {
                CONDITION_BEACON_TYPE_EDDYSTONE_UID
            }
            FeasyBeacon.CONDITION_BEACON_TYPE_ALTBEACON -> {
                CONDITION_BEACON_TYPE_ALTBEACON
            }
            FeasyBeacon.CONDITION_BEACON_TYPE_CUSTOM -> {
                CONDITION_BEACON_TYPE_CUSTOM_BEACON
            }
            else -> {
                CONDITION_BEACON_TYPE_OTHER
            }
        }
    }

    companion object {
        private const val TAG = "SettingConditionParameterAdapter"
        const val CONDITION_BEACON_TYPE_I_BEACON = 1
        const val CONDITION_BEACON_TYPE_EDDYSTONE_URL = 2
        const val CONDITION_BEACON_TYPE_EDDYSTONE_UID = 3
        const val CONDITION_BEACON_TYPE_ALTBEACON = 4
        const val CONDITION_BEACON_TYPE_CUSTOM_BEACON = 5
        const val CONDITION_BEACON_TYPE_OTHER = 6
    }

}