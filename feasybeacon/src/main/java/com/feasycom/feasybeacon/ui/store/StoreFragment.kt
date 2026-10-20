package com.feasycom.feasybeacon.ui.store

import android.annotation.SuppressLint
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
 * 应用商店页面
 */
class StoreFragment : BaseFragment<FragmentStoreBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentStoreBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.foot.navigationBar.check(R.id.store_nav)
        binding.header.headerTitle.text = getString(R.string.store)

        with(binding.webView.settings) {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true // 设置允许访问文件数据
            setSupportZoom(true)
            builtInZoomControls = true
            javaScriptCanOpenWindowsAutomatically = true
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
        binding.webView.loadUrl("https://www.feasycom.com.cn/products/")
    }

    override fun onResume() {
        super.onResume()
        binding.foot.navigationBar.check(R.id.store_nav)
    }

    companion object {
        private const val TAG = "StoreFragment"
    }
}
