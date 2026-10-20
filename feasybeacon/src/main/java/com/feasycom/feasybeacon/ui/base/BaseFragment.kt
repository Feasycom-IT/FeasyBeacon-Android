package com.feasycom.feasybeacon.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * BaseFragment 是所有 Fragment 的基类，用于简化 ViewBinding 的初始化和公共操作。
 * @param T 表示 ViewBinding 类型的泛型参数。
 */

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    // ViewBinding 对象，可为空，延迟初始化
    private var _binding: T? = null

    // 公共的获取 ViewBinding 对象的属性
    protected val binding get() = _binding!!

    // 创建 View 时的回调方法
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 初始化 ViewBinding
        _binding = getViewBinding(inflater, container)
        // 返回 ViewBinding 的根布局
        return binding.root
    }

    // View 创建完成时的回调方法
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化界面
        initView()
        // 初始化事件
        initEvent()
    }

    // 销毁 View 时的回调方法
    override fun onDestroyView() {
        super.onDestroyView()
        // 释放 ViewBinding 对象的引用，防止内存泄漏
        _binding = null
    }

    /**
     * 获取 ViewBinding 对象的抽象方法，子类必须实现。
     * @param inflater LayoutInflater 对象，用于从布局文件中加载 ViewBinding。
     * @param container ViewGroup 对象，父容器。
     * @return 返回 ViewBinding 对象。
     */
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    /**
     * 初始化界面的方法，子类必须实现。
     */
    abstract fun initView()

    /**
     * 初始化事件的方法，子类必须实现。
     */
    abstract fun initEvent()

}