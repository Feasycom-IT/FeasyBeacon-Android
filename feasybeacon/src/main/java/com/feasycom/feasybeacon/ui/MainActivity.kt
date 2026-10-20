package com.feasycom.feasybeacon.ui

import com.feasycom.feasybeacon.databinding.ActivityMainBinding
import com.feasycom.feasybeacon.ui.base.BaseActivity

/**
 * 主界面
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)
}
