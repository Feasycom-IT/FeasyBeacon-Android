package com.feasycom.feasybeacon.ui.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.FootBinding
import com.feasycom.feasybeacon.databinding.FragmentAboutBinding
import com.feasycom.feasybeacon.databinding.HeaderBinding
import com.feasycom.feasybeacon.ui.base.BaseFragment

/**
 * 关于页面片段。
 */
class AboutFragment : BaseFragment<FragmentAboutBinding>() {

    private lateinit var headerBinding: HeaderBinding
    private lateinit var footBinding: FootBinding

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentAboutBinding = FragmentAboutBinding.inflate(inflater, container, false)

    override fun initView() {
        headerBinding = HeaderBinding.bind(binding.root.findViewById(R.id.header))
        footBinding = FootBinding.bind(binding.root.findViewById(R.id.foot))
        initializeUI()
    }

    override fun initEvent() {
        initializeEvents()
    }

    override fun onResume() {
        super.onResume()
        footBinding.navigationBar.check(R.id.about_nav)
    }

    /**
     * 初始化界面。FeasyBeaconLibrary-release.aar
     */
    private fun initializeUI() {
        with(binding){
            // 设置导航栏
            footBinding.navigationBar.check(R.id.about_nav)
            // 设置标题和反馈按钮
            headerBinding.headerTitle.text = getString(R.string.aboutcompany)
//            headerBinding.text = getString(R.string.suota_upgrade)
//            headerBinding.setOnClickListener { navigateToScanActivity() }
            headerBinding.headerRight.text = getString(R.string.feedback)
            headerBinding.headerRight.setOnClickListener { navigateToFeedbackActivity() }
            headerBinding.headerRight.visibility = View.INVISIBLE
            // 显示版本信息
            ver.text = getAppVersionName(requireContext())
            sdkVer.text = com.feasycom.util.FileUtil.SDK_VER;
        }

        // 设置按钮点击事件
        setupButtonListeners()

    }

    /**
     * 初始化事件监听器。
     */
    private fun initializeEvents() {
        with(footBinding){
            beaconNav.setOnClickListener {
                navigateToFragment(R.id.beaconFragment)
            }
            sensorNav.setOnClickListener {
                navigateToFragment(R.id.sensorFragment)
            }
            settingNav.setOnClickListener {
                navigateToFragment(R.id.settingFragment)
            }
            forumNav.setOnClickListener {
                navigateToFragment(R.id.forumFragment)
            }
            storeNav.setOnClickListener {
                navigateToFragment(R.id.storeFragment)
            }
        }

    }

    /**
     * 设置各个按钮的点击监听器。
     */
    private fun setupButtonListeners() {
        with(binding){
          /*  more.setOnClickListener { navigateToMoreActivity() }
            details.setOnClickListener { navigateToPdfActivity() }
            wx.setOnClickListener { navigateToQRCodeActivity() }*/
            userAgreement.setOnClickListener { navigateToAgreementActivity(1) }
            privacyAgreement.setOnClickListener { navigateToAgreementActivity(2) }
            toWeb.setOnClickListener {
                try {
                    var url: String = "https://www.feasycom.cn/"
                    // 确保URL有协议前缀
                    val formattedUrl = if (!url.startsWith("http")) {
                        "https://$url"
                    } else {
                        url
                    }

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(formattedUrl)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    activity?.let {
                        if (intent.resolveActivity(it.packageManager) != null) {
                            startActivity(intent)
                        } else {
                            ToastUtils.showLong("无法打开浏览器")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            phoneCall.setOnClickListener{
                try {
                    // 清理电话号码格式
                    val formattedNumber = formatPhoneNumber("17744976310")

                    // 创建拨号Intent
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$formattedNumber")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    // 验证是否有应用能处理
                    activity?.let {
                        if (intent.resolveActivity(it.packageManager) != null) {
                            startActivity(intent)
                        } else {
                            ToastUtils.showLong("无法打开拨号应用")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtils.showLong("拨号失败")
                }

            }

            toEmail.setOnClickListener {

                try {
                    // 使用 ACTION_SENDTO 而非 ACTION_SEND
                    // ACTION_SENDTO 专门用于mailto:协议，通常不会弹出选择器
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        // 构建mailto URI，只填写收件人
                        data = Uri.parse("mailto:support@feasycom.com")

                        // 如果需要填写主题和正文，可以这样：
                        // data = Uri.parse("mailto:support@feasycom.com" +
                        //     "?subject=Support Request" +
                        //     "&body=Please describe your issue here")

                        // 确保不会显示应用选择器
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                    // 验证是否有应用能处理
                    activity?.let {
                        if (emailIntent.resolveActivity(it.packageManager) != null) {
                            startActivity(emailIntent)
                        } else {
                            // 没有邮件应用，提示用户
                            ToastUtils.showLong("未找到邮箱应用")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastUtils.showLong("打开邮箱失败")
                }



            }

            toolbarFeedback.setOnClickListener {

                navigateToFeedbackActivity()

            }
        }
    }



    /**
     * 格式化电话号码
     */
    private fun formatPhoneNumber(phoneNumber: String): String {
        // 移除所有非数字字符，保留 + 号
        return phoneNumber.replace("[^+0-9]".toRegex(), "")
    }

    /**
     * 获取应用版本名称。
     * @param context 上下文
     * @return 应用版本名称
     */
    private fun getAppVersionName(context: Context): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        view?.visibility = if (menuVisible) View.VISIBLE else View.GONE
    }

    /**
     * 无下划线的 Span 类。
     */
    class NoUnderlineSpan : UnderlineSpan() {
        override fun updateDrawState(ds: TextPaint) {
            // 设置颜色并取消下划线
            ds.color = ds.linkColor
            ds.isUnderlineText = false
        }
    }

    /**
     * 导航到反馈活动。
     */
    private fun navigateToFeedbackActivity() {
        startActivity(Intent(requireContext(), FeedbackActivity::class.java))
    }

    /**
     * 导航到二维码活动。
     */
    private fun navigateToQRCodeActivity() {
        startActivity(Intent(requireContext(), QRCodeActivity::class.java))
    }

    /**
     * 导航到更多活动。
     */
    private fun navigateToMoreActivity() {
        startActivity(Intent(requireContext(), AboutMoreActivity::class.java))
    }

    /**
     * 导航到 PDF 活动。
     */
    private fun navigateToPdfActivity() {
        startActivity(Intent(requireContext(), PdfActivity::class.java))
    }

    /**
     * 导航到协议活动。
     * @param type 协议类型（1: 用户协议, 2: 隐私政策）
     */
    private fun navigateToAgreementActivity(type: Int) {
        val intent = Intent(requireContext(), AgreementActivity::class.java).apply {
            putExtra("type", type)
        }
        startActivity(intent)
    }

    /**
     * 导航到指定的片段。
     * @param fragmentId 片段ID
     */
    private fun navigateToFragment(fragmentId: Int) {
        findNavController().navigate(fragmentId)
    }

}
