package org.eastpav.discovery;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.eastpav.discovery.config.Config;
import org.eastpav.discovery.config.ClientConfigImpl;
import org.eastpav.discovery.config.NodeConfig;
import org.eastpav.discovery.config.ServerConfigImpl;
import org.eastpav.discovery.node.Node;
import org.eastpav.discovery.node.NodeImpl;

/**
 * 发现者.
 * 发现者提供Node创建、Config获取监听、命名、服务发现、广播、选举方法。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/18.
 */
public class Discoverer extends BaseDiscovery {

    public Node newNode(String environment, String nodeType, boolean autoPresent, boolean enableGlobalConfig, Object configBean) {
        Node node = new NodeImpl(this, environment, nodeType, configBean, enableGlobalConfig);

        node.getConfig();
        if(enableGlobalConfig) {
            node.getGlobalConfig();
        }

        if(autoPresent) {
            node.present();
        }

        return node;
    }


    public String present(String path, String nodeData) {
        return createNodePath(path, false, nodeData);
    }


    public Config createServerConfig(String configPath, String initialConf, boolean force) {
        configPath = createConfigPath(configPath, true, initialConf, force);
        return new ServerConfigImpl(configPath, initialConf, this);
    }

//    public void addNodeWatcher(String parentPath, String nodeType,  Node node) {
//        //addWatcher(nodeParentPath, nodeType, handler);
//        PathChildrenCache childrenCache = addChildrenWatcher(parentPath, node);
//        node.addCache(nodeType, childrenCache);
//    }

//    public String addConfigWatcher(String configPath, String nodeType, NodeConfig config) {
//        NodeCache nodeCache = addNodeWatcher(configPath, config);
//        config.addCache(nodeType, nodeCache);
//
//        return configPath;
//    }

    private String createNodePath(String path, boolean durable, String nodeData) {
        return createPath(path, durable, true, nodeData, false);
    }

    private String createConfigPath(String path, boolean durable, String initialConf, boolean force) {
        return createPath(path, durable, false, initialConf, force);
    }
}
