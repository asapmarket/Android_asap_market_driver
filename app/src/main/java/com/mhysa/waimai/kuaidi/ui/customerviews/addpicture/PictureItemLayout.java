package com.mhysa.waimai.kuaidi.ui.customerviews.addpicture;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.joey.devilfish.utils.ImageUtils;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;

import java.io.File;

/**
 * 图片布局
 * Date: 2018/1/18
 *
 * @author xusheng
 */

public class PictureItemLayout extends LinearLayout {

    private Context mContext;

    private View mRootView;

    private RelativeLayout mContentLayout;

    private SimpleDraweeView mPictureSdv;

    private int mItemWidth;

    private int mItemHeight;

    public PictureItemLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public PictureItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_picture_item, null);

        mPictureSdv = (SimpleDraweeView) mRootView.findViewById(R.id.sdv_picture);
        mContentLayout = (RelativeLayout) mRootView.findViewById(R.id.layout_content);

        this.addView(mRootView);
    }

    public void setDimensions(int itemWidth, int itemHeight) {
        this.mItemHeight = itemHeight;
        this.mItemWidth = itemWidth;
    }

    public void setDataWithUrl(String url) {
        if (StringUtils.getInstance().isNullOrEmpty(url)) {
            return;
        }
        LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);
        mContentLayout.setLayoutParams(params);

        ImageUtils.getInstance().setImageURL(url, mPictureSdv);

    }

    public void setDataWithResId(int resId) {
        if (resId == 0) {
            return;
        }

        LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);
        mContentLayout.setLayoutParams(params);

        ImageUtils.getInstance().setImageResId(resId, mPictureSdv);
    }

    public void setDataWithPath(String path) {
        if (StringUtils.getInstance().isNullOrEmpty(path)) {
            return;
        }

        LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);
        mContentLayout.setLayoutParams(params);

        ImageUtils.getInstance().setImageLocalPath(path, mPictureSdv);
    }

    public void setDataWithFile(File file) {
        if (null == file) {
            return;
        }

        LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);
        mContentLayout.setLayoutParams(params);

        ImageUtils.getInstance().setImageLocalPath(file.getAbsolutePath(), mPictureSdv);
    }

    public void setDataWithUri(Uri uri) {
        if (null == uri) {
            return;
        }

        LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);
        mContentLayout.setLayoutParams(params);

        ImageUtils.getInstance().setImageURI(uri, mPictureSdv);
    }
}
