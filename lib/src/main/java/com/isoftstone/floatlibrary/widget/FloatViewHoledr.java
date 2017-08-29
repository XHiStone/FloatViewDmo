package com.isoftstone.floatlibrary.widget;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;

import com.isoftstone.floatlibrary.R;
import com.isoftstone.floatlibrary.anchor.CollapsedMenuAnchor;
import com.isoftstone.floatlibrary.anchor.Dragger;
import com.isoftstone.floatlibrary.anchor.MagnetPositioner;
import com.isoftstone.floatlibrary.anchor.Navigator;
import com.isoftstone.floatlibrary.anchor.Positionable;
import com.isoftstone.floatlibrary.view.FloatView;
import com.isoftstone.floatlibrary.view.HoverMenuExitRequestListener;

/**
 * FloatViewDmo
 * com.isoftstone.floatlibrary.widget
 *
 * @Author: xie
 * @Time: 2017/8/21 18:07
 * @Description:
 */


public class FloatViewHoledr extends RelativeLayout implements HoverMenuExitRequestListener {
    private CollapsedMenuAnchor mMenuAnchor;
    private Dragger mWindowDragWatcher;
    private Navigator mNavigator;
    private boolean mEnableBackground = true;
    private View mExitGradientBackground;
    private View mExitView;
    private View floatView;
    private int mFloatViewSize;
    private float mExitRadiusInPx;
    private HoverMenuExitRequestListener mExitRequestListener;
    private boolean mIsExitRegionActivated = false;
    private Point mDraggingPoint = new Point();
    private static final float EXIT_RADIUS_IN_DP = 72;

    private Positionable mSidePullerPositioner = new Positionable() {
        @Override
        public void setPosition(@NonNull Point position) {
            setCollapsedPosition(position.x, position.y);
        }
    };

    private CollapsedMenuViewController mCollapsedMenuViewController = new CollapsedMenuViewController() {

        @Override
        public void onPress() {
            onPressOfCollapsedMenu();
            if (mEnableBackground)
                startDragMode();
        }

        @Override
        public void onDragTo(@NonNull Point dragCenterPosition) {
            int left = dragCenterPosition.x - (floatView.getWidth() / 2);
            int top = dragCenterPosition.y - (floatView.getHeight() / 2);
            setCollapsedPosition(left, top);
            if (mEnableBackground)
                checkForExitRegionActivation();
        }

        @Override
        public void onRelease() {
            stopDragMode();
            onReleaseOfCollapsedMenu();
        }

        @Override
        public void pullToAnchor() {
            MagnetPositioner magnetPositioner = new MagnetPositioner(getResources().getDisplayMetrics(), mSidePullerPositioner, new MagnetPositioner.OnCompletionListener() {
                @Override
                public void onPullToSideCompleted() {
                    // TODO: this collapsed logic is duplicated
                    enableDragging();
                }
            });
            View activeTab = getActiveTab();
            Rect tabViewBounds = new Rect(
                    (int) activeTab.getX(),
                    (int) activeTab.getY(),
                    (int) activeTab.getX() + activeTab.getWidth(),
                    (int) activeTab.getY() + activeTab.getHeight());
            mMenuAnchor.setAnchorByInterpolation(tabViewBounds);
            magnetPositioner.pullToAnchor(mMenuAnchor, tabViewBounds, new BounceInterpolator());
        }
    };

    private Dragger.DragListener mDragListener = new Dragger.DragListener() {
        @Override
        public void onPress(float x, float y) {
            getCollapsedMenuViewController().onPress();
        }

        @Override
        public void onDragStart(float x, float y) {
        }

        @Override
        public void onDragTo(float x, float y) {
            getCollapsedMenuViewController().onDragTo(new Point(
                    (int) x + (getActiveTab().getWidth() / 2),
                    (int) y + (getActiveTab().getHeight() / 2))
            );
        }

        @Override
        public void onReleasedAt(float x, float y) {
            getCollapsedMenuViewController().onRelease();
            if (mEnableBackground){
                if (isActiveTabInExitRegion()) {
                    mExitRequestListener.onExitRequested();
                } else {
                    getCollapsedMenuViewController().pullToAnchor();
                }
            }else {
                getCollapsedMenuViewController().pullToAnchor();
            }
        }

        @Override
        public void onTap() {
            // Update the visual pressed state of the hover menu.
            getCollapsedMenuViewController().onRelease();
        }
    };

    private OnTouchListener mTabOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                showTabAsPressed(view);
            } else if (MotionEvent.ACTION_UP == motionEvent.getAction()) {
                showTabAsNotPressed(view);
            }
            return false;
        }
    };

    public FloatViewHoledr(Context context, @Nullable Navigator navigator, @NonNull Dragger windowDragWatcher,
                           @NonNull PointF savedAnchor) {
        super(context);
        mWindowDragWatcher = windowDragWatcher;
        //初始化floatview边缘偏移量
        mMenuAnchor = new CollapsedMenuAnchor(getResources().getDisplayMetrics(), 5);
        mMenuAnchor.setAnchorAt((int) savedAnchor.x, savedAnchor.y);
        mNavigator = null == navigator ? new DefaultNavigator(context) : navigator;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_float_holder, this, true);
        mExitGradientBackground = findViewById(R.id.view_exit_gradient);
        mExitView = findViewById(R.id.view_exit);
        mFloatViewSize = getResources().getDimensionPixelSize(R.dimen.floating_icon_size);
        mExitRadiusInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EXIT_RADIUS_IN_DP, getResources().getDisplayMetrics());
        initLayoutTransitionAnimations();
        setHoverMenuExitRequestListener(this);
    }

    private void initLayoutTransitionAnimations() {
        setLayoutTransition(new LayoutTransition());
        final LayoutTransition transition = getLayoutTransition();

        transition.setAnimator(LayoutTransition.APPEARING, createEnterObjectAnimator());

        transition.setAnimator(LayoutTransition.DISAPPEARING, createExitObjectAnimator());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            transition.disableTransitionType(LayoutTransition.APPEARING);
            transition.disableTransitionType(LayoutTransition.DISAPPEARING);
            transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
            transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
            transition.disableTransitionType(LayoutTransition.CHANGING);
        }
    }

    private ObjectAnimator createEnterObjectAnimator() {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.0f, 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.0f, 1.0f);

        // Target object doesn't matter because it is overriden by layout system.
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(new Object(), scaleX, scaleY);
        animator.setDuration(500);
        animator.setInterpolator(new OvershootInterpolator());
        return animator;
    }

    private ObjectAnimator createExitObjectAnimator() {
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.1f);

        // Target object doesn't matter because it is overriden by layout system.
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(new Object(), scaleX, scaleY);
        animator.setDuration(500);
        animator.setInterpolator(new AnticipateInterpolator());
        return animator;
    }

    public PointF getAnchorState() {
        return new PointF(mMenuAnchor.getAnchorSide(), mMenuAnchor.getAnchorNormalizedY());
    }

    public void setFloatView(FloatView view) {
        floatView = view.createFloatView();
        floatView.setOnTouchListener(mTabOnTouchListener);
        mMenuAnchor.setAnchorSideOffset(getResources().getDisplayMetrics(), view.setFloatViewSideOffset());
        mEnableBackground = view.setEnableBackground();
        if (floatView != null) {
            addFloatView(floatView);
        }
    }

    private void addFloatView(View view) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mFloatViewSize, mFloatViewSize);
        layoutParams.addRule(LEFT_OF);
        layoutParams.addRule(RelativeLayout.ALIGN_TOP);
        addView(view, layoutParams);
    }

    private void showTabAsPressed(@NonNull View tabView) {
        tabView.setScaleX(0.90f);
        tabView.setScaleY(0.90f);
    }

    private void showTabAsNotPressed(@NonNull View tabView) {
        tabView.setScaleX(1.0f);
        tabView.setScaleY(1.0f);
    }

    private void onPressOfCollapsedMenu() {
        // Scale down the active tab to give "pressed" effect.
        if (null != floatView) {
            showTabAsPressed(floatView);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Rect newBounds = new Rect(left, top, right, bottom);
        mMenuAnchor.setDisplayBounds(newBounds);
        Rect anchoredBounds = mMenuAnchor.anchor(new Rect(0, 0, floatView.getWidth(), floatView.getHeight()));
        if (!changed) {
            return;
        }
        if (null != floatView) {
            getCollapsedMenuViewController().onDragTo(new Point(
                    anchoredBounds.left + (floatView.getWidth() / 2),
                    anchoredBounds.top + (floatView.getHeight() / 2)
            ));
        } else {
            Log.e("FloatViewHolder", "There is no active tab, no need to adjust positioning during layout change.");
        }
        enableDragging();
    }

    private void enableDragging() {
        Point anchorPosition = mDraggingPoint;
        mWindowDragWatcher.deactivate();
        mWindowDragWatcher.activate(mDragListener, new Rect(
                anchorPosition.x,
                anchorPosition.y,
                anchorPosition.x + floatView.getWidth(),
                anchorPosition.y + floatView.getHeight()
        ));
    }

    private void setCollapsedPosition(int x, int y) {
        mDraggingPoint.set(x, y);

        if (null != floatView) {
            floatView.setX(mDraggingPoint.x);
            floatView.setY(mDraggingPoint.y);
        }
    }

    public CollapsedMenuViewController getCollapsedMenuViewController() {
        return mCollapsedMenuViewController;
    }

    private void startDragMode() {
        mExitGradientBackground.setAlpha(0.0f);
        ObjectAnimator.ofFloat(mExitGradientBackground, "alpha", 0.0f, 1.0f).setDuration(300).start();
        mExitGradientBackground.setVisibility(VISIBLE);

        LayoutTransition transition = getLayoutTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            transition.enableTransitionType(LayoutTransition.APPEARING);
        }

        mExitView.setVisibility(VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            transition.disableTransitionType(LayoutTransition.APPEARING);
        }
    }

    private View getActiveTab() {
        return floatView;
    }

    public boolean isActiveTabInExitRegion() {
        PointF centerOfExitView = new PointF(mExitView.getX() + (mExitView.getWidth() / 2), mExitView.getY() + (mExitView.getHeight() / 2));
        PointF iconPosition = new PointF(floatView.getX() + (floatView.getWidth() / 2), floatView.getY() + (floatView.getHeight() / 2));
        double distance = getDistanceBetweenTwoPoints(iconPosition.x, iconPosition.y, centerOfExitView.x, centerOfExitView.y);
        return distance <= mExitRadiusInPx;
    }

    private double getDistanceBetweenTwoPoints(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private void checkForExitRegionActivation() {
        if (!mIsExitRegionActivated && isActiveTabInExitRegion()) {
            activateExitRegion();
        } else if (mIsExitRegionActivated && !isActiveTabInExitRegion()) {
            deactivateExitRegion();
        }
    }

    private void activateExitRegion() {
        mIsExitRegionActivated = true;
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
        mExitView.setScaleX(1.25f);
        mExitView.setScaleY(1.25f);
    }

    private void deactivateExitRegion() {
        mIsExitRegionActivated = false;
        mExitView.setScaleX(1.0f);
        mExitView.setScaleY(1.0f);
    }

    private void stopDragMode() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mExitGradientBackground, "alpha", 1.0f, 0.0f).setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mExitGradientBackground.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.start();

        LayoutTransition transition = getLayoutTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            transition.enableTransitionType(LayoutTransition.DISAPPEARING);
        }

        mExitView.setVisibility(INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        }
    }

    private void onReleaseOfCollapsedMenu() {
        if (null != floatView) {
            showTabAsNotPressed(floatView);
        }
    }


    public void setHoverMenuExitRequestListener(HoverMenuExitRequestListener exitRequestListener) {
        mExitRequestListener = exitRequestListener;
    }

    @Override
    public void onExitRequested() {
        floatView.setVisibility(GONE);
    }

    public interface CollapsedMenuViewController {
        void onPress();

        void onDragTo(@NonNull Point dragCenterPosition);

        void onRelease();

        void pullToAnchor();
    }

}
