package com.feasycom.feasybeacon.ui.ota

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.feasybeacon.R
import com.feasycom.feasybeacon.databinding.ActivityOtaBinding
import com.feasycom.feasybeacon.logic.BluetoothRepository
import com.feasycom.feasybeacon.logic.dao.DeviceDatabase
import com.feasycom.feasybeacon.logic.interfaces.FscBleCallback
import com.feasycom.feasybeacon.logic.model.DeviceInfo
import com.feasycom.feasybeacon.logic.model.Devices
import com.feasycom.feasybeacon.logic.network.BeaconNetwork
import com.feasycom.feasybeacon.logic.utils.Utils
import com.feasycom.feasybeacon.logic.utils.getStr
import com.feasycom.feasybeacon.logic.utils.putStr
import com.feasycom.feasybeacon.ui.base.BaseActivity
import com.feasycom.feasybeacon.ui.dialog.DfuNameDialogFragment
import com.feasycom.feasybeacon.ui.dialog.ProgressBarDialogFragment
import com.feasycom.feasybeacon.ui.utils.assetsToJsonString
import com.feasycom.feasybeacon.ui.utils.isExternalStorageLegacy
import com.feasycom.util.FeasyBeaconUtil
import com.feasycom.util.FileUtil
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.io.*
import java.lang.Runnable
import java.net.UnknownHostException

class OtaActivity : BaseActivity<ActivityOtaBinding>(), FscBleCallback {

    private var mFileNameLiveData = MutableLiveData<String>()
    private var mFileUriLiveData = MutableLiveData<Uri>()
    private val mFileByteArrayLiveData = MutableLiveData<ByteArray>()
    private var mIsDisconnect = false
    private var mDeviceInfo: DeviceInfo? = null
    private val mProgress = ProgressBarDialogFragment()
    private val mDeviceDao by lazy { DeviceDatabase.getDatabase(this).deviceDao() }
    private val mDevice by lazy { intent.getSerializableExtra("device") as BluetoothDeviceWrapper }
    private val mDevicePin by lazy { intent.getStringExtra("pin") ?: "000000" }
    private var dialog: androidx.appcompat.app.AlertDialog? = null

    private val mHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            START_DOWNLOAD -> {
                mProgress.show(supportFragmentManager, "ota")
            }

            DOWNLOAD_FINISH -> {
                mProgress.dismiss()
            }

            SHOW_DIALOG -> {
                mProgress.dismiss()
                com.feasycom.feasybeacon.ui.utils.showDialogs(
                    this,
                    msg.data.getString("message", "")
                )
            }

            UPDATE_FAILED -> {
            }
        }
        false
    }

    val mDisconnectRunnable = { BluetoothRepository.disconnect("======OtaActivity1========") }

    private val mRunnable = Runnable {
        AlertDialog.Builder(this).setTitle(R.string.error)
            .setMessage(getString(R.string.ota_failed)).setIcon(R.mipmap.ic_launcher)
            .setCancelable(false)
            .setNegativeButton(R.string.ok) { dialog, _ ->
                BluetoothRepository.disconnect("======OtaActivity2=========")
                dialog.dismiss()
            }.create().show()
    }

    override fun getViewBinding() = ActivityOtaBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothRepository.registerViewCallback(this)
    }

    override fun initView() {
        binding.header.headerTitle.text = resources.getString(R.string.update)
        binding.header.headerLeft.text = resources.getString(R.string.back)

        mFileUriLiveData.observe(this) { uri ->
            putStr("fileUri", uri.toString())
            getUriInfo(uri)
        }

        mFileNameLiveData.observe(this) { fileName ->
            binding.otaFileName.text = fileName
        }

        mFileByteArrayLiveData.observe(this) { byteArray ->
            if (byteArray == null || byteArray.isEmpty()) {
                LogUtils.e(TAG, "initView byteArray is null or empty")
                binding.startOTA.isEnabled = false
                return@observe
            }

            try {
                BluetoothRepository.checkDfuFile(byteArray).let { dfuFileInfo ->
                    binding.exceptVersion.text =
                        "${Integer.valueOf(dfuFileInfo.versonStart)}-${Integer.valueOf(dfuFileInfo.verson_soft_end)}"
                    binding.exceptBLVersion.text =
                        Integer.valueOf(dfuFileInfo.bootloader).toString()
                    Log.e("vvvvv", "========mFileByteArrayLiveData===============${dfuFileInfo.type_model}      ${dfuFileInfo.bootloader}");
                    mDeviceInfo = run {
                        // 直接从 JSON 文件中加载设备信息
                        val json = assetsToJsonString("Device.json")
                        val gson = Gson()
                        val devices = gson.fromJson(json, Devices::class.java)
                        devices?.data?.list?.find { it.number == dfuFileInfo.type_model }
                    }

                    mHandler.postDelayed({

                        if (null != mDeviceInfo) {
                            binding.exceptModule.text = mDeviceInfo?.name
                            binding.startOTA.isEnabled =
                                binding.currentModule.text.toString() == binding.exceptModule.text
                        } else {
                            binding.exceptModule.text = "Unknown"
                            binding.startOTA.isEnabled = false

                        }

                    }, 100)


                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("vvvvv", "========mFileByteArrayLiveData===============${e.toString()}");
            }

        }

        mFileUriLiveData.observe(this) {
            if (it != null) {
                putStr("FileUri", it.toString())
            }
        }

        mDevice.let {

            binding.currentVersion.text = it.feasyBeacon.version
            binding.currentModule.text = FeasyBeaconUtil.moduleAdapter(it.feasyBeacon.module)
            mIsDisconnect = true
            BluetoothRepository.disconnect("========OtaActivity3=========")
        }
    }

    override fun onResume() {
        super.onResume()
        getFileUri()
    }

    private fun getFileUri() {
        getStr("fileUri", "").let {
            if (it.isNotEmpty()) {
                mFileUriLiveData.value = Uri.parse(it)
            }
        }
    }

    override fun initEvent() {
        binding.selectFileImage.setOnClickListener { performFileSearch() }
        val downloadClickListener = View.OnClickListener {
            if (isExternalStorageLegacy()) {
                createDfuDialog()
            } else {
                checkPermission()
            }
        }
        binding.downloadFileImg.setOnClickListener(downloadClickListener)

        binding.startOTA.setOnClickListener {
            if (mFileByteArrayLiveData.value != null) {
                binding.selectFileConstraint.visibility = View.VISIBLE
                binding.OTAInfoLL.visibility = View.VISIBLE
                binding.resetLL.visibility = View.GONE
                binding.fileLinear.visibility = View.VISIBLE
                binding.startOTA.visibility = View.GONE
                binding.otaProgressLL.visibility = View.VISIBLE

                mHandler.postDelayed(mRunnable, 25 * 1000)
                binding.otaProgress.progress = 0
                binding.selectFileImage.isEnabled = false
                binding.selectFileTv.isEnabled = false
                binding.downloadFileImg.isEnabled = false
                binding.downloadFileTv.isEnabled = false

                BluetoothRepository.connect(mDevice, mDevicePin)
            } else {
                binding.selectFileConstraint.visibility = View.VISIBLE
                binding.OTAInfoLL.visibility = View.VISIBLE
                binding.resetLL.visibility = View.VISIBLE
                binding.fileLinear.visibility = View.VISIBLE
                binding.startOTA.visibility = View.VISIBLE
                binding.otaProgressLL.visibility = View.GONE
            }
        }
        binding.header.headerLeft.setOnClickListener {
            BluetoothRepository.disconnect("=========OtaActivity4==========")
            finish()
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (dialog != null) {
                dialog!!.dismiss()
                dialog = null
            }
            dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(resources.getString(R.string.tips))
                .setMessage(resources.getString(R.string.file_access_permissions))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                    ActivityCompat.requestPermissions(
                        this@OtaActivity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                    )
                }.create()
            dialog!!.show()
        } else {
            createDfuDialog()
        }
    }

    override fun onOtaProgressUpdate(percentage: Int) {
        super.onOtaProgressUpdate(percentage)
        if (percentage != 0) {
            mHandler.removeCallbacks(mRunnable)
        }
        runOnUiThread(object : Runnable {
            override fun run() {
                try {
                    binding.otaProgress.progress = percentage
                    if (percentage == 100) {
                        binding.otaProgress.setmTxtHint1(getString(R.string.ota_success))
                        binding.otaProgress.setmTxtHint2(getString(R.string.ota_success_tips))
                        mHandler.postDelayed(mDisconnectRunnable, 3000)
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    return
                }
            }
        })
    }

    override fun onConnectedSuccess(device: BluetoothDevice) {
        mHandler.removeCallbacks(mDisconnectRunnable)
        mHandler.removeCallbacks(mRunnable)

        BluetoothRepository.startOtaUpdate(mFileByteArrayLiveData.value!!, binding.reset.isChecked)
    }

    override fun onDisconnect() {
        super.onDisconnect()
        mHandler.removeCallbacks(mDisconnectRunnable)
        mHandler.removeCallbacks(mRunnable)
        if (binding.otaProgressLL.visibility == View.VISIBLE) {
            runOnUiThread {
                ToastUtils.showShort(getString(R.string.disconnect))
                finish()
            }
        } else {
            if (mIsDisconnect) {
                mIsDisconnect = false
            } else {
                runOnUiThread {
                    ToastUtils.showShort(getString(R.string.disconnect))
                    finish()
                }
            }
        }
    }

    override fun onPacketReceived(strValue: String, hexString: String, rawValue: ByteArray) {
        super.onPacketReceived(strValue, hexString, rawValue)
        if (strValue.contains("OK")) {
            if (strValue.length >= 15) {
                runOnUiThread {
                    val moduleBootloaderInt =
                        Integer.valueOf(FileUtil.stringToInt(strValue.substring(7, 11)))
                    binding.currentBl.text = moduleBootloaderInt.toString()
                }

            } else {

            }
        } else if (strValue.contains("C")) {
        } else if (strValue.contains("S")) {
        } else if ((rawValue[0].toInt() != 0x06) && (rawValue[0].toInt() != 0x15) && (rawValue[0].toInt() != 0x7F)) {
        } else if (hexString == "18") {
            BluetoothRepository.disconnect("========OtaActivity5=======")
            ToastUtils.showShort(getString(R.string.ota_failed))
        }
    }

    private fun performFileSearch() {
        val intent = Intent()
        intent.setType("*/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        Intent.createChooser(intent, "Choose directory")
        startActivityForResult(intent, READ_FILE_REQUEST_CODE)
    }

    private fun getUriInfo(uri: Uri) {
        when (uri.scheme) {
            "content" -> {
                try {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            contentResolver.openInputStream(uri)?.use {
                                mFileByteArrayLiveData.postValue(it.readBytes())
                            }
                            val fileName =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
                            mFileNameLiveData.postValue(fileName)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            "file" -> {
                try {
                    uri.toFile().let { file ->
                        mFileNameLiveData.postValue(file.name)
                        mFileByteArrayLiveData.postValue(file.readBytes())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != READ_FILE_REQUEST_CODE || resultCode != RESULT_OK) {
            return
        }
        data?.data?.let { uri ->
            mFileUriLiveData.postValue(uri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createDfuDialog()
                }
            }
        }
    }

    private fun createDfuDialog() {
        val dfuNameDialogFragment = DfuNameDialogFragment().apply {
            onClickComplete = {
                dialog?.dismiss()
                mHandler.sendEmptyMessage(START_DOWNLOAD)
                if (it.isNotBlank()) {
                    GlobalScope.launch { downloadDfu(it) }
                } else {
                    mHandler.sendEmptyMessage(DOWNLOAD_FINISH)
                }
            }
        }
        if (!this.isFinishing) {
            try {
                dfuNameDialogFragment.show(supportFragmentManager, null)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun downloadDfu(parameter: String) {
        coroutineScope {
            deleteExistsFile(parameter)
            try {
                BeaconNetwork.downloadDFU(parameter).apply {
                    if (Utils.isExternalStorageLegacy()) {
                        val contentUri = MediaStore.Downloads.getContentUri("external")
                        val contentValues = ContentValues()
                        contentValues.put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS + "/dfu"
                        )
                        contentValues.put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            "${parameter}.dfu"
                        )
                        launch(Dispatchers.IO) {
                            // 创建文件
                            Log.e(TAG, "========固件下载===========${contentUri}    ")
                            contentResolver.insert(contentUri, contentValues)?.let {
                                contentResolver.openOutputStream(it)?.use { out ->
                                    val byteArray = ByteArray(1024)
                                    var len: Int
                                    while (byteStream().read(byteArray).also { len = it } != -1) {
                                        out.write(byteArray, 0, len)
                                    }
                                }
                                mFileUriLiveData.postValue(it)
                            }
                            runOnUiThread {
                                ToastUtils.showShort(getString(R.string.down_file_finish))
                            }
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH)
                        }
                        return@coroutineScope
                    } else {
                        val directoryPath =
                            "${File.separator}sdcard${File.separator}Download${File.separator}dfu${File.separator}"
                        val directory = File(directoryPath)
                        if (!directory.exists()) {
                            directory.mkdirs()
                        }
                        val replaceParameter = parameter.replace("/", "_")
                        val path = "${directoryPath}${replaceParameter}.dfu"
                        val file = File(path)
                        launch(Dispatchers.IO) {
                            BufferedInputStream(byteStream()).use { input ->
                                BufferedOutputStream(FileOutputStream(file)).use { out ->
                                    var data: Int
                                    while (input.read().also { data = it } != -1) {
                                        out.write(data)
                                    }
                                }
                            }
                            runOnUiThread {
                                ToastUtils.showShort(getString(R.string.down_file_finish))
                            }
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH)
                            mFileUriLiveData.postValue(file.toUri())
                        }
                    }
                }
            } catch (e: HttpException) {
                Log.e(TAG, "========固件下载====2=======${e.message}    ")
                val message = Message()
                message.what = SHOW_DIALOG
                message.data.putString(
                    "message", when (e.code()) {
                        404 -> {
                            getString(R.string.down_file_error)
                        }

                        else -> {
                            getString(R.string.network_error)
                        }
                    }
                )
                mHandler.sendMessage(message)
            } catch (e: IOException) {
                Log.e(TAG, "========固件下载====3=======${e.message}    ")
                val message = Message()
                message.what = SHOW_DIALOG
                message.data.putString("message", getString(R.string.io_error))
                mHandler.sendMessage(message)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "========固件下载====4=======${e.message}    ")
                val message = Message()
                message.what = SHOW_DIALOG
                message.data.putString("message", getString(R.string.network_error))
                mHandler.sendMessage(message)
            } catch (e: Exception) {
                Log.e(TAG, "========固件下载====5=======${e.message}    ")
                e.printStackTrace()
            }
        }
    }

    private fun deleteExistsFile(parameter: String) {
        val replaceParameter = parameter.replace("/", "_")
        if (isExternalStorageLegacy()) {
            val externalContentUri = MediaStore.Downloads.getContentUri("external")
            val selection = MediaStore.Downloads.DISPLAY_NAME + "=?"
            val arg = arrayOf("$replaceParameter.dfu")
            val cursor = contentResolver.query(
                externalContentUri,
                null,
                selection,
                arg,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndexOrThrow = it.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                    val queryId = it.getLong(columnIndexOrThrow)
                    val uri = ContentUris.withAppendedId(externalContentUri, queryId)
                    contentResolver.delete(uri, null, null)
                }
            }
        } else {
            // 不是分区存储
            val path =
                "${File.separator}sdcard${File.separator}Download${File.separator}dfu${File.separator}${replaceParameter}"
            val file = File(path)
            val exists = file.exists()
            if (exists) {
                file.delete()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothRepository.disconnect("======OtaActivity6========")
        BluetoothRepository.unRegisterViewCallback(this)
        mHandler.removeCallbacks(mRunnable)
    }

    companion object {
        private const val TAG = "OtaActivity"
        private const val START_DOWNLOAD = 0x0001
        private const val DOWNLOAD_FINISH = 0x0002
        private const val SHOW_DIALOG = 0x0003
        private const val UPDATE_FAILED = 0x0004
        private const val READ_FILE_REQUEST_CODE = 42

        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

}