package com.kye.smart.simplevideorecord.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.kye.smart.simplevideorecord.R;
import com.kye.smart.simplevideorecord.ulits.ViewUtils;


/**
 * @Package com.internalkye.express.module.onekeyrepair.widget
 * @author: llw
 * @Description: 视频录制按钮
 * @date: 2017/12/14 17:13
 */
public class RecordVideoButton extends View {

    private static final int WHAT_LONG_CLICK = 1;
    private Paint mProgressPain;
    private RectF mRectF;
    private int mMeasuredWidth;
    private boolean mIsFirstTime = true;
    private float mProgress;
    private float mMaxTime;
    private OnGestureListener mOnGestureListener;
    private ValueAnimator mValueAnimator;
    private float mSwipProgress = 0;
    private float mStartProgress = 0;
    private boolean mIsPressed;
    private boolean mIsRecording;
    /**
     * 剩余绘制时长
     */
    private float mAnimatorTime;
    private int mMinTime;
    private int mProgressColor;
    private float mProgressWidth;
    private Paint mInnerCirclePain;
    private float mInnerRadiu;
    private float mInnerCurrentRadiu;
    private float mOutterRadiu;
    private float mOutterCurrentRadiu;
    private Paint mOutterCirclePaint;
    private int mInnerCircleColor;
    private int mOutterCircleColor;
    private long mDownTime;
    private long mLongClickTime = 500;
    private long mEndTime;
    private long mStartTime;
    private Paint mLeftCirclePain;
    private boolean mIsCompleted = false;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_LONG_CLICK:
                    if (mOnGestureListener != null) {
                        mOnGestureListener.setOnLongClickListener();
                    }
                    mDownTime = System.currentTimeMillis();
                    startChangeAnimation(mOutterRadiu, mOutterRadiu * 1.33f, mInnerRadiu, mInnerRadiu * 0.75f);
                    break;

                default:
                    break;
            }
        }
    };
    private int mHeight;


    public RecordVideoButton(Context context) {
        super(context);
        init(context, null);
    }

    public RecordVideoButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RecordVideoButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecordVideoButton);
        //最长时间
        mMaxTime = a.getInt(R.styleable.RecordVideoButton_maxProgress, 15 * 1000);
        mAnimatorTime = mMaxTime;
        //最小时间
        mMinTime = a.getInt(R.styleable.RecordVideoButton_minProgess, 5 * 1000);
        //内圆填充颜色
        mInnerCircleColor = a.getColor(R.styleable.RecordVideoButton_innerCircleColor, Color.parseColor("#FFFFFF"));
        //外圆填充颜色
        mOutterCircleColor = a.getColor(R.styleable.RecordVideoButton_outterCircleColor, Color.parseColor("#DDDDDD"));
        //进度条颜色
        mProgressColor = a.getColor(R.styleable.RecordVideoButton_progressColor, Color.GREEN);
        //进度条宽度
        mProgressWidth = a.getDimension(R.styleable.RecordVideoButton_progressWidth, 20f);
        //内圆半径
        mInnerRadiu = a.getDimension(R.styleable.RecordVideoButton_innerRadiu, 140f);
        mInnerCurrentRadiu = mInnerRadiu;
        //外圆半径
        mOutterRadiu = a.getDimension(R.styleable.RecordVideoButton_outterRadiu, 160f);
        mOutterCurrentRadiu = mOutterRadiu;
        a.recycle();
        initInnerCircle();
        initOutterCircle();
        initProgressBar();
        initLeftCircle();

        mRectF = new RectF();
    }

    private void initLeftCircle() {
        mLeftCirclePain = new Paint();
        mLeftCirclePain.setAntiAlias(true);
        mLeftCirclePain.setStyle(Paint.Style.FILL);
        mLeftCirclePain.setColor(Color.WHITE);
    }

    /**
     * 进度条画笔初始化
     */
    private void initProgressBar() {
        //白色圆形按钮
        mProgressPain = new Paint();
        mProgressPain.setStrokeCap(Paint.Cap.ROUND);
        mProgressPain.setAntiAlias(true);
        mProgressPain.setStyle(Paint.Style.STROKE);
        mProgressPain.setStrokeWidth(mProgressWidth);
        mProgressPain.setColor(mProgressColor);
    }

    /**
     * 外圆画笔初始化
     */
    private void initOutterCircle() {
        mOutterCirclePaint = new Paint();
        mOutterCirclePaint.setAntiAlias(true);
        mOutterCirclePaint.setStrokeWidth(mProgressWidth);
        mOutterCirclePaint.setStyle(Paint.Style.FILL);
        mOutterCirclePaint.setColor(mOutterCircleColor);
    }

    /**
     * 内圆画笔初始化
     */
    private void initInnerCircle() {
        mInnerCirclePain = new Paint();
        mInnerCirclePain.setAntiAlias(true);
        mInnerCirclePain.setStyle(Paint.Style.FILL);
        mInnerCirclePain.setColor(mInnerCircleColor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ViewUtils.isFastDoubleClick(500)) {

                } else {
                    mStartTime = System.currentTimeMillis();
                    mIsPressed = true;
                    mIsRecording = true;
                    Message mMessage = Message.obtain();
                    mMessage.what = WHAT_LONG_CLICK;
                    resetParams();
                    mHandler.sendMessageDelayed(mMessage, mLongClickTime);
                }


                break;

            case MotionEvent.ACTION_UP:

                mIsPressed = false;
                mInnerCurrentRadiu = mInnerRadiu;
                mOutterCurrentRadiu = mOutterRadiu;
                mEndTime = System.currentTimeMillis();
                if (mEndTime - mStartTime < mLongClickTime) {
                    mHandler.removeMessages(WHAT_LONG_CLICK);

                } else {
                    if (mAnimatorTime > mMaxTime - mMinTime) {
                        mSwipProgress = 0;
                    }
                    startChangeAnimation(mOutterRadiu * 1.33f, mOutterRadiu, mInnerRadiu * 0.75f, mInnerRadiu);//手指离开时动画复原
                }

                mIsCompleted = mAnimatorTime == 0;
//                mIsRecording = false;
                stopAnimation();
                break;
            default:
                break;
        }
        return true;

    }

    /**
     * 重置动画参数
     */
    public void resetParams() {
        mAnimatorTime = mMaxTime;
        mSwipProgress = 0;
        mStartProgress = 0;
    }

    private void startChangeAnimation(float bigStart, float bigEnd, float smallStart, float smallEnd) {

        ValueAnimator innerAnimator = ValueAnimator.ofFloat(smallStart, smallEnd);
        ValueAnimator outterAnimator = ValueAnimator.ofFloat(bigStart, bigEnd);
        innerAnimator.setDuration(200);
        outterAnimator.setDuration(200);

        outterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mOutterCurrentRadiu = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        innerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mInnerCurrentRadiu = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        outterAnimator.start();
        innerAnimator.start();

        innerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (mIsPressed) {
                    mIsRecording = true;
                    mIsCompleted = false;
                    startAnimation(mStartProgress, 360, (long) mAnimatorTime);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }


    private void stopAnimation() {
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            if (mOnGestureListener != null) {
                mOnGestureListener.onPause(mAnimatorTime);
            }
            mValueAnimator.cancel();

        }
    }


    /**
     * 绘制进度条
     */
    private void drawProgressBar() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingBottom = getPaddingBottom();
        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int heightSpec = MeasureSpec.getMode(heightMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightSpec) {
            case MeasureSpec.UNSPECIFIED:
                mHeight = (int) (2 * mOutterRadiu * 1.33 + paddingBottom + paddingTop);
                break;
            case MeasureSpec.AT_MOST:
                mHeight = (int) (2 * mOutterRadiu * 1.33 + paddingBottom + paddingTop);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                break;
        }

        int widthSpec = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthSpec) {
            case MeasureSpec.UNSPECIFIED:
                width = (int) (2 * mOutterRadiu * 1.33 + paddingLeft + paddingRight);
                break;
            case MeasureSpec.AT_MOST:
                width = (int) (2 * mOutterRadiu * 1.33 + paddingLeft + paddingRight);
                break;
            case MeasureSpec.EXACTLY:
                break;
            default:
                break;
        }
        mMeasuredWidth = width;
        setMeasuredDimension(mMeasuredWidth, mHeight);
        Log.d("llw", "测量宽:" + mMeasuredWidth + ",高:" + mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制外圆
        canvas.drawCircle(mMeasuredWidth / 2f, mHeight / 2f, mOutterCurrentRadiu, mOutterCirclePaint);
        //绘制内圆
        canvas.drawCircle(mMeasuredWidth / 2f, mHeight / 2f, mInnerCurrentRadiu, mInnerCirclePain);
        //绘制进度条
//        if (mIsRecording) {
        mRectF.left = (mMeasuredWidth / 2f - mOutterCurrentRadiu + mProgressWidth / 2f);
        mRectF.top = (mHeight / 2f - mOutterCurrentRadiu + mProgressWidth / 2f);
        mRectF.right = (mMeasuredWidth / 2f + mOutterCurrentRadiu - mProgressWidth / 2f);
        mRectF.bottom = (mHeight / 2f + mOutterCurrentRadiu - mProgressWidth / 2f);
//        canvas.drawArc(mRectF, 270, mSwipProgress, false, mProgressPain);
//        }
    }

    private void startAnimation(float startProgress, float endProgress, long durationTime) {

        mValueAnimator = ValueAnimator.ofFloat(startProgress, endProgress);
        //匀速
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setDuration(durationTime);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mSwipProgress = (float) valueAnimator.getAnimatedValue();
//                invalidate();
                mStartProgress = mSwipProgress;
                mAnimatorTime = mMaxTime * (1 - mSwipProgress / 360);
                if (mOnGestureListener != null) {
                    mOnGestureListener.remainingTime(mAnimatorTime);
                }
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mIsCompleted = false;
                Log.d("llw", "onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d("llw", "onAnimationEnd");
                mIsCompleted = true;
                if (mOnGestureListener != null) {
                    mOnGestureListener.completed(mAnimatorTime);
                }
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                Log.d("llw", "onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.d("llw", "onAnimationRepeat");
            }
        });
        mValueAnimator.start();
    }

    public void setProgress(float f) {
        mProgress = f / mMaxTime * 360;
        startAnimation(270, mProgress, (long) mAnimatorTime);
    }

    public void setMaxProgress(float f) {
        this.mMaxTime = f;
        invalidate();
    }

    /**
     * 重置进度条
     */
    public void resetProgress() {
        resetParams();
        if (mIsPressed) {
            mIsPressed = false;
            mInnerCurrentRadiu = mInnerRadiu;
            mOutterCurrentRadiu = mOutterRadiu;
            mEndTime = System.currentTimeMillis();
            if (mEndTime - mStartTime < mLongClickTime) {
                mHandler.removeMessages(WHAT_LONG_CLICK);

            } else {
                if (mAnimatorTime > mMaxTime - mMinTime) {
                    mSwipProgress = 0;
                }
                startChangeAnimation(mOutterRadiu * 1.33f, mOutterRadiu, mInnerRadiu * 0.75f, mInnerRadiu);//手指离开时动画复原
            }

            mIsCompleted = mAnimatorTime == 0;
//                mIsRecording = false;
            stopAnimation();
        } else {
            invalidate();
        }
    }

    public interface OnGestureListener {
        /**
         * 长按事件
         */
        void setOnLongClickListener();

        /**
         * 暂停
         *
         * @param animatorTime
         */
        void onPause(float animatorTime);

        /**
         * 完成
         */
        void completed(float animatorTime);

        /**
         * 剩余时间
         *
         * @param time
         */
        void remainingTime(float time);

        /**
         * 操作异常事件处理
         */
        void exceptionMessage(long downTime);
    }

    public void setLongClickListener(OnGestureListener l) {
        mOnGestureListener = l;
    }
}
