package com.feasycom.feasybeacon.ui.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.feasycom.feasybeacon.logic.BeaconRepository
import com.feasycom.feasybeacon.logic.model.FeedbackParams

/**
 * 反馈 ViewMode，用于处理反馈相关逻辑
 */

class FeedbackViewModel : ViewModel() {

    // LiveData 用于存储反馈请求参数，并观察反馈响应
    private val feedbackLiveData = MutableLiveData<FeedbackParams.Params>()

    // Transformations.switchMap 用于将 feedbackLiveData 转换成对应的反馈响应 LiveData
    val mFeedbackResponseBodyLiveData = feedbackLiveData.switchMap {
        BeaconRepository.feedback(it.content, it.feedbackType)
    }

    /**
     * 发起反馈请求
     * @param content 反馈内容
     * @param feedbackType 反馈类型
     */
    fun feedback(content: String, feedbackType: Int) {
        // 设置反馈请求参数，并更新 LiveData
        feedbackLiveData.value = FeedbackParams.Params(content, feedbackType)
    }

}