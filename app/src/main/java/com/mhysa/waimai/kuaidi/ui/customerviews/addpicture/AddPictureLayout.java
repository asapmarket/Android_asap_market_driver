package com.mhysa.waimai.kuaidi.ui.customerviews.addpicture;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.joey.devilfish.ui.activity.picture.PictureViewActivity;
import com.joey.devilfish.utils.ExtendUtils;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.model.errand.ErrandImage;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件描述
 * Date: 2018/1/18
 *
 * @author xusheng
 */

public class AddPictureLayout extends LinearLayout {

    private Context mContext;

    private View mRootView;

    private LinearLayout mContentLayout;

    private List<Uri> mPictureUris = new ArrayList<Uri>();

    private OnOperateListener mListener;

    private int mItemWidth;

    private int mItemHeight;

    private int mNumInLine;

    private int mLimitSize;

    private boolean mIsView = false;

    public AddPictureLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public AddPictureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_add_picture, null);
        mContentLayout = (LinearLayout) mRootView.findViewById(R.id.layout_content);

        this.addView(mRootView);
    }

    public void setIsView(boolean isView) {
        this.mIsView = isView;
    }

    public void setNumInLine(int numInLine) {
        this.mNumInLine = numInLine;
    }

    public void setLimitSize(int limitSize) {
        this.mLimitSize = limitSize;
    }

    public void setDimensions(int itemWidth, int itemHeight) {
        this.mItemHeight = itemHeight;
        this.mItemWidth = itemWidth;
    }

    public void setListener(OnOperateListener listener) {
        this.mListener = listener;
    }

    public void addPictures(final List<ErrandImage> errandImages) {
        if (null == errandImages || errandImages.size() == 0) {
            mContentLayout.removeAllViews();
            return;
        } else {
            mContentLayout.removeAllViews();

            LayoutParams params1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.leftMargin = ExtendUtils.getInstance().dip2px(mContext, 10);

            final int size = errandImages.size();

            int tempSize = size / mNumInLine;
            if (tempSize != 0) {
                for (int j = 0; j < tempSize; j++) {
                    LinearLayout linearLayout = new LinearLayout(mContext);
                    linearLayout.setOrientation(HORIZONTAL);
                    mContentLayout.addView(linearLayout, params1);

                    for (int k = j * mNumInLine; k < j * mNumInLine + mNumInLine; k++) {
                        final int tempPosition = k;
                        PictureItemLayout pictureItemLayout = new PictureItemLayout(mContext);
                        pictureItemLayout.setDimensions(mItemWidth, mItemHeight);
                        pictureItemLayout.setDataWithUrl(errandImages.get(k).path);

                        pictureItemLayout.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewPictures(errandImages, tempPosition);
                            }
                        });
                        linearLayout.addView(pictureItemLayout, params2);
                    }
                }
            }

            if (size % mNumInLine != 0) {
                LinearLayout linearLayout = new LinearLayout(mContext);
                linearLayout.setOrientation(HORIZONTAL);
                mContentLayout.addView(linearLayout, params1);

                for (int k = tempSize * mNumInLine; k < size; k++) {
                    final int tempPosition = k;
                    PictureItemLayout pictureItemLayout = new PictureItemLayout(mContext);
                    pictureItemLayout.setDimensions(mItemWidth, mItemHeight);
                    pictureItemLayout.setDataWithUrl(errandImages.get(k).path);

                    pictureItemLayout.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            viewPictures(errandImages, tempPosition);
                        }
                    });
                    linearLayout.addView(pictureItemLayout, params2);
                }
            }
        }
    }

    private void viewPictures(List<ErrandImage> errandImages, int position) {
        if (!mIsView) {
            return;
        }
        List<String> paths = new ArrayList<String>();
        final int size = errandImages.size();
        for (int i = 0; i < size; i++) {
            ErrandImage errandImage = errandImages.get(i);
            if (null != errandImage
                    && !StringUtils.getInstance().isNullOrEmpty(errandImage.path)) {
                paths.add(errandImage.path);
            }
        }

        if (null != paths && paths.size() > position) {
            PictureViewActivity.forwardPictureView(mContext, paths, position);
        }
    }

    public List<Uri> getPictures() {
        return mPictureUris;
    }

    public interface OnOperateListener {
        void onAddClick();
    }
}
