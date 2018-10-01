package com.mhysa.waimai.kuaidi.ui.fragments.personal;

import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.joey.devilfish.ui.fragment.base.BaseFragment;
import com.joey.devilfish.utils.ImageUtils;
import com.joey.devilfish.utils.StringUtils;
import com.joey.devilfish.utils.Utils;
import com.mhysa.waimai.kuaidi.R;
import com.mhysa.waimai.kuaidi.event.ApplicationEvent;
import com.mhysa.waimai.kuaidi.event.EventID;
import com.mhysa.waimai.kuaidi.ui.activities.order.OrderListManagerActivity;
import com.mhysa.waimai.kuaidi.ui.activities.personal.PersonalSettingActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 文件描述
 * Date: 2017/7/21
 *
 * @author xusheng
 */

public class PersonalFragment extends BaseFragment {

    @Bind(R.id.scrollView)
    PullToRefreshScrollView mScrollView;

    @Bind(R.id.tv_name)
    TextView mNameTv;

    @Bind(R.id.sdv_avatar)
    SimpleDraweeView mAvatarSdv;

    @Override
    protected int getContentLayout() {
        return R.layout.fragment_personal;
    }

    @Override
    protected void initContentView() {
        super.initContentView();
    }

    @Override
    protected void initData() {
        super.initData();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!StringUtils.getInstance().isNullOrEmpty(Utils.getNickName(mContext))) {
            mNameTv.setText(Utils.getNickName(mContext));
        } else {
            mNameTv.setText("");
        }

        if (!StringUtils.getInstance().isNullOrEmpty(Utils.getHeadImage(mContext))) {
            ImageUtils.getInstance().setImageURL(Utils.getHeadImage(mContext), mAvatarSdv);
        } else {
            ImageUtils.getInstance().setImageURL("", mAvatarSdv);
        }
    }

    @Subscribe
    public void onEvent(ApplicationEvent event) {
        final int eventId = event.getEventId();
        switch (eventId) {
            case EventID.CHANGE_NICKNAME_SUCCESS: {
                if (null != event.getEventMessage()) {
                    String nickname = event.getEventMessage();
                    Utils.setNickName(mContext, nickname);
                    mNameTv.setText(nickname);
                }
                break;
            }

            case EventID.CHANGE_AVATAR_SUCCESS: {
                if (null != event.getEventMessage()) {
                    String path = event.getEventMessage();
                    Utils.setHeadImage(mContext, path);
                    ImageUtils.getInstance().setImageURL(path, mAvatarSdv);
                }
                break;
            }
        }
    }

    @OnClick({R.id.layout_setting, R.id.layout_order_manager})
    public void onClick(View view) {
        final int id = view.getId();
        switch (id) {
            case R.id.layout_setting: {
                PersonalSettingActivity.invoke(getActivity());
                break;
            }

            case R.id.layout_order_manager: {
                OrderListManagerActivity.invoke(getActivity());
                break;
            }
        }
    }
}
