package com.mhysa.waimai.kuaidi.ui.customerviews;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.joey.devilfish.utils.ExtendUtils;
import com.mhysa.waimai.kuaidi.R;

/**
 * 文件描述
 * Date: 2017/8/9
 *
 * @author xusheng
 */

public class CircleImageView extends AppCompatImageView {
    /**
     * 转动频率
     */
    private static final int INCREMENT = 30;

    /**
     * 延迟时间
     */
    private static final int DELAY_TIME = 80;

    private Handler mHandler;

    /**
     * 是否停止
     */
    private boolean mIsStop;

    private int mMsgCount;

    private int mArc;

    private Context mContext;

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public CircleImageView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }

    /**
     * 停止
     */
    public void stop() {
        mIsStop = true;
        invalidate();
    }

    /**
     * 开始
     */
    public void start() {
        mIsStop = false;
        if (mMsgCount == 0) {
            mHandler.sendEmptyMessage(0);
            mMsgCount++;
        }
    }

    private void init() {
        if (getDrawable() == null) {
            setImageDrawable(getResources().getDrawable(R.mipmap.ic_refresh));
        }

        setLayoutParams(new ViewGroup.LayoutParams(ExtendUtils.getInstance().dip2px(mContext, 25),
                ExtendUtils.getInstance().dip2px(mContext, 25)));

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mMsgCount--;
                if (!mIsStop) {
                    invalidate();
                    mArc = (mArc + INCREMENT) % 360;
                    sendEmptyMessageDelayed(0, DELAY_TIME);
                    mMsgCount++;
                }
                if (mIsStop) {
                    mArc = 0;
                    invalidate();
                }
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(mArc, getWidth() / 2, getHeight() / 2);
        super.onDraw(canvas);
    }
}