package com.isoftstone.floatlibrary.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.isoftstone.floatlibrary.BuildConfig;
import com.isoftstone.floatlibrary.anchor.InViewGroupDragger;
import com.isoftstone.floatlibrary.view.FloatView;

/**
 * FloatViewDmo
 * com.isoftstone.floatlibrary.widget
 *
 * @Author: xie
 * @Time: 2017/8/21 16:47
 * @Description:
 */


public class FloatViewLayout extends FrameLayout {
    private final String SPFILE = "floatlocation";
    private final String PREFS_KEY_ANCHOR_SIDE = "anchor_side";
    private final String PREFS_KEY_ANCHOR_Y = "anchor_y";
    private SharedPreferences mPrefs;
    private FloatViewHoledr mFloatViewHolder;
    private InViewGroupDragger mDragger;


    public FloatViewLayout(Context context) {
        this(context, null);
    }

    public FloatViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPrefs = getContext().getSharedPreferences(SPFILE, Context.MODE_PRIVATE);
        mDragger = new InViewGroupDragger(this, ViewConfiguration.get(getContext()).getScaledTouchSlop());
        mDragger.setDebugMode(BuildConfig.DEBUG);
        mFloatViewHolder = new FloatViewHoledr(getContext(), null, mDragger, loadSavedAnchorState());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        addView(mFloatViewHolder, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onDetachedFromWindow() {
        saveAnchorState();
        removeView(mFloatViewHolder);
        mFloatViewHolder = null;
        mDragger.deactivate();
        super.onDetachedFromWindow();
    }

    private void saveAnchorState() {
        PointF anchorState = mFloatViewHolder.getAnchorState();
        mPrefs.edit()
                .putFloat(PREFS_KEY_ANCHOR_SIDE, anchorState.x)
                .putFloat(PREFS_KEY_ANCHOR_Y, anchorState.y)
                .apply();
    }

    private PointF loadSavedAnchorState() {
        return new PointF(
                mPrefs.getFloat(PREFS_KEY_ANCHOR_SIDE, 2),
                mPrefs.getFloat(PREFS_KEY_ANCHOR_Y, 0.5f)
        );
    }

    public void setmFloatView(FloatView floatView) {
        if (mFloatViewHolder != null)
            mFloatViewHolder.setFloatView(floatView);
    }
}
