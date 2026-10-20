package com.feasycom.feasybeacon.ui.suota.fragments

import android.widget.TextView
import android.os.Bundle
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.ui.suota.activities.SuotaActivity
import java.lang.RuntimeException

class UpdateFirmwareFragment : BaseSuotaFragment() {
    // UI组件
    private var uploadInfoTv: TextView? = null
    private var progressBar: ProgressBar? = null
    private var progressTv: TextView? = null
    private var close: Button? = null
    private var mListener: OnUpdateFirmwareFragmentInteractionListener? = null
    private var deviceName: String? = null
    private var firmwareName: String? = null

    // 在Fragment创建时获取传递的设备名和固件名
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            deviceName = requireArguments().getString("BluetoothDevice")
            firmwareName = requireArguments().getString("SuotaFile")
        }
    }

    // 创建Fragment的视图
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 通知Activity当前Fragment的标识符
        baseListener!!.onSuotaFragmentIdentifierUpdate(SuotaActivity.UPDATE_FIRMWARE_ID)
        return inflater.inflate(R.layout.fragment_update_firmware, container, false)
    }

    // 在视图创建后初始化UI组件
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        uploadInfoTv = view.findViewById(R.id.uploading_info)
        progressBar = view.findViewById(R.id.progress_bar)
        progressTv = view.findViewById(R.id.progress_bar_text)
        close = view.findViewById(R.id.close)

        // 设置关闭按钮不可见，点击关闭按钮会结束当前Activity
        close?.let {
            it.visibility = View.GONE
            it.setOnClickListener { requireActivity().finish() }
        }

        // 设置上传信息的文本
        uploadInfoTv?.let {
            it.text = getString(R.string.uploading_info, if (firmwareName != null) firmwareName else "", if (deviceName != null) deviceName else "")
        }

        // 初始化进度条
        progressBar?.let {
            it.visibility = View.VISIBLE
            it.progress = 0
        }

        // 通知监听器启动SUOTA协议
        mListener!!.startSuotaProtocol()
    }

    // 更新进度条的值
    fun updateProgressValue(value: Int) {
        progressBar!!.progress = value
        progressTv!!.text = value.toString()
    }

    // 使关闭按钮可见
    fun makeCloseButtonVisible() {
        close!!.visibility = View.VISIBLE
    }

    // 通知SUOTA完成
    fun notifySuotaFinished(elapsedTime: Double, uploadTime: Double) {
        mListener!!.notifySuotaFinished()
    }

    // 保存Fragment状态时返回null，不保存任何状态
    override fun saveState(): Bundle? {
        return null
    }

    // 恢复Fragment状态时，不执行任何操作
    override fun restoreState(bundle: Bundle?) {}

    // Fragment附加到Activity时调用
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 判断上下文是否实现了OnUpdateFirmwareFragmentInteractionListener接口
        mListener = if (context is OnUpdateFirmwareFragmentInteractionListener) {
            context // 如果是，则将content赋值给mListener
        } else {
            // 如果不是，则抛出异常
            throw RuntimeException("$context must implement OnUpdateFirmwareFragmentInteractionListener")
        }
    }

    // Fragment从Activity分离时调用，清空mListener
    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    // 用于Fragment与Activity交互的接口
    interface OnUpdateFirmwareFragmentInteractionListener {
        fun startSuotaProtocol() // 启动SUOTA协议
        fun notifySuotaFinished() // 通知OTA完成
    }

    // 伴生对象，用于创建Fragment实例
    companion object {
        // 使用设备名和固件名创建UpdateFirmwareFragment实例
        fun newInstance(deviceName: String?, firmwareName: String?): UpdateFirmwareFragment {
            val parameterSettingFragment = UpdateFirmwareFragment()
            val args = Bundle()
            args.putString("BluetoothDevice", deviceName)
            args.putString("SuotaFile", firmwareName)
            parameterSettingFragment.arguments = args
            return parameterSettingFragment
        }

        // 使用Bundle创建UpdateFirmwareFragment实例
        fun newInstance(bundle: Bundle?): UpdateFirmwareFragment {
            val updateFirmwareFragment = UpdateFirmwareFragment()
            updateFirmwareFragment.arguments = bundle
            return updateFirmwareFragment
        }
    }
}