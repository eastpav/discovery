package org.eastpav.discovery.node;

import org.eastpav.discovery.util.PathInfo;

/**
 * 节点事件监听器.
 * 监听感兴趣节点的上线、更新和下线事件
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/19.
 */
public interface ServerNodeListener {
    /**
     * 节点上线事件
     * @param pathInfo 上线的节点信息
     */
    void onNodeAdded(PathInfo pathInfo);

    /**
     * 节点更新事件
     * @param pathInfo 更新的节点信息
     */
    void onNodeUpdated(PathInfo pathInfo);

    /**
     * 节点离线事件
     * @param pathInfo 离线的节点信息
     */
    void onNodeRemoved(PathInfo pathInfo);
}
