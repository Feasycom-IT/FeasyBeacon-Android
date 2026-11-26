package com.feasycom.feasybeacon.ui.store

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.FragmentStoreBinding
import com.feasycom.feasybeacon.ui.base.BaseFragment

/**
 * 产品页面（原商城页面）
 */
class StoreFragment : BaseFragment<FragmentStoreBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentStoreBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.foot.navigationBar.check(R.id.store_nav)
        binding.header.headerTitle.text = getString(R.string.product)

        // 标题栏返回/刷新（与 FeasyBlue 产品页一致）
        binding.header.headerLeft.text = getString(R.string.back)
        binding.header.headerLeft.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            }
        }
        binding.header.headerRight.text = getString(R.string.title_refresh)
        binding.header.headerRight.setOnClickListener {
            binding.webView.reload()
        }

        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            setSupportZoom(true)
            builtInZoomControls = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            domStorageEnabled = true
            databaseEnabled = true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initEvent() {
        binding.foot.beaconNav.setOnClickListener {
            findNavController().navigate(R.id.beaconFragment)
        }
        binding.foot.sensorNav.setOnClickListener {
            findNavController().navigate(R.id.sensorFragment)
        }
        binding.foot.settingNav.setOnClickListener {
            findNavController().navigate(R.id.settingFragment)
        }
        binding.foot.aboutNav.setOnClickListener {
            findNavController().navigate(R.id.about_nav)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                LogUtils.d(TAG, "initView onReceivedSslError ")
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                val scheme = url.scheme?.lowercase()
                if (scheme == "http" || scheme == "https") return false
                // tel: / mailto: 等交给系统处理，避免 WebView 报错
                return runCatching {
                    startActivity(Intent(Intent.ACTION_VIEW, url))
                    true
                }.getOrDefault(true)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                /*if(userVisible){
                    try{
                        if(url != "https://store.feasycom.com/"){
                            (requireActivity() as MainActivity).storeBackVisibility(View.VISIBLE)
                        }else{
                            (requireActivity() as MainActivity).storeBackVisibility(View.GONE)
                        }
                    }catch (e: IllegalStateException){
                        e.printStackTrace()
                    }
                }*/
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                // WebView 回调可能晚于视图销毁，此时 binding 已为空
                if (this@StoreFragment.view == null) return
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    if (binding.progressBar.visibility == View.GONE) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
        binding.webView.loadUrl("https://www.feasycom.com.cn/products/")
    }

    override fun onResume() {
        super.onResume()
        binding.foot.navigationBar.check(R.id.store_nav)
    }

    override fun onDestroyView() {
        // 先解绑回调再销毁 WebView，避免销毁后仍有回调访问 binding
        binding.webView.apply {
            stopLoading()
            webChromeClient = null
            loadUrl("about:blank")
            (parent as? ViewGroup)?.removeView(this)
            destroy()
        }
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "StoreFragment"
    }
}
