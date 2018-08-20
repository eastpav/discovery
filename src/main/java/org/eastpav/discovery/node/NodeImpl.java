package org.eastpav.discovery.node;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.eastpav.discovery.Discoverer;
import org.eastpav.discovery.config.Config;
import org.eastpav.discovery.config.ClientConfigImpl;
import org.eastpav.discovery.config.ConfigBean;
import org.eastpav.discovery.config.NodeConfig;
import org.eastpav.discovery.mq.EMqClient;
import org.eastpav.discovery.mq.MessageListener;
import org.eastpav.discovery.mq.MqClient;
import org.eastpav.discovery.util.PathInfo;
import org.eastpav.discovery.util.PathUtil;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 服务节点类.
 * 节点表示一个具体的server实例。
 *
 * 注意：当使用如Spring之类的Bean容器来实例化Node时，如果系统中的其余
 * 部分的初始化需要大量工作而它们很可能在Node之后实例化。若Node已向
 * zookeeper的发布出席消息，使朋友（协作）节点把它当做已经正常服务的节
 * 点而向它发布数据。在这种情形下系统将不能正确处理数据。
 * 解决方案是让Node延迟出席。使用Discoverer.newNode(false)创建Node,
 * 在之后合适的时间执行Node.present()出席。
 * 如在Spring的ApplicationListener<ApplicationReadyEvent>监听器中发布节点
 * 出席消息。
 *
 * 注意：使用Discoverer.newNode(false)创建的Node对象是没有确定的节点名的。
 * 仅仅有一个"noName"表示。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/17.
 */

public class NodeImpl implements Node {
    private Logger log = LoggerFactory.getLogger(getClass());
    /**
     * 节点所运行的环境
     * 节点运行环境应该为以下几种之一：
     * 1. 生产环境 product
     * 2. 测试环境 test
     * 3. 开发环境 develop
     * 表示环境的字符串可自定义，需实现者设置。若不设置则为null
     * 该项并不会影响程序的运行。
     */

    @Setter private String environment;
    @Setter private String nodeType;
    @Setter @Getter private String nodeName;
    @Getter private String nodeTypeName;
    @Setter private String simpleName;
    @Setter @Getter private Discoverer discoverer;
    @Setter private boolean presented;
    @Setter @Getter private boolean enableGlobalConfig;

    /**
     * 配置对象，节点的配置参数对象
     * 它可以是实现ConfigBean接口的java bean或java对象，它仅包含配置基本数据对象。
     */
    private ConfigBean configBean;

    private List<PresentListener> listeners;
    private List<ServerNodeListener> nodesListeners;
    private Map<String, PathChildrenCache> cacheMap;
    private Map<String, NodeRouter> routerMap;

    private boolean enableInc;
    private INC inc;
    private String host;
    private int port;
    //private INCClient incClient;
    private MqClient incClient;
    private MessageListener messageListener;
    private ExecutorService sendService;

    /**
     * 与当前节点关联的配置
     */
    private NodeConfig config;

    /**
     * 与当前节点关联的集群全局配置
     */
    private NodeConfig globalConfig;

    public NodeImpl(Discoverer discoverer, String environment, String nodeType) {
        this(discoverer, environment, nodeType, null, false);
    }

    public NodeImpl(Discoverer discoverer, String environment, String nodeType, ConfigBean configBean, boolean enableGlobalConfig) {
        this.environment = environment;
        this.nodeName = "noName";
        this.simpleName = "noName";
        this.nodeTypeName = PathUtil.makeNodeTypeName(environment, nodeType);
        this.nodeType = nodeType;
        this.discoverer = discoverer;
        this.configBean = configBean;
        presented = false;
        listeners = new ArrayList<>();
        nodesListeners = new ArrayList<>();
        cacheMap = new HashMap<>();
        routerMap = new HashMap<>();
        enableInc = true;
        inc = INC.EMQ;
        host = "localhost";
        port = 1883;
        this.enableGlobalConfig = enableGlobalConfig;

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(nodeType + "-%d").build();
        sendService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(10240),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    //********************************Node接口***********************************************

    @Override
    public void addCache(String nodeType, PathChildrenCache childrenCache) {
        cacheMap.put(nodeType, childrenCache);
    }

    @Override
    public void addNodeListener(ServerNodeListener listener) {
        nodesListeners.add(listener);
    }

    @Override
    public void addPresentListener(PresentListener listener) {
        listeners.add(listener);
    }

    @Override
    public void addRouteRule(String nodeType, RouteRule routeRule) {

    }

    @Override
    public void addMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void enableInc(boolean enable) {
        this.enableInc = enable;
    }

    @Override
    public NodeConfig getConfig() {
        if(config == null) {
            if(configBean == null) {
                throw new IllegalArgumentException("configBean must be not null.");
            }
            return getConfig(configBean);
        }

        return config;
    }

    @Override
    public NodeConfig getConfig(ConfigBean configBean) {
        if(configBean == null) {
            throw new IllegalArgumentException("configBean must be not null.");
        }

        if(config == null) {
            String configPath = PathUtil.makeConfigPath(environment, nodeType);
            config = new ClientConfigImpl(configPath, configBean, discoverer, false);
            NodeCache nodeCache = discoverer.addNodeWatcher(configPath, config, configBean.getConfigString());
            config.addCache(nodeType, nodeCache);
        }

        return config;
    }

    @Override
    public NodeConfig getGlobalConfig() {
        if(enableGlobalConfig) {
            if (globalConfig == null) {
                return getGlobalConfig(configBean);
            }
        }

        return globalConfig;
    }

    @Override
    public NodeConfig getGlobalConfig(ConfigBean configBean) {
        if(enableGlobalConfig) {
            if (globalConfig == null) {
                String globalConfigPath = PathUtil.makeConfigPath(environment);
                globalConfig = new ClientConfigImpl(globalConfigPath, configBean, discoverer, true);
                NodeCache nodeCache = discoverer.addNodeWatcher(globalConfigPath, globalConfig, configBean.getGlobalConfigString());
                globalConfig.addCache(nodeType, nodeCache);
            }
        }

        return globalConfig;
    }

    @Override
    public String getEnvironment() {
        return environment;
    }

    @Override
    public String getName() {
        return nodeName;
    }

    @Override
    public String getNodeType() {
        return nodeType;
    }

    @Override
    public String getNodeTypeName() {
        return nodeTypeName;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public List<PathInfo> getWatched(String serverType) {
        PathChildrenCache cache = cacheMap.get(serverType);
        if(cache != null) {
            return cache.getCurrentData()
                    .stream()
                    .filter(childData -> !childData.getPath().equals(nodeName))
                    .map(cd -> PathUtil.parsePathInfo(cd.getPath(), cd.getData()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public List<PathInfo> getWatched() {
        ArrayList<PathInfo> list = new ArrayList<>();
        cacheMap.forEach((type, cache) -> list.addAll(cache.getCurrentData()
                .stream()
                .filter(cd -> !cd.getPath().equals(nodeName))
                .map(cd -> PathUtil.parsePathInfo(cd.getPath(), cd.getData()))
                .collect(Collectors.toList())));

        return list;
    }

    @Override
    public boolean isPresented() {
        return presented;
    }

    @Override
    public boolean nodeAvailable(String nodeName) {
        return discoverer.pathAvailable(nodeName);
    }

    @Override
    public void leave() {
        if(presented) {
            discoverer.deletePath(nodeName);
            presented = false;
        }
    }

    @Override
    public void present() {
        presentWithNodeData(null);
    }

    @Override
    public void present(String... topic) {
        presentWithNodeData(null, topic);
    }

    @Override
    public void presentWithNodeData(String nodeData, String... topics) {
        if(presented) {
            log.debug("Node {} already presented.", nodeName);
            return;
        }

        nodeName = discoverer.present(PathUtil.makeNodePath(environment, nodeType), nodeData);
        presented = true;
        simpleName = PathUtil.getSimpleName(nodeName);
        // 出席后本节点获得唯一节点名，使用节点名创建INC通信客户端
        if (enableInc && incClient == null) {
            try {
                incClient = createIncClient(nodeName, topics);
            } catch (Exception e) {
                log.error("createIncClient", e);
            }
        }

        // 通知节点出席事件(出席后，节点才有全局唯一的节点名)
        listeners.forEach(listener -> listener.onPresentEvent(this));
    }

    @Override
    public void represent() {
        if(!presented) {
            discoverer.createPath(nodeName, false, false, null, false);
            presented = true;
        }
    }

    @Override
    public void selectINC(INC inc, String host, int port) {
        this.inc = inc;
        this.host = host;
        this.port = port;
    }

    @Override
    public void watchNode(String serverType, RouteRule routeRule) {
        if(routerMap.containsKey(serverType)) {
            return;
        }

        String parentPath = PathUtil.makeNodeListenPath(environment, serverType);

        PathChildrenCache childrenCache = discoverer.addChildrenWatcher(parentPath, this);
        this.addCache(nodeType, childrenCache);

        // 新建节点路由
        NodeRouter nodeRouter = new NodeRouter(serverType, routeRule);
        addNodeListener(nodeRouter);
        routerMap.put(serverType, nodeRouter);
    }


    @Override
    public String toString() {
        return environment + ":" + nodeName + ":" + presented;
    }



    private MqClient createIncClient(String clientId, String... topics) throws Exception {
        MqClient client;
        if(inc == INC.EMQ) {
            //client = new EMQINCClientImpl("tcp://"+host+":"+port, clientId, messageProcessor);
            client = new EMqClient(host, port, clientId);
            client.setMessageListener(messageListener);
            // nodeName作为节点间通信的topic
            client.addTopic(clientId);
            for(String topic: topics) {
                client.addTopic(topic);
            }

            client.doConnect();
        } else {
            client = null;
        }

        return client;
    }

    @Override
    public boolean send(String nodeType, Object key, byte[] data) {
        //1、根据message的peerType找到对应的路由
        NodeRouter nodeRouter = routerMap.get(nodeType);
        if(nodeRouter != null) {
            //2、根据事先设置的路由规则，根据message的routeKey查找目标节点
            Optional<PathInfo> op = nodeRouter.getNode(key);
            //3、向目标节点发送消息。
            //op.ifPresent(pathInfo -> incClient.send(pathInfo.getFullName(), message.getData()));
            if(op.isPresent()) {
                return sendTo(op.get().getFullName(), data);
            } else {
                log.warn("No available {} node for process data", nodeType);
            }
        } else {
            log.warn("Message for {} node not supported", nodeType);
        }

        return false;
    }

    @Override
    public boolean send(String nodeName, byte[] data) {
        return sendTo(nodeName, data);
    }

    @Override
    public boolean subscribe(String topic) {
        return incClient.subscribe(topic);
    }

    private boolean sendTo(String nodeName, byte[] data) {
        log.trace("sendTo:{}", nodeName);
        sendService.execute(() -> incClient.send(nodeName, data));
        return true;
    }

    //********************************EventHandler接口***********************************************

    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
        log.trace("event:{}", event.getType());
        PathChildrenCacheEvent.Type type = event.getType();
        // 与zookeeper断连、重连时，ChildData为null。不处理。
        if(type == PathChildrenCacheEvent.Type.CONNECTION_SUSPENDED ||
                type == PathChildrenCacheEvent.Type.CONNECTION_LOST ||
                type == PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED) {
            return;
        }

        ChildData childData = event.getData();
        PathInfo pathInfo = PathUtil.parsePathInfo(childData.getPath(), childData.getData());
        if(pathInfo == null) {
            return;
        }

        if(pathInfo.isConfigPath()) {
            return;
        }

        switch (type) {
            case CHILD_ADDED:
                log.info("[{}] {} add", nodeName, pathInfo.getFullName());
                nodesListeners.forEach(listener -> listener.onNodeAdded(pathInfo));
                break;
            case CHILD_UPDATED:
                log.info("[{}] {} update", nodeName, pathInfo.getFullName());
                nodesListeners.forEach(listener -> listener.onNodeUpdated(pathInfo));
                break;
            case CHILD_REMOVED:
                log.info("[{}] {} remove", nodeName, pathInfo.getFullName());
                nodesListeners.forEach(listener -> listener.onNodeRemoved(pathInfo));
                break;
            default:
                break;
        }
    }
}
