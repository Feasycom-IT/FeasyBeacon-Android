package com.feasycom.feasybeacon.ui.ad

import androidx.lifecycle.ViewModel
import com.feasycom.feasybeacon.logic.BeaconRepository

class AdViewModel: ViewModel() {
    val mAdLiveData = BeaconRepository.getAdData()
}