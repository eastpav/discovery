package org.eastpav.discovery.mq;

/**
 * @描述: MQ消息接收监听器
 * @作者: 张尧
 * @创建时间: 2018-08-16 16:40:12
 */
public interface MessageListener {
    /**
     * 消息接收回调
     * @param topic 接收到消息的主题
     * @param payload 消息内容
     */
    void onMessage(String topic, byte[] payload);
}
