package com.kye.smart.simplevideorecord.ulits;

import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author ymh
 * @version V1.0
 * @company 跨越速运
 * @Description
 * @date 2016/3/8
 */
public class ViewUtils {
    private static final String TAG = "ViewUtils";
    private static final int TOUCH_OFFSET = 20;     // 触摸区域（左、上、右、下）的偏移量。数值越大，触摸区域越大
    private static long lastClickTime;

    /**
     * 检测是否点击了EditText输入控件之外的区域
     *
     * @param v
     * @param event
     * @return 如果点击的是EditText输入区，返回false,否则返回true
     */
    public static boolean isTouchedViewOutSideEditText(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            return isTouchedViewOutSideView(v, event);
        }
        if (v == null) {
            return false;
        }
        return true;
    }

    /**
     * 检测是否点击了EditText输入控件之外的区域
     *
     * @param v
     * @param event
     * @return 如果点击的是EditText输入区，返回false,否则返回true
     */
    public static boolean isTouchedViewOutSideView(View v, MotionEvent event) {
        if (v != null) {
            int[] leftTop = {0, 0};
            // 获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left - TOUCH_OFFSET && event.getX() < right + TOUCH_OFFSET &&
                    event.getY() > top - TOUCH_OFFSET && event.getY() < bottom + TOUCH_OFFSET) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }





    /**
     * 防止快速点击
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (300 < timeD) {
            lastClickTime = time;
            return false;
        }
        lastClickTime = time;
        return true;
    }

    /**
     * 防止快速点击
     *
     * @return
     */
    public static boolean isFastDoubleClick(long spaceTime) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;

        if (spaceTime < timeD) {
            lastClickTime = time;
            return false;
        }
//        lastClickTime = time;
        return true;
    }

    /**
     * 判断点击事件是否在控件范围内
     *
     * @param view 控件
     * @param ev   点击事件
     * @return
     */
    public static boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x
                || ev.getX() > (x + view.getWidth())
                || ev.getY() < y
                || ev.getY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }





    /**
     * 设置控件自身处理自己的触摸事件,不响应父容器的触摸事件 如ScollView
     *
     * @param v 需要设置自己处理触摸操作的控件
     */
    public static void setViewDoItselfTouchEvent(View v) {

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //这句话说的意思告诉父View我自己的事件我自己处理
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }


    /**
     * EditText竖直方向是否可以滚动
     *
     * @param editText 需要判断的EditText
     * @return true：可以滚动   false：不可以滚动
     */
    public static boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;

        if (scrollDifference == 0) {
            return false;
        }

        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }

    /**
     * EditText禁用换行按键
     *
     * @param editText 需要设置的EditText
     */
    public static void setSingleLine(EditText editText) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    /**
     * 禁止EditText输入空格,换行按键
     *
     * @param editText
     */
    public static void setEditTextInhibitInputSpaceAndEnter(EditText editText) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.equals(" ") || source.toString().contentEquals("\n")) {
                    return "";
                } else {
                    return null;
                }
            }
        };
        editText.setFilters(new InputFilter[]{filter});
    }

    public static boolean isFastDoubleClick(final View v) {
        if (v != null) {
            v.setEnabled(false);
        }
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (v != null) {
                    v.setEnabled(true);
                }
            }
        }, 300);
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (300 < timeD) {
            lastClickTime = time;
            return false;
        }
        lastClickTime = time;
        return true;
    }
}
