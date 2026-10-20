package com.feasycom.feasybeacon.ui.setting

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivityScancodeBinding
import com.feasycom.feasybeacon.ui.base.BaseActivity
import com.google.zxing.BarcodeFormat
import com.mylhyl.zxing.scanner.OnScannerCompletionListener
import com.mylhyl.zxing.scanner.decode.DecodeFormatManager

class ScanQrActivity : BaseActivity<ActivityScancodeBinding>() {


    override fun getViewBinding() = ActivityScancodeBinding.inflate(layoutInflater)

    override fun initView() {
        super.initView()
        binding.header.apply {
            headerLeft.text = resources.getString(R.string.back)
            headerTitle.text = getString(R.string.scaning)
            // 返回按钮点击事件
            headerLeft.setOnClickListener { finish() }
        }

    }


    override fun initEvent() {
        super.initEvent()

        binding.scannerView.setOnScannerCompletionListener(OnScannerCompletionListener { result1, result2, bitmap ->

            if (TextUtils.isEmpty(result1.text)) {
                ToastUtils.showLong("Please rescan！")
                binding.scannerView.restartPreviewAfterDelay(1000)
            } else {
             /*   val intent = Intent()
                intent.putExtra("str_data", result1.text)
                setResult(Activity.RESULT_OK, intent)
                finish()*/

                ScanCallback.getInstance().scall.result(result1.text)
          /*      val intent = Intent()
                intent.putExtra(
                    "code_type", 888
                )
                intent.putExtra("code", result1.text)
                setResult(Activity.RESULT_OK, intent)*/
                finish()


            }

        })
    }





    override fun onResume() {
        super.onResume()
        binding.scannerView.onResume()
    }


    override fun onPause() {
        super.onPause()
        binding.scannerView.onPause()
    }


}