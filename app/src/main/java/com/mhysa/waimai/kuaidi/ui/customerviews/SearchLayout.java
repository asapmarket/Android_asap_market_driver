package com.mhysa.waimai.kuaidi.ui.customerviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joey.devilfish.utils.PromptUtils;
import com.joey.devilfish.utils.StringUtils;
import com.mhysa.waimai.kuaidi.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 搜索
 * Date: 2017/7/21
 *
 * @author xusheng
 */

public class SearchLayout extends RelativeLayout {

    private Context mContext;

    private View mRootView;

    @Bind(R.id.et_content)
    EditText mContentEt;

    @Bind(R.id.layout_choose)
    LinearLayout mChooseLayout;

    private OnSearchListener mListener;

    public SearchLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public SearchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.layout_search, null);

        ButterKnife.bind(this, mRootView);

        mContentEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String content = mContentEt.getText().toString().trim();
                    if (StringUtils.getInstance().isNullOrEmpty(content)) {
                        PromptUtils.getInstance().showShortPromptToast(mContext, R.string.search_order_hint);
                    } else {
                        if (null != mListener) {
                            mListener.onSearch(content);
                        }
                    }
                }

                return false;
            }
        });

        mChooseLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onChoose();
                }
            }
        });

        this.addView(mRootView);
    }

    public void setChooseVisibility(int visibility) {
        mChooseLayout.setVisibility(visibility);
    }

    public void setListener(OnSearchListener listener) {
        this.mListener = listener;
    }

    public interface OnSearchListener {

        void onSearch(String search);

        void onChoose();
    }
}
