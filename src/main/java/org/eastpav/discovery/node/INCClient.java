package org.eastpav.discovery.node;

/**
 * 节点间消息发送器.
 * 创建发送器时，需要绑定路由，路由是基于一致性hash的监听节点表。
 * 发送时根据消息的routeKey使用一致性hash查找对应的接收节点。
 * 该路由需要监听对端节点的上线、下线事件来更新hash环。
 * 这些工作需要在发送器创建时一起完成。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/22.
 */
public interface INCClient {
    /**
     * 节点间消息发送接口
     * @param topic 消息主题，及对端节点名
     * @param data 消息二进制数据
     */
    void send(String topic, byte[] data);

    /**
     * 节点订阅主题接口
     * @param topic 主题名称
     */
    boolean subscribe(String topic);

    /**
     * 增加订阅主题，在doConnect()之前调用
     * @param topic 主题名
     */
    void addTopic(String topic);

    /**
     * 执行连接消息队列
     */
    void doConnect();
}
