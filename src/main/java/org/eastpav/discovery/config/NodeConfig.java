package org.eastpav.discovery.config;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

/**
 * 服务节点的配置接口.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/27.
 */
public interface NodeConfig extends NodeCacheListener {
    void addCache(String nodeType, NodeCache cache);
    /**
     * 配置自动应用更新模式。
     * 自动：当配置更新时，自动覆盖Config的旧配置。
     * 不自动：当配置更新时，依旧使用旧配置而不更新。
     *
     * @param auto 自动模式
     */
    void setAuto(boolean auto);

    /**
     * 获取配置对象
     * @return 配置对象
     */
    Object getConfig();

    /**
     * 设置节点配置改变回调
     * @param callback 回调
     */
    void setConfigChangeCallback(ConfigChangeCallback callback);
}
