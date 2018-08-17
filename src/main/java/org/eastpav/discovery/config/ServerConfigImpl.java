package org.eastpav.discovery.config;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.eastpav.discovery.Discoverer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 节点配置对象（设置端）。
 *
 * 该类是ClientConfigImpl对应的服务设置端。
 * 设置端修改zookeeper配置path的数据，出发ClientConfigImpl节点更新其配置。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/27.
 */
public class ServerConfigImpl implements Config {
    private Logger log = LoggerFactory.getLogger(getClass());
    private Discoverer discoverer;
    private String configName;
    private String configInfo;

    public ServerConfigImpl(String configName, String initConfig, Discoverer discoverer) {
        this.discoverer = discoverer;
        this.configName = configName;
        configInfo = initConfig;
    }

    @Override
    public void addCache(String nodeType, NodeCache cache) {
        // no use
    }

    @Override
    public void setAuto(boolean auto) {
        // no use
    }

    /**
     * 获取配置路径的数据
     * @return 数据的字符串形式
     */
    @Override
    public Object getConfig() {
        return discoverer.getPathData(configName);
    }

    @Override
    public void update(String data) throws ConfigNotSupportedException {
        discoverer.updatePathData(configName, data);
    }

    @Override
    public void nodeChanged() throws Exception {
        // no use
    }
}
