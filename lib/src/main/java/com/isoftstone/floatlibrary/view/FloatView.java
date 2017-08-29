package com.isoftstone.floatlibrary.view;

import android.view.View;

/**
 * FloatViewDmo
 * com.isoftstone.floatlibrary.view
 *
 * @Author: xie
 * @Time: 2017/8/29 9:50
 * @Description:
 */


public interface FloatView {
    View createFloatView();
    int setFloatViewSideOffset();
    boolean setEnableBackground();
}
