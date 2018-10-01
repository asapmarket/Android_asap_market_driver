package com.mhysa.waimai.kuaidi.event;

/**
 * Event通知实体类模版
 * <p>
 * Date: 2017/7/25
 *
 * @author xusheng
 */

public class ApplicationEvent<T> {

    // 通知id，需要保持应用级别的唯一性
    // 定义于VivaEventID类
    private final int mEventId;

    // 通知说明
    private String mEventMessage;

    // 通知实体
    private T mData;

    public ApplicationEvent(int eventId) {
        this.mEventId = eventId;
    }

    public ApplicationEvent(int eventId, String eventMessage) {
        this.mEventId = eventId;
        this.mEventMessage = eventMessage;
    }

    public ApplicationEvent(int eventId, T data) {
        this.mEventId = eventId;
        this.mData = data;
    }

    public ApplicationEvent(int eventId, String eventMessage, T data) {
        this.mEventId = eventId;
        this.mEventMessage = eventMessage;
        this.mData = data;
    }

    public int getEventId() {
        return mEventId;
    }

    public String getEventMessage() {
        return mEventMessage;
    }

    public T getData() {
        return mData;
    }
}
