package com.feasycom.feasybeacon.ui.forum

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.LogUtils
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.FragmentForumBinding
import com.feasycom.feasybeacon.ui.base.BaseFragment

class ForumFragment : BaseFragment<FragmentForumBinding>(){

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentForumBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.foot.navigationBar.check(R.id.forum_nav)
        binding.header.headerTitle.text = getString(R.string.forum)

        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true // 设置允许访问文件数据
            setSupportZoom(true)
            builtInZoomControls = true
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = true
            databaseEnabled = true

            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            loadsImagesAutomatically = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
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
            findNavController().navigate(R.id.forum_nav)
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
                return super.shouldOverrideUrlLoading(view, request)
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

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().flush()
                }
            }
        }
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
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

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)
        binding.webView.loadUrl("https://forum.feasycom.com/")
    }

    override fun onResume() {
        super.onResume()
        binding.foot.navigationBar.check(R.id.forum_nav)
    }

    override fun onDestroyView() {
        // 在调用 super.onDestroyView 之前清理 WebView，确保不会在 binding 变为 null 后还有回调
        binding.webView.apply {
            stopLoading()
            webChromeClient = null
        }
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "ForumFragment"
    }
}