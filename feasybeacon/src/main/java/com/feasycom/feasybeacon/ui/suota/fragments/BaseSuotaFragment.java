package com.feasycom.feasybeacon.ui.suota.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

public abstract class BaseSuotaFragment extends Fragment {

    protected OnBaseSuotaFragmentInteractionListener baseListener; // 监听器，用于与宿主Activity通信

    // 抽象方法，用于保存Fragment的状态
    public abstract Bundle saveState();

    // 抽象方法，用于恢复Fragment的状态
    public abstract void restoreState(Bundle bundle);

    @Override
    public void onPause() {
        super.onPause();
        if (baseListener != null)
            baseListener.onSuotaFragmentDestroy(saveState()); // 在Fragment暂停时通知宿主Activity
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 确保宿主Activity实现了监听器接口
        if (context instanceof OnBaseSuotaFragmentInteractionListener) {
            baseListener = (OnBaseSuotaFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnBaseSuotaFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseListener = null; // 解除监听器引用
    }

    // 定义监听器接口，用于与宿主Activity通信
    public interface OnBaseSuotaFragmentInteractionListener {
        void onSuotaFragmentIdentifierUpdate(int identifier); // 更新Fragment标识
        void onSuotaFragmentDestroy(Bundle outState); // Fragment销毁时回调
    }

}
