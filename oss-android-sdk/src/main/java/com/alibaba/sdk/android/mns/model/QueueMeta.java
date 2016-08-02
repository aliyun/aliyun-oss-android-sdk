package com.alibaba.sdk.android.mns.model;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import java.util.Date;

public class QueueMeta {
    protected String queueName = null;
    protected Long delaySeconds = null;
    protected Long messageRetentionPeriod = null;
    protected Long maxMessageSize = null;
    protected Long visibilityTimeout = null;

    protected Date createTime = null;
    protected Date lastModifyTime = null;
    protected Integer pollingWaitSeconds = null;

    protected Long activeMessages = null;
    protected Long inactiveMessages = null;
    protected Long delayMessages = null;
    protected String queueURL = null;
    protected Integer loggingEnabled = null;


    /**
     * 队列是否开通了Logging功能
     */
    public Integer getLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * 设置开通队列的Logging功能
     */
    public void setLoggingEnabled(Integer loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        if (loggingEnabled)
            this.loggingEnabled = 1;
        else
            this.loggingEnabled = 0;
    }
    /**
     * 获取队列的名字
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * 设置队列的名字
     *
     * @param queueName
     */
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    /**
     * 获取队列的延时消息的延时，单位是秒
     */
    public Long getDelaySeconds() {
        return delaySeconds;
    }

    /**
     * 设置队列的延时消息的延时，单位是秒
     *
     * @param delaySeconds
     */
    public void setDelaySeconds(Long delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    /**
     * 获取队列消息的最长存活时间，单位是秒
     */
    public Long getMessageRetentionPeriod() {
        return messageRetentionPeriod;
    }

    /**
     * 设置队列消息的最长存活时间，单位是秒
     *
     * @param messageRetentionPeriod
     */
    public void setMessageRetentionPeriod(Long messageRetentionPeriod) {
        this.messageRetentionPeriod = messageRetentionPeriod;
    }

    /**
     * 获取队列消息的最大长度，单位是byte
     */
    public Long getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * 设置队列消息的最大长度，单位是byte
     *
     * @param maxMessageSize
     */
    public void setMaxMessageSize(Long maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * 获取队列消息的长轮询等待时间，单位是秒
     */
    public Integer getPollingWaitSeconds() {
        return pollingWaitSeconds;
    }

    /**
     * 设置队列消息的长轮询等待时间，单位是秒
     *
     * @param pollingWaitseconds
     */
    public void setPollingWaitSeconds(Integer pollingWaitseconds) {
        this.pollingWaitSeconds = pollingWaitseconds;
    }

    /**
     * 获取队列消息的不可见时间，单位是秒
     */
    public Long getVisibilityTimeout() {
        return visibilityTimeout;
    }

    /**
     * 设置队列消息的不可见时间，单位是秒
     *
     * @param visibilityTimeout
     */
    public void setVisibilityTimeout(Long visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }

    /**
     * 获取队列的创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取队列的最后修改时间
     */
    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    public void setLastModifyTime(Date lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }

    /**
     * 获取队列中活跃消息数
     */
    public Long getActiveMessages() {
        return activeMessages;
    }

    public void setActiveMessages(Long activeMessages) {
        this.activeMessages = activeMessages;
    }

    /**
     * 获取队列中不活跃消息数
     */
    public Long getInactiveMessages() {
        return inactiveMessages;
    }

    public void setInactiveMessages(Long inactiveMessages) {
        this.inactiveMessages = inactiveMessages;
    }

    /**
     * 获取队列中延时消息数
     */
    public Long getDelayMessages() {
        return delayMessages;
    }

    public void setDelayMessages(Long delayMessages) {
        this.delayMessages = delayMessages;
    }

    /**
     * 获取队列的URL
     */
    public String getQueueURL() {
        return queueURL;
    }

    /**
     * 设置队列的URL
     */
    public void setQueueURL(String queueURL) {
        this.queueURL = queueURL;
    }
}
