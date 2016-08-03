package com.alibaba.sdk.android.mns.model;

import java.util.Date;

/**
 * Created by pan.zengp on 2016/7/31.
 */
public class Message {
    private String receiptHandle = null;
    private Integer priority = null;
    private Date enqueueTime = null;
    private Date nextVisibleTime = null;
    private Date firstDequeueTime = null;
    private Integer dequeueCount = null;
    private Integer delaySeconds = null;
    private String messageBody = null;

    private String messageId = null;
    private String messageBodyMd5 = null;

    /**
     * 获取消息延时，单位是秒
     */
    public Integer getDelaySeconds() {
        return delaySeconds;
    }

    /**
     * 设置消息延时，单位是秒
     *
     * @param delaySeconds
     */
    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }


    /**
     * 获取消息句柄
     */
    public String getReceiptHandle() {
        return receiptHandle;
    }

    /**
     * 设置消息句柄
     *
     * @param receiptHandle
     */
    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }

    /**
     * 获取消息入队时间
     */
    public Date getEnqueueTime() {
        return enqueueTime;
    }

    public void setEnqueueTime(Date enqueueTime) {
        this.enqueueTime = enqueueTime;
    }

    /**
     * 获取消息下次可见时间
     */
    public Date getNextVisibleTime() {
        return nextVisibleTime;
    }

    public void setNextVisibleTime(Date nextVisibleTime) {
        this.nextVisibleTime = nextVisibleTime;
    }

    /**
     * 获取消息第一次入队时间
     */
    public Date getFirstDequeueTime() {
        return firstDequeueTime;
    }

    public void setFirstDequeueTime(Date firstDequeueTime) {
        this.firstDequeueTime = firstDequeueTime;
    }

    /**
     * 获取消息出队次数
     */
    public Integer getDequeueCount() {
        return dequeueCount;
    }

    public void setDequeueCount(int dequeueCount) {
        this.dequeueCount = dequeueCount;
    }

    /**
     * 设置消息体
     *
     * @param messageBody
     */
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    /**
     * 获取消息体，文本类型
     *
     * @return
     */
    public String getMessageBody() {
        return messageBody;
    }

    /**
     * 获取消息的优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 设置消息的优先级
     *
     * @param priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 获取消息ID，发送消息请求返回
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * 设置消息ID，发送消息请求返回
     *
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * 获取消息体Md5值，发送消息请求返回
     */
    public String getMessageBodyMd5() {
        return messageBodyMd5;
    }

    /**
     * 获取消息体Md5值，发送消息请求返回
     */
    public void setMessageBodyMd5(String messageBodyMd5) { this.messageBodyMd5 = messageBodyMd5; }


}
