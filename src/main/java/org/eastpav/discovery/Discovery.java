package org.eastpav.discovery;

import java.util.List;

/**
 * 发现服务接口
 *
 * 发现服务定义与zookeeper(curator)交互的接口。其实现类及其子类在这些接口的
 * 基础上可以构建出节点注册、配置管理、命名服务、服务发现、集群消息广播等功能。
 * 注：以上功能使用同一个Discovery实现类实例
 *
 * Discovery接口需实现：
 * 1. 获取节点类型的配置数据（未解析）
 * 2. 监听节点类型的配置更新
 * 3. 监听兄弟节点（同类型节点）
 * 4. 监听朋友节点（不同类型的协作节点）
 *
 * 机制1：
 * 当使用如Spring之类的Bean容器来实例化Discovery的实现类时，可能系统中的其余
 * 部分的初始化需要大量工作而它们很可能在Discovery之后实例化。在这种情形下系统
 * 还不能处理任何请求，而Discovery向zookeeper的出席消息却已经发布。使朋友（协作）
 * 节点把它当做已经正常服务的节点而向它发布数据。解决方案是让Discovery延迟出席。
 * 如在Spring的ApplicationListener<ApplicationReadyEvent>监听器中发布节点
 * 出席消息。出席后，Node对象才会生成。
 *
 * 机制2：
 * 若系统的功能模块依赖于Node，当机制1生效时，其他功能模块在初始化时不能得到Node
 * 进而不能处理某些请求（如向协作节点发送的数据需要本节点信息的时候）。解决方案是
 * 将功能模块的除依赖Node的初始化先完成，然后向Discovery注册一个出席监听器，在监
 * 听器中设置关于Node的信息。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/17.
 */
public interface Discovery {

    /**
     * 获取本节点
     * @return 代表本节点的Node对象
     */
    Node getNode();

    /**
     * 添加监听的节点类型
     * @param nodeType 要监听的节点类型
     */
    void addWatchNode(String nodeType);

    /**
     * 获取本节点的配置
     * 若有环境属性，则getConfig方法仅获取本节点所在的环境的配置。
     * @return 节点参数配置对象
     */
    Config getConfig();

    /**
     * 节点配置改变通知方法。
     * zookeeper上的节点配置改变时调用。
     * @param data 配置数据
     */
    void onConfigUpdated(String data);

    /**
     * 获取本节点同类型的节点。
     * @return 兄弟节点列表
     */
    List<Node> getBrothers();

    /**
     * 获取本节点的朋友（协作）的节点。
     * 朋友节点可能包含多种类型
     * @return 朋友节点列表
     */
    List<Node> getFriends();


    /**
     * 节点出席消息，发送出席消息表示节点已经可以处理请求或数据。
     */
    void present();
}
