package com.feasycom.feasybeacon.ui.about

import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Build.BRAND
import android.os.Build.MODEL
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.feasybeacon.App
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivityFeedbackBinding
import com.feasycom.feasybeacon.databinding.HeaderBinding
import com.feasycom.feasybeacon.logic.BeaconRepository.FEEDBACK_ADVICE
import com.feasycom.feasybeacon.logic.BeaconRepository.FEEDBACK_BUG
import com.feasycom.feasybeacon.logic.BeaconRepository.FEEDBACK_COOPERATION
import com.feasycom.feasybeacon.logic.BeaconRepository.FEEDBACK_UI
import com.feasycom.feasybeacon.logic.network.NetworkUtil
import com.feasycom.feasybeacon.logic.network.NetworkUtil.FeedbackCallback
import com.feasycom.feasybeacon.ui.base.BaseActivity
import com.feasycom.feasybeacon.ui.dialog.LoadDialogFragment
import com.feasycom.feasybeacon.ui.dialog.TipsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 创建反馈活动
 */

class FeedbackActivity : BaseActivity<ActivityFeedbackBinding>() {

    private lateinit var waitDialog: LoadDialogFragment

    private lateinit var headerBinding: HeaderBinding

    // 请求网址
    private var urlString = "https://feasycomm.com/app/sdk/statistics/add"

    // 声明延迟初始化的 viewModel 属性，并通过 viewModels() 扩展函数初始化
    private val viewModel: FeedbackViewModel by viewModels()

    // 获取视图绑定对象的方法
    override fun getViewBinding() = ActivityFeedbackBinding.inflate(layoutInflater)

    // 初始化视图的方法
    override fun initView() {
        headerBinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))

        // 设置标题和返回按钮的文本
        headerBinding.apply {
            headerTitle.text = getString(R.string.feedback)
            headerLeft.text = getString(R.string.feedback_left)
            headerLeft.setOnClickListener { finish() }
        }
    }


    private var mPreferenceKey = "FEEDBACK_ADVICE"

    // 初始化事件的方法
    override fun initEvent() {
        var fbType = "建议"
        binding.proposal.setOnClickListener {
            fbType = "建议"
            mPreferenceKey = "FEEDBACK_ADVICE"
            binding.abnormalFunction.setChecked(false)
            binding.interfaceAbnormality.setChecked(false)
            binding.cooperation.setChecked(false)
        }


        binding.abnormalFunction.setOnClickListener {
            fbType = "功能异常"
            mPreferenceKey = "FEEDBACK_BUG"
            binding.proposal.setChecked(false)
            binding.interfaceAbnormality.setChecked(false)
            binding.cooperation.setChecked(false)
        }
        binding.interfaceAbnormality.setOnClickListener {
            fbType = "界面异常"
            mPreferenceKey = "FEEDBACK_UI"
            binding.abnormalFunction.setChecked(false)
            binding.proposal.setChecked(false)
            binding.cooperation.setChecked(false)
        }
        binding.cooperation.setOnClickListener {
            fbType = "合作"
            mPreferenceKey = "FEEDBACK_COOPERATION"
            binding.abnormalFunction.setChecked(false)
            binding.interfaceAbnormality.setChecked(false)
            binding.proposal.setChecked(false)
        }


        // 设置提交按钮的点击事件
        binding.submit.setOnClickListener {
            // 获取反馈文本并检查是否为空
            val feedbackText = binding.dataEv.text.toString().trim()
            if (feedbackText.isEmpty()) {
                ToastUtils.showLong(R.string.feedback_data_error)

            } else if (binding.editEmail.text.toString().isEmpty()) {
                ToastUtils.showLong(R.string.feedback_email)

            }else if (!RegexUtils.isEmail(binding.editEmail.text.toString())) {
                ToastUtils.showLong(R.string.feedback_email_erro)
            }else if (feedbackText.isNotEmpty() && binding.editEmail.text.toString().isNotEmpty()) {
                    // 显示等待对话框
                    waitDialog = LoadDialogFragment(getString(R.string.feedback_status))
                    waitDialog.show(supportFragmentManager, "feedback")


                    NetworkUtil.feedback(
                        feedbackText,
                        fbType,
                        binding.editEmail.text.toString(),
                        this,
                        object : FeedbackCallback {
                            override fun onSuccess(response: String?) {
                                // 关闭等待对话框
                                waitDialog.dismiss()
                                runOnUiThread {
                                    binding.editEmail.setText("")
                                    binding.dataEv.setText("")
                                    val tipsDialog = TipsDialog(this@FeedbackActivity)
                                    tipsDialog.setInfo(
                                        getString(R.string.feedback_success),
                                        binding.dataEv
                                    )
                                    tipsDialog.show()
                                }
                            }

                            override fun onFailure(error: String?) {
                                // 关闭等待对话框
                                waitDialog.dismiss()
                                runOnUiThread {
                                    val tipsDialog = TipsDialog(this@FeedbackActivity)
                                    tipsDialog.setInfo(
                                        getString(R.string.feedback_failure),
                                        binding.dataEv
                                    )
                                    tipsDialog.show()
                                }
                            }

                            override fun onNetworkError(errorMessage: String?) {
                                // 关闭等待对话框
                                waitDialog.dismiss()
                                runOnUiThread {
                                    val tipsDialog = TipsDialog(this@FeedbackActivity)
                                    tipsDialog.setInfo(
                                        getString(R.string.network_error),
                                        binding.dataEv
                                    )
                                    tipsDialog.show()
                                }
                            }

                        })

                }
        }
    }
}
