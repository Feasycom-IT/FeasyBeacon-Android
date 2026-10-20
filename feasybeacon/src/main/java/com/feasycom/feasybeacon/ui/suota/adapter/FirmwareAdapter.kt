package com.feasycom.feasybeacon.ui.suota.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.feasycom.suotalib.suota.HeaderInfo58x
import com.feasycom.suotalib.suota.HeaderInfo68x
import com.feasycom.suotalib.suota.HeaderInfo69x
import com.feasycom.suotalib.suota.SuotaFile
import com.feasycom.feasybeacon.R

class FirmwareAdapter(
    private val itemClickListener: OnItemClickListener,
    private var displayDetails: Boolean,
    private var displayType58x: Boolean,
    private var displayType68x: Boolean,
    private var displayType69x: Boolean,
    private var displayUnknownType: Boolean
) : RecyclerView.Adapter<FirmwareAdapter.FirmwareViewHolder>() {

    private var suotaFiles: MutableList<SuotaFile>? = null // 存储SUOTA文件的列表
    private var removedFiles: MutableList<SuotaFile>? = null // 存储被移除的文件的列表
    private var noInfo: String? = null // 当信息不可用时显示的文本

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirmwareViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_firmware, parent, false)
        return FirmwareViewHolder(view)
    }

    override fun onBindViewHolder(holder: FirmwareViewHolder, position: Int) {
        val suotaFile = suotaFiles?.get(position) ?: return // 获取对应位置的文件
        holder.name.text = suotaFile.filename // 显示文件名

        holder.root.setOnClickListener {
            itemClickListener.onClick(suotaFile) // 设置点击事件
        }

        holder.detailsWrapper.visibility =
            if (displayDetails) View.VISIBLE else View.GONE // 根据条件显示或隐藏详情
        if (displayDetails) populateDetails(holder, suotaFile) // 如果需要显示详情，填充详情信息
    }

    private fun populateDetails(holder: FirmwareViewHolder, suotaFile: SuotaFile) {
        if (noInfo == null) {
            noInfo = holder.itemView.context.getString(R.string.no_info) // 设置无信息时的默认文本
        }
        val headerInfo = suotaFile.headerInfo
        holder.type.text = headerInfo?.type ?: noInfo // 显示设备类型
        holder.version.text =
            if (!TextUtils.isEmpty(headerInfo?.version)) headerInfo.version else noInfo // 显示版本号
        val size = headerInfo?.totalBytes?.toInt() ?: suotaFile.fileSize // 获取文件大小
        holder.payloadSize.text = if (size > 0) size.toString() else noInfo // 显示有效载荷大小
    }

    fun displayDetails(displayDetails: Boolean) {
        this.displayDetails = displayDetails
        notifyDataSetChanged() // 通知适配器数据更改
    }

    fun displayType58x(displayType58x: Boolean) {
        this.displayType58x = displayType58x
        val modified = if (displayType58x) insertRemovedItems(HeaderInfo58x.TYPE) else removeItems(
            HeaderInfo58x.TYPE
        )
        if (modified) notifyDataSetChanged() // 如果数据修改了，通知适配器
    }

    fun displayType68x(displayType68x: Boolean) {
        this.displayType68x = displayType68x
        val modified = if (displayType68x) insertRemovedItems(HeaderInfo68x.TYPE) else removeItems(
            HeaderInfo68x.TYPE
        )
        if (modified) notifyDataSetChanged()
    }

    fun displayType69x(displayType69x: Boolean) {
        this.displayType69x = displayType69x
        val modified = if (displayType69x) insertRemovedItems(HeaderInfo69x.TYPE) else removeItems(
            HeaderInfo69x.TYPE
        )
        if (modified) notifyDataSetChanged()
    }

    fun displayUnknownType(displayUnknownType: Boolean) {
        this.displayUnknownType = displayUnknownType
        val modified = if (displayUnknownType) insertRemovedItems() else removeItems()
        if (modified) notifyDataSetChanged()
    }

    fun add(suotaFile: SuotaFile) {
        if (suotaFiles == null) {
            suotaFiles = mutableListOf()
        }

        val itemPositionChanged = suotaFiles?.size ?: 0 // 获取当前文件列表的大小
        suotaFiles?.add(suotaFile) // 添加新的文件到列表中
        updateUsingFlags() // 更新显示标志
        notifyItemChanged(itemPositionChanged) // 通知适配器该位置数据更改
    }

    fun setSuotaFiles(suotaFiles: List<SuotaFile>) {
        this.suotaFiles = suotaFiles.toMutableList() // 设置新的文件列表
        removedFiles?.clear() // 设置新的文件列表
        updateUsingFlags()
        notifyDataSetChanged() // 通知适配器数据更改
    }

    private fun updateUsingFlags() {
        // 更新显示标志
        displayDetails(displayDetails)
        displayType58x(displayType58x)
        displayType68x(displayType68x)
        displayType69x(displayType69x)
        displayUnknownType(displayUnknownType)
    }

    private fun insertRemovedItems(type: String? = null): Boolean {
        if (removedFiles.isNullOrEmpty()) return false // 如果没有被移除的文件，返回false

        val temp = mutableListOf<SuotaFile>()
        for (suotaFile in removedFiles!!) {
            if (type == null) {
                if (suotaFile.headerInfo != null) continue // 跳过有header信息的文件
                suotaFiles?.add(suotaFile) // 添加文件到列表中
                temp.add(suotaFile)
            } else {
                if (suotaFile.headerInfo == null || suotaFile.headerInfo.type != type) continue
                suotaFiles?.add(suotaFile)
                temp.add(suotaFile)
            }
        }

        if (temp.isEmpty()) return false // 如果没有文件被添加，返回false

        removedFiles?.removeAll(temp) // 从移除文件列表中移除这些文件
        suotaFiles?.sortBy { it.filename.toLowerCase() } // 根据文件名排序

        return true
    }

    private fun removeItems(type: String? = null): Boolean {
        if (removedFiles == null) {
            removedFiles = mutableListOf()
        }

        val temp = mutableListOf<SuotaFile>()
        for (suotaFile in suotaFiles!!) {
            if (type == null) {
                if (suotaFile.headerInfo == null) temp.add(suotaFile)
            } else {
                if (suotaFile.headerInfo != null && suotaFile.headerInfo.type == type) temp.add(
                    suotaFile
                )
            }
        }

        if (temp.isEmpty()) return false // 如果没有文件被移除，返回false

        suotaFiles?.removeAll(temp) // 从列表中移除这些文件
        removedFiles?.addAll(temp) // 添加到移除文件列表中
        return true
    }

    override fun getItemCount(): Int {
        return suotaFiles?.size ?: 0 // 返回文件列表的大小
    }

    inner class FirmwareViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: ConstraintLayout = view.findViewById(R.id.root) // 根布局
        val detailsWrapper: RelativeLayout = view.findViewById(R.id.details_wrapper) // 详情包装布局
        val name: TextView = view.findViewById(R.id.name) // 文件名
        val type: TextView = view.findViewById(R.id.type) // 设备类型
        val version: TextView = view.findViewById(R.id.version) // 版本号
        val payloadSize: TextView = view.findViewById(R.id.payload_size) // 有效载荷大小
    }

    interface OnItemClickListener {
        fun onClick(suotaFile: SuotaFile) // 点击事件接口
    }
}
