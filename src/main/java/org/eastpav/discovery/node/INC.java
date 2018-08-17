package org.eastpav.discovery.node;

/**
 * 节点间通信的实现选择.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/22.
 */
public enum INC {
    /**
     * 节点间消息通信使用EMQ
     */
    EMQ,

    /**
     * 节点间下次通信使用rabbitmq
     */
    RABBITMQ
}
