package org.eastpav.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Discovery的抽象实现类。
 * 实现基本的与zookeeper的交互。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/17.
 */
public class BaseDiscovery implements Discovery {

    private static final int DEFAULT_RETRY_INTERVAL = 3000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;
    private static final int DEFAULT_SESSION_TIMEOUT = 3000;
    private static final int MAX_CREATE_NUMBER = 64;

    private Logger log = LoggerFactory.getLogger(getClass());
    private CuratorFramework client;
    private List<Object> cacheList;

    public void init(String url) {
        init(url, DEFAULT_RETRY_INTERVAL, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SESSION_TIMEOUT);
    }

    public void init(String url, int retryInterval, int connectionTimeout, int sessionTimeout) {
        cacheList = new ArrayList<>();

        client = CuratorFrameworkFactory.newClient(url, sessionTimeout, connectionTimeout,
                new RetryForever(retryInterval));
        client.start();

        //client.getConnectionStateListenable().addListener();

        try {
            //client.blockUntilConnected();
            boolean connected = client.blockUntilConnected(10, TimeUnit.SECONDS);
            if(!connected) {
                throw new RuntimeException("can not connect to " + url);
            }

        } catch (InterruptedException e) {
            log.error("Failed to connect to zookeeper {}", e.getMessage(), e);
            CloseableUtils.closeQuietly(client);
            throw new RuntimeException(e);
        }
    }

    public void close() {
        cacheList.forEach(cache -> {
            try {
                if(cache instanceof PathChildrenCache) {
                    ((PathChildrenCache)cache).close();
                } else {
                    ((NodeCache)cache).close();
                }

            } catch (IOException e) {
                log.error("close", e);
            }
        });

        cacheList.clear();

        client.close();
    }

    @Override
    public String getPathData(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            if(stat != null) {
                return new String(client.getData().forPath(path));
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("getPathData", e);
        }

        return null;
    }

    @Override
    public Stat getPathStat(String path) {
        try {
            return client.checkExists().forPath(path);
        } catch (Exception e) {
            log.error("getPathStat", e);
        }

        return null;
    }

    @Override
    public List<String> getChildren(String parentPath) {
        try {
            return client.getChildren().forPath(parentPath);
        } catch (Exception e) {
            log.error("getChildren", e);
        }

        return new ArrayList<>();
    }

    @Override
    public PathChildrenCache addChildrenWatcher(String parentPath, PathChildrenCacheListener cacheListener) {
        try {
            log.info("ChildrenWatcher:{}", parentPath);
            PathChildrenCache cache = new PathChildrenCache(client, parentPath, true);
            cache.getListenable().addListener(cacheListener);
            cache.start();
            cacheList.add(cache);
            return cache;
        } catch (Exception e) {
            log.error("addWatcher", e);
        }

        return null;
    }

    @Override
    public NodeCache addNodeWatcher(String path, NodeCacheListener cacheListener, String defaultConfig) {
        try {
            log.info("NodeWatcher:{}", path);
            NodeCache cache = new NodeCache(client, path);
            cache.getListenable().addListener(cacheListener);
            cache.start();

            if(cache.getCurrentData() == null) {
                // 节点配置path不存在，则创建该path并使用节点配置默认值设置path数据
                createPath(path, true, false, defaultConfig, false);
            } else if(cache.getCurrentData().getData() == null) {
                //节点配置path存在，但无数据，则使用节点配置默认值设置path数据
                updatePathData(path, defaultConfig);
            } else {
                log.info("path:{}", cache.getCurrentData().getPath());
            }

            cacheList.add(cache);
            return cache;
        } catch (Exception e) {
            log.error("addWatcher", e);
        }

        return null;
    }

    @Override
    public String createPath(String path, boolean durable, boolean sequential, String data, boolean force) {
        try {
            CreateMode mode;
            if(durable) {
                if(sequential) {
                    mode = CreateMode.PERSISTENT_SEQUENTIAL;
                } else {
                    mode = CreateMode.PERSISTENT;
                }
            } else {
                if(sequential) {
                    mode = CreateMode.EPHEMERAL_SEQUENTIAL;
                } else {
                    mode = CreateMode.EPHEMERAL;
                }
            }

            return client.create()
                    .creatingParentsIfNeeded()
                    .withMode(mode)
                    .forPath(path, data != null ? data.getBytes() : null);
        } catch (Exception e) {
            if(e instanceof KeeperException.NodeExistsException) {
                if(data != null && force) {
                    updatePathData(path, data);
                }
                return path;
            } else {
                log.error("createPath", e);
            }
        }

        return null;
    }

    @Override
    public boolean updatePathData(String path, String data) {
        try {
            client.setData().forPath(path, data.getBytes());
            return true;
        } catch (Exception e) {
            log.error("updatePathData", e);
        }

        return false;
    }

    @Override
    public void deletePath(String path) {
        try {
            client.delete().forPath(path);
        } catch (Exception e) {
            log.error("deletePath", e);
        }
    }

    @Override
    public boolean pathAvailable(String path) {
        try {
            if(client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("pathAvailable", e);
        }

        return false;
    }
}
