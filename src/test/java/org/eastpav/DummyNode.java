package org.eastpav;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import lombok.Getter;
import org.eastpav.discovery.Discoverer;
import org.eastpav.discovery.config.ConfigChangeCallback;
import org.eastpav.discovery.config.NodeConfig;
import org.eastpav.discovery.mq.MessageListener;
import org.eastpav.discovery.node.*;
import org.eastpav.discovery.config.Config;
import org.eastpav.discovery.util.PathInfo;

import java.util.List;

/**
 * 模拟一个类型的节点.
 *
 * @author Yao Zhang
 *
 *         Created on 2018/1/19.
 */
public class DummyNode {
    private String type;
    private String listenType;
    private String listenType2;
    private String environment;
    @Getter private Node node;
    private ConfigBean nodeConfigBean = new ConfigBean();

    private NodeConfig config;
    private NodeConfig globalConfig;


    public DummyNode(String type, String listenType) {
        this.type = type;
        this.listenType = listenType;
        environment = "test";
    }

    public DummyNode(String type, String listenType, String listenType2) {
        this.type = type;
        this.listenType = listenType;
        this.listenType2 = listenType2;
        environment = "test";
    }

    public void start() {
        // 1. 新建Discoverer对象
        Discoverer discoverer = new Discoverer();
        discoverer.init("121.196.201.204:32181");

        // 2. 新建服务节点（延迟出席，当前服务节点还未分配节点名）
        node = discoverer.newNode(environment, type, false, true, nodeConfigBean);

        // 3. 获取节点配置（根据节点类型获取当前节点配置，若无远程配置，则使用本地配置）
        config = node.getConfig();
        config.setConfigChangeCallback(new ConfigChangeCallback() {
            @Override
            public void configChanged() {
                System.out.println("--------configChanged-----------");
                //nodeConfigBean.print();
            }
        });

        globalConfig = node.getGlobalConfig();
        globalConfig.setConfigChangeCallback(new ConfigChangeCallback() {
            @Override
            public void configChanged() {
                System.out.println("---------globalConfigChanged----------");
                //nodeConfigBean.print();
            }
        });

        // 4. 添加监听服务类型（0个、一个或多个）
        if(listenType != null) {
            node.watchNode(listenType, RouteRule.BY_STRING);
        }

        if(listenType2 != null) {
            node.watchNode(listenType2, RouteRule.BY_STRING);
        }

        // 6. 设置节点消息处理器
        node.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String topic, byte[] payload) {
                System.out.println(type + " -> " + new String(payload));
            }
        });

        // 7. 内部启动INC消息队列（还未执行本节点的消息订阅）
        node.selectINC(INC.EMQ, "121.196.201.204", 30183);

        // 8. 后续某个时刻节点出席
        //node.enableInc(false);
        node.present();


        // 9. 然后在需要发送消息的地方
        //node.send(message);

    }

    public void print() {
        List<PathInfo> list = node.getWatched(listenType);
        System.out.println("===========================");
        list.forEach(System.out::println);
        System.out.println("===========================");
    }

    public void printAll() {
        List<PathInfo> list = node.getWatched();
        System.out.println("===========================");
        list.forEach(System.out::println);
        System.out.println("===========================");
    }


    public void stop() {
        node.leave();
    }


    public void sendMsg(String nodeType, String key, String msg) {
        node.send(nodeType, key, msg.getBytes());
    }

}
