package org.eastpav.discovery.mq;

/**
 * @描述: 基于发布订阅的消息队列接口
 * @作者: 张尧
 * @创建时间: 2018-08-16 11:05:15
 */
public interface MqClient {

    /**
     * 增加订阅主题，在doConnect()之前调用
     * @param topic 主题名
     */
    void addTopic(String topic);

    /**
     * 执行连接消息队列
     */
    void doConnect() throws Exception;

    /**
     * 节点订阅主题接口
     * @param topic 主题名称
     */
    boolean subscribe(String topic);

    /**
     * 节点间消息发送接口
     * @param topic 消息主题，及对端节点名
     * @param data 消息二进制数据
     */
    void send(String topic, byte[] data);

    /**
     * 设置消息接收监听器
     * @param listener 消息监听器
     */
    void setMessageListener(MessageListener listener);

    /**
     * 设置SSL配置，调用后，即开启SSL连接
     * @param keyPath key文件路径（PKCS12）
     * @param keyPassword key密码
     * @param trustPath ca证书store文件路径（JKS）
     * @param trustPassword ca证书store密码
     */
    void setSSL(String keyPath, String keyPassword, String trustPath, String trustPassword) throws Exception;

    /**
     * 关闭客户端
     */
    void close();
}
