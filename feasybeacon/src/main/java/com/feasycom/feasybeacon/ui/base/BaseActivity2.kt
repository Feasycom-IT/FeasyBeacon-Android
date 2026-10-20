package com.feasycom.feasybeacon.ui.base

import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ToastUtils

/**
 * BaseActivity 是所有 Activity 的基类，用于简化 ViewBinding 的初始化和公共操作。
 * @param T 表示 ViewBinding 类型的泛型参数。
 */

abstract class BaseActivity2<T : ViewBinding> : AppCompatActivity() {

    // ViewBinding 对象，延迟初始化
    private lateinit var _binding: T

    // 公共的获取 ViewBinding 对象的方法
    protected val binding: T
        get() = _binding

    // Activity 创建时的回调方法
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    //    BarUtils.transparentStatusBar(this@BaseActivity2)
    //    BarUtils.setStatusBarLightMode(this@BaseActivity2, true)
        // 沉浸式状态栏
        setImmersiveStatusBar(this, true)

        // 初始化 ViewBinding
        _binding = getViewBinding()
        // 设置布局内容为 ViewBinding 的根布局
        setContentView(binding.root)
        // 初始化界面
        initView()
        // 初始化事件
        initEvent()


    }


    private fun setImmersiveStatusBar(activity: Activity, isLightStatusBar: Boolean) {
        val window = activity.getWindow()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setStatusBarColor(Color.TRANSPARENT)
        val controller =
            WindowCompat.getInsetsController(window, window.getDecorView())
        controller.setAppearanceLightStatusBars(isLightStatusBar)

        val contentView = findViewById<View?>(Window.ID_ANDROID_CONTENT)
        ViewCompat.setOnApplyWindowInsetsListener(
            contentView,
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                // 只获取底部导航栏高度
                val bottom = insets!!.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                // 只设置底部padding，顶部0，不遮挡沉浸式
                v!!.setPadding(0, 0, 0, bottom)
                insets.consumeSystemWindowInsets()
            })
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val configuration = resources.configuration
        configuration.fontScale = 1.0f
        val newResources = createConfigurationContext(configuration).resources
        return newResources ?: super.getResources()
    }

    /**
     * 获取 ViewBinding 对象的抽象方法，子类必须实现。
     * @return 返回 ViewBinding 对象。
     */
    abstract fun getViewBinding(): T



    /**
     * 初始化界面的方法，子类可选择性重写。
     */
    open fun initView() {}

    /**
     * 初始化事件的方法，子类可选择性重写。
     */
    open fun initEvent() {}


    override fun onDestroy() {
        super.onDestroy()
        ToastUtils.cancel()
    }

}