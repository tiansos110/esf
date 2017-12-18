package com.example.loginapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ScrollView;


public class MyScrollView extends ScrollView {

    private int mHeaderWidth;
    private int mHeaderHeight;
    private View mHeaderView;
    private Boolean mIsPulling = false;
    private int mLastY;
    private float mScaleRatio = 0.5f;
    private float mScaleTimes = 2.0f;
    private float mReplyRatio = 0.5f;
    private int mTouchSlop;
    private int downY;

    public MyScrollView(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeaderWidth = mHeaderView.getMeasuredWidth();
        mHeaderHeight = mHeaderView.getMeasuredHeight();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOverScrollMode(OVER_SCROLL_NEVER);
        View child = getChildAt(0);
        if (child != null && child instanceof ViewGroup){
            //mHeaderView = ((ViewGroup)child).getChildAt(0);
            mHeaderView = ((ViewGroup)child).getChildAt(1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mHeaderView == null){
            return super.onTouchEvent(ev);
        }

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:

                downY = (int) ev.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                int moveY = (int) ev.getRawY();
                if (!mIsPulling){
                    if (getScrollY() == 0){
                        mLastY = (int)ev.getY();
                    }else {
                        break;
                    }
                }

                if (ev.getY() - mLastY < 0)
                    return super.onTouchEvent(ev);

                float distance = (ev.getY() - mLastY) * mScaleRatio;
                mIsPulling = true;
                setZoom(distance);

                if (Math.abs(moveY - downY) > mTouchSlop) {
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
                mIsPulling = false;
                replyView();
                break;
        }

        return super.onTouchEvent(ev);
    }

    private void setZoom(float s){
        float scaleTimes = (float) ((mHeaderWidth + s) / (mHeaderWidth * 1.0));
        //如超过最大放大倍数，直接返回
        if (scaleTimes > mScaleTimes) return;

        ViewGroup.LayoutParams layoutParams = mHeaderView.getLayoutParams();
        layoutParams.width = (int) (mHeaderWidth + s);
        layoutParams.height = (int) (mHeaderHeight * ((mHeaderWidth + s) / mHeaderWidth));
        //设置控件水平居中
        ((MarginLayoutParams) layoutParams).setMargins(-(layoutParams.width - mHeaderWidth) / 2, 0, 0, 0);
        mHeaderView.setLayoutParams(layoutParams);
    }

    private void replyView() {
        final float distance = mHeaderView.getMeasuredWidth() - mHeaderWidth;
        // 设置动画
        ValueAnimator anim = ObjectAnimator.ofFloat(distance, 0.0F).setDuration((long) (distance * mReplyRatio));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setZoom((Float) animation.getAnimatedValue());
            }
        });
        anim.start();
    }
}
