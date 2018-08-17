package org.eastpav.discovery.node;

import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.eastpav.discovery.config.Config;
import org.eastpav.discovery.config.NodeConfig;
import org.eastpav.discovery.mq.MessageListener;
import org.eastpav.discovery.util.PathInfo;

import java.util.List;

/**
 * 服务节点接口.
 * 定义一个节点需要实现的方法
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/19.
 */
public interface Node extends PathChildrenCacheListener {

    /**
     * 添加监听的路径监听缓存对象
     * @param nodeType 监听的节点类型
     * @param childrenCache 监听的节点的缓存对象
     */
    void addCache(String nodeType, PathChildrenCache childrenCache);

    /**
     * 添加服务节点监听器
     * @param listener 监听器
     */
    void addNodeListener(ServerNodeListener listener);

    /**
     * 添加节点出席监听器
     * @param listener 出席监听器
     */
    void addPresentListener(PresentListener listener);

    /**
     * 添加监听节点类型对应的路由规则
     * @param nodeType 对端节点（消息接收）的节点类型
     * @param routeRule 路由规则
     */
    void addRouteRule(String nodeType, RouteRule routeRule);

    /**
     * 创建节点发送器
     * @return 节点发送器
     */
    //INCClient createNodeSender();

    /**
     * 创建节点接收器
     * @param messageListener 节点接收处理器
     */
    void addMessageListener(MessageListener messageListener);

    /**
     * 节点使能/禁用INC
     * @param enable true - 使能INC，false - 禁用INC
     */
    void enableInc(boolean enable);

    /**
     * 获取节点对象的Config引用
     * @return 节点的配置
     */
    NodeConfig getConfig();

    /**
     * 从配置中心获取节点的配置(依照默认的zookeeper拓扑结构定义节点的配置路径)
     * @param localConfigBean 本地默认配置对象，若无远程配置数据则使用本地配置
     *                        当远程配置数据更新时，覆盖该配置
     * @return 节点的配置
     */
    NodeConfig getConfig(Object localConfigBean);

    /**
     * 获取节点对象的集群间全局Config引用
     * @return 节点的配置
     */
    NodeConfig getGlobalConfig();

    /**
     * 获取节点对象的集群间全局Config引用
     * @param localConfigBean 本地默认配置对象，若无远程配置数据则使用本地配置
     *                        当远程配置数据更新时，覆盖该配置
     * @return 节点的配置
     */
    NodeConfig getGlobalConfig(Object localConfigBean);

    /**
     * 获取节点的运行环境
     * @return 运行环境字符串
     */
    String getEnvironment();

    /**
     * 获取节点完全名
     * @return 节点完全名
     */
    String getName();

    /**
     * 获取节点类型
     * @return 节点类型
     */
    String getNodeType();

    /**
     * 获取节点类型，包含environment
     * @return 节点类型
     */
    String getNodeTypeName();

    /**
     * 获取节点简单名（不包含前缀路径的名称）
     * @return 节点简单名
     */
    String getSimpleName();

    /**
     * 获取指定监听服务类型的节点path信息
     * @param nodeType 服务类型
     * @return 节点path信息列表
     */
    List<PathInfo> getWatched(String nodeType);

    /**
     * 获取所有监听服务类型的节点path信息
     * @return 节点path信息列表
     */
    List<PathInfo> getWatched();

    /**
     * 检查节点是否已经发布出席
     * @return true - 已出席， false - 未出席
     */
    boolean isPresented();

    /**
     * 节点离场
     */
    void leave();

    /**
     * 检查指定的节点是否存在
     * @param nodeName 节点路径名
     * @return true - 存在， false - 不存在
     */
    boolean nodeAvailable(String nodeName);

    /**
     * 发布节点出席，使用节点名为topic。
     */
    void present();

    /**
     * 使用指定的topic，发布节点出席，节点从该topic获取发送到它的消息。
     */
    void present(String... topic);

    /**
     * 发布节点出席
     */
    void presentWithNodeData(String nodeData, String... topic);

    /**
     * 节点重新出席（节点名不变）
     */
    void represent();

    /**
     * 选择节点间通信
     * @param inc 节点间通信的实现
     */
    void selectINC(INC inc, String host, int port);

    /**
     * 添加一个协作节点
     * 使本节点能够获得协作节点的上线、更新和下线事件
     * @param nodeType 协作节点类型
     * @param routeRule 路由规则
     */
    void watchNode(String nodeType, RouteRule routeRule);

    /**
     * 节点消息发送，用于向指定类型的节点发送数据，具体哪个节点由路由决定
     * 发送的内部流程如下：
     * 1、根据nodeType找到对应的路由
     * 2、根据事先设置的路由规则，根据key查找目标节点
     * 3、向目标节点发送消息。
     * @param nodeType INC消息对象
     * @param key 路由查找键（String或Integer对象）
     * @param data 发送的数据
     * @return true - 成功，false - 失败
     */
    boolean send(String nodeType, Object key, byte[] data);

    /**
     * 向指定节点发送消息，多用于消息回复
     * @param nodeName 节点名称
     * @param data 数据
     */
    boolean send(String nodeName, byte[] data);

    /**
     * 节点增加主题订阅
     * @param topic 主题名称
     */
    boolean subscribe(String topic);
}
