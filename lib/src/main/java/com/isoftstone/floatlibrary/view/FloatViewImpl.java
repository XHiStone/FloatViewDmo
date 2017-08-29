package com.isoftstone.floatlibrary.view;

/**
 * FloatViewDmo
 * com.isoftstone.floatlibrary.view
 *
 * @Author: xie
 * @Time: 2017/8/29 15:47
 * @Description:
 */


public abstract class FloatViewImpl implements FloatView{
    @Override
    public int setFloatViewSideOffset() {
        return 5;
    }

    @Override
    public boolean setEnableBackground() {
        return true;
    }
}
