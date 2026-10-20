package com.feasycom.feasybeacon.ui.suota.fragments

import android.os.Bundle
import android.content.Intent
import android.app.Activity
import android.provider.DocumentsContract
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.feasycom.suotalib.global.SuotaLibConfig
import com.feasycom.suotalib.suota.SuotaFile
import com.feasycom.suotalib.suota.SuotaManager
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.ui.suota.activities.SuotaActivity
import com.feasycom.feasybeacon.ui.suota.adapter.FirmwareAdapter
import java.lang.RuntimeException

class AvailableFirmwareFragment : BaseSuotaFragment(), FirmwareAdapter.OnItemClickListener {

    private var firmwareRv: RecyclerView? = null // 用于显示固件列表的RecyclerView
    private var mListener: OnAvailableFirmwareFragmentInteractionListener? =
        null // 监听器，用于与宿主Activity通信
    private var firmwareAdapter: FirmwareAdapter? = null // 适配器，用于展示固件数据
    private var files: List<SuotaFile>? = null // 存储固件文件列表

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 恢复保存的状态
        restoreState(arguments)
        // 初始化固件适配器
        firmwareAdapter = FirmwareAdapter(this,
            displayDetails = true,
            displayType58x = true,
            displayType68x = true,
            displayType69x = true,
            displayUnknownType = true
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 更新 Fragment 标识，通知相关组件当前 Fragment 的标识
        baseListener!!.onSuotaFragmentIdentifierUpdate(SuotaActivity.AVAILABLE_FIRMWARE_ID)

        // 加载并返回 Fragment 的布局视图
        return inflater.inflate(R.layout.fragment_available_firmware, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 获取 RecyclerView 实例
        firmwareRv = view.findViewById(R.id.rv_firmware)

        // 如果 RecyclerView 实例不为空
        firmwareRv?.let {
            // 创建并设置布局管理器，垂直方向的线性布局
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            it.layoutManager = layoutManager

            // 添加分隔线装饰
            it.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

            // 设置适配器，用于提供 RecyclerView 的数据
            it.adapter = firmwareAdapter
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 检查是否有存储权限，必要时请求权限
        SuotaManager.checkStorage(context)

        // 初始化固件列表数据
        initializeFirmwareList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 处理文件选择结果
        if (requestCode == SELECT_FILE_REQUEST) {
            LogUtils.e(TAG, "onActivityResult SELECT_FILE_REQUEST")
            // 检查结果是否为成功
            if (resultCode == Activity.RESULT_OK) {
                // 获取选中的文件 URI，并创建 SuotaFile 对象
                mListener!!.onFirmwareSelected(SuotaFile(context, data!!.data)) // 传递选中的文件的 URI
            }
            return
        }

        // 处理路径选择结果
        if (requestCode == SELECT_PATH_REQUEST) {
            LogUtils.e(TAG, "onActivityResult SELECT_PATH_REQUEST")
            // 检查结果是否为成功
            if (resultCode == Activity.RESULT_OK) {
                // 设置文件路径 URI，并更新固件列表
                SuotaManager.setFilePathUri(context, data!!.data)
                initializeFirmwareList() // 更新固件列表
            }
            return
        }

        // 调用父类的 onActivityResult 方法处理其他请求码
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initializeFirmwareList() {

        // 根据是否启用了 Scoped Storage 来获取固件文件列表
        //    !SuotaLibConfig.SCOPED_STORAGE
        files = if (!SuotaLibConfig.SCOPED_STORAGE) {
            // 如果没有启用 Scoped Storage，直接获取固件文件列表
            SuotaFile.listFilesWithHeaderInfo() // 获取固件文件列表
        } else {
            // 如果启用了 Scoped Storage，调用带有上下文的获取方法
            SuotaFile.listFilesWithHeaderInfo(context).apply {
                // 检查获取到的固件文件列表是否为空
                if (isEmpty() && SuotaManager.isFilePathValid()) {
                    // 如果列表为空，并且文件路径有效，获取并处理文件路径
                    val path =
                        DocumentsContract.getTreeDocumentId(SuotaManager.getFilePath().uri)
                            .replaceFirst("^.*:".toRegex(), "") // 去掉 URI 的前缀部分
                    // 显示提示对话框，告知用户没有找到固件
                    showMessageDialog(
                        getString(R.string.no_firmware_found_title),
                        getString(R.string.no_firmware_found_msg, path)
                    )
                } else if (isEmpty()) {
                    // 如果列表仍然为空，显示选择路径对话框
                    showSelectPathDialog() // 显示选择路径对话框
                }
            }
        }

        // 对获取到的文件列表进行处理
        files?.let {
            // 如果文件列表为空，显示提示对话框
            if (it.isEmpty()) {
                showMessageDialog(
                    getString(R.string.no_firmware_found_title),
                    getString(R.string.no_firmware_found_msg, "SuOta")
                )
            } else {
                // 如果文件列表不为空，更新固件适配器的数据
                firmwareAdapter?.setSuotaFiles(it) // 更新适配器数据
            }
        }
    }

    override fun saveState(): Bundle? {
        return null // 不需要保存状态
    }

    override fun restoreState(bundle: Bundle?) {}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnAvailableFirmwareFragmentInteractionListener) {
            context // 确保宿主Activity实现了监听器接口
        } else {
            throw RuntimeException("$context must implement OnAvailableFirmwareFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null // 解除监听器引用
    }

    override fun onClick(suotaFile: SuotaFile) {
        mListener!!.onFirmwareSelected(suotaFile) // 处理固件文件点击事件
    }

    interface OnAvailableFirmwareFragmentInteractionListener {
        fun onFirmwareSelected(suotaFile: SuotaFile?) // 固件选择回调接口
    }

    private fun selectFile() {
        // 创建一个用于选择文档的意图
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

        // 添加一个类别，表示我们希望打开一个可打开的文档
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // 设置要选择的文件的 MIME 类型为所有类型，允许选择任何类型的文件
        intent.type = "*/*"

        // 启动文件选择活动，并指定请求码 SELECT_FILE_REQUEST
        // 选择的文件将在 onActivityResult 方法中处理
        startActivityForResult(intent, SELECT_FILE_REQUEST)
    }

    @TargetApi(21)
    private fun selectPath() {
        // 启动一个活动以选择文档树（即目录），用于获取用户选择的目录路径
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), // 创建一个选择文档树的意图
            SELECT_PATH_REQUEST // 请求码，用于在 onActivityResult 方法中区分这个请求
        )
    }

    class SelectPathDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_path_title)
                .setMessage(R.string.select_path_message)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int -> (targetFragment as AvailableFirmwareFragment?)!!.onSelectPathDialogOK() }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
        }
    }

    private fun showSelectPathDialog() {
        val dialog: DialogFragment = SelectPathDialog()
        dialog.setTargetFragment(this, 0)
        dialog.show(requireFragmentManager(), "SelectPathDialog") // 显示选择路径对话框
    }

    @TargetApi(21)
    private fun onSelectPathDialogOK() {
        selectPath() // 确认选择路径
    }

    class MessageDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val args = arguments
            val title = args!!.getString("title")
            val message = args.getString("message")
            val builder = AlertDialog.Builder(
                requireContext()
            )
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
            return builder.create()
        }
    }

    private fun showMessageDialog(title: String, message: String) {
        val args = Bundle()
        args.putString("title", title)
        args.putString("message", message)
        val dialog: DialogFragment = MessageDialog()
        dialog.arguments = args
        dialog.show(requireFragmentManager(), "MessageDialog") // 显示消息对话框
    }

    companion object {
        private const val TAG: String = "AvailableFirmware"
        private const val SELECT_PATH_REQUEST = 1
        private const val SELECT_FILE_REQUEST = 2

        // 创建新实例
        fun newInstance(): AvailableFirmwareFragment {
            return AvailableFirmwareFragment()
        }

        fun newInstance(bundle: Bundle?): AvailableFirmwareFragment {
            val availableFirmwareFragment = AvailableFirmwareFragment()
            availableFirmwareFragment.arguments = bundle
            return availableFirmwareFragment
        }
    }
}