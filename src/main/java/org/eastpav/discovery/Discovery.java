package org.eastpav.discovery;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * 发现服务接口
 *
 * 发现服务定义与zookeeper(curator)交互的接口。其实现类及其子类在这些接口的
 * 基础上可以构建出节点注册、配置管理、命名服务、服务发现、集群消息广播、选举
 * 等功能。
 * 注：以上功能使用同一个Discovery实现类实例
 *
 * Discovery接口需实现：
 * 1、 主动获取指定path的数据
 * 2、 增加监听path，不需要其数据
 * 3、 增加监听path，需要其数据
 * 4、 创建path（节点），不设置数据
 * 5、 创建path（节点），设置数据
 * 6、 更新path（节点数据）
 * 7、 删除path（节点）
 *
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
     * 获取指定节点的数据
     * @param path 指定的节点路径
     * @return 代表本节点的Node对象
     */
    String getPathData(String path);

    /**
     * 获取指定节点的状态
     * @param path 节点路径
     * @return 节点状态
     */
    Stat getPathStat(String path);

    /**
     * 获取指定节点的子节点列表
     * @param parentPath 子节点的父节点
     * @return 子节点列表
     */
    List<String> getChildren(String parentPath);

    /**
     * 添加监听路径，在该路径下的子节点事件将被监听到
     * @param parentPath 要监听的节点父路径
     * @param cacheListener 子节点监听器
     * @return 子节点监听句柄
     */
    PathChildrenCache addChildrenWatcher(String parentPath, PathChildrenCacheListener cacheListener);

    /**
     * 添加监听节点路径，该路径节点的事件将被监听到
     * @param path 要监听的节点路径
     * @param cacheListener 节点数据监听器
     * @return 节点监听句柄
     */
    NodeCache addNodeWatcher(String path, NodeCacheListener cacheListener);

    /**
     * 创建子节点，并设置数据
     * @param path 在该path下创建子节点。
     * @param durable 是否是持久化节点。
     * @param data 写入节点路径的数据。
     * @param force 数据强制写入。若节点已存在时（持久化节点，如配置节点）,可能需要强制写入。
     * @return 节点路径。
     */
    String createPath(String path, boolean durable, boolean sequential, String data, boolean force);

    /**
     * 更新节点的数据
     * @param path 节点路径
     * @param data 更新的数据
     * @return true - 成功， false - 失败
     */
    boolean updatePathData(String path, String data);

    /**
     * 删除节点。
     * @param path 要删除的节点路径
     */
    void deletePath(String path);

    /**
     * 检查节点是否存在
     * @param path 节点路径
     * @return true - 存在， false - 不存在
     */
    boolean pathAvailable(String path);
}
