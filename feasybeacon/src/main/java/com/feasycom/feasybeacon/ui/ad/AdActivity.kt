package com.feasycom.feasybeacon.ui.ad

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.feasycom.feasybeacon.databinding.FragmentAdBinding
import com.feasycom.feasybeacon.logic.utils.getStr
import com.feasycom.feasybeacon.ui.MainActivity
import com.feasycom.feasybeacon.ui.base.BaseActivity2

class AdActivity : BaseActivity2<FragmentAdBinding>() {

    // 修复：将 lateinit 改为可空类型，避免未初始化崩溃
    private var countDownTimer: CountDownTimer? = null

    // 获取 ViewBinding 对象的方法
    override fun getViewBinding() = FragmentAdBinding.inflate(layoutInflater)

    override fun initView() {

        loadCachedAdData()
    }



    override fun onResume() {
        super.onResume()
        initCountDownTimer()
    }

    private var remainingTime: Long = 5000 // 总时长5秒
    private fun initCountDownTimer() {
        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                binding.toMain.text = "${millisUntilFinished / 1000} s"
            }

            override fun onFinish() {
                navigateToBeaconActivity()
            }
        }
        binding.toMain.setOnClickListener {
            navigateToBeaconActivity()
        }
        countDownTimer?.start()
    }

    private fun isChineseLanguage(): Boolean {
        return resources.configuration.locales[0].language.endsWith("zh")
    }

    private fun loadCachedAdData() {
        val isChinese = isChineseLanguage()
        val imageKey = if (isChinese) "lanch1" else "lanch2"
        val urlKeys = if (isChinese) listOf("cnurl1", "cnurl2", "cnurl3")
        else listOf("enurl1", "enurl2", "enurl3")

        val cachedImagePath = getStr(imageKey)
        val cachedUrls = urlKeys.map { getStr(it) }

        if (TextUtils.isEmpty(cachedImagePath)) {
            // 修复：安全调用，避免空指针
            countDownTimer?.cancel()
            navigateToBeaconActivity()
        } else {
            LogUtils.d(TAG, "APP AD load cached image => $cachedImagePath")
            val options = RequestOptions()
                .fitCenter()

            Glide.with(this)
                .load(cachedImagePath)
                .apply(options)
                .into(binding.adImg)

            cachedUrls.forEachIndexed { index, url ->
                if (!TextUtils.isEmpty(url)) {
                    val button = when (index) {
                        0 -> binding.button1Area
                        1 -> binding.button2Area
                        else -> binding.button3Area
                    }
                    setupAdButton(button, url!!)
                }
            }

        }
    }

    private fun setupAdButton(buttonArea: View, url: String) {
        buttonArea.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                if (intent.resolveActivity(packageManager) != null) {
                    // 修复：安全调用
                    countDownTimer?.cancel()
                    startActivity(intent)
                } else {
                    ToastUtils.showShort("未找到浏览器应用")
                }
            } catch (e: Exception) {
                ToastUtils.showShort("打开链接失败: ${e.message}")
            }
        }
    }


    private var gg: Boolean = true

    /**
     * 跳转到 Beacon 页面
     */
    private fun navigateToBeaconActivity() {
        if (gg) {
            gg = false
            Log.e("vvvv", "============跳转到 Beacon 页面=========")
            // 修复：页面跳转前先取消倒计时
            countDownTimer?.cancel()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 修复：安全销毁
        countDownTimer?.cancel()
        countDownTimer = null
    }

    companion object {
        const val TAG = "AdActivity"
    }
}