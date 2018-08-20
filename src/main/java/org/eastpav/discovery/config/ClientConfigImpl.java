package org.eastpav.discovery.config;


import org.apache.curator.framework.recipes.cache.NodeCache;
import org.eastpav.discovery.Discoverer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * 节点配置对象（客户端）。
 * 配置以键值对存于configBean对象中。
 *
 * ClientConfigImpl中的配置是从zookeeper的配置节点的信息解析而来。
 * 当zookeeper中的配置改变时，ClientConfigImpl会相应地更新，对于
 * 依赖ClientConfigImpl配置的其他模块来说这是透明的。以此实现多节点配置
 * 参数的同步修改。
 *
 * 注意：该类是服务节点的配置，只能被动接受配置的修改，而不能修改配置。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/17.
 */
public class ClientConfigImpl implements NodeConfig {
    private Logger log = LoggerFactory.getLogger(getClass());


    private ConfigResolve resolver;
    private Discoverer discoverer;
    private String configName;
    private boolean autoMode;
    private NodeCache nodeCache;

    /**
     * 指示该配置是否为全局配置。即包含还是排除注解@GlobalItem
     * 的配置对象属性。
     * injectAnnotated为true，只注入含有注解@GlobalItem的属性
     * injectAnnotated为false，只注入不含有注解@GlobalItem的属性
     */
    private boolean injectAnnotated;

    private ConfigChangeCallback changeCallback;

    /**
     * configBean表示一个具体的配置类对象，该类由调用者确定
     * 该对象的属性是具体需求决定的。ConfigImpl对象根据侦听
     * 到的新配置通过反射重建该对象，并完成属性值注入。然后
     * 替换掉旧对象。
     *
     * 注意：任何在运行时要使用该对象中的属性需要从ConfigImpl
     * 获得而不是使用其引用备份。
     */
    private Object configBean;

    /**
     * 配置是否已取回
     */
    private boolean retrieved;

    public ClientConfigImpl(String configName, Object configObj, Discoverer discoverer, boolean injectAnnotated) {
        this.configName = configName;
        this.autoMode = true;
        this.discoverer = discoverer;
        this.configBean = configObj;
        this.retrieved = false;
        this.injectAnnotated = injectAnnotated;
    }

    //***************************Config接口*************************************

    @Override
    public void addCache(String nodeType, NodeCache cache) {
        nodeCache = cache;
    }

    @Override
    public void setAuto(boolean auto) {
        autoMode = auto;
    }

    /**
     * 获取配置对象(有用户指定的configBean对象)
     * @return configBean对象
     */
    @Override
    public Object getConfig() {
        return configBean;
    }

    @Override
    public void setConfigChangeCallback(ConfigChangeCallback callback) {
        changeCallback = callback;
    }

    private void configChange(String configString) {
        if(resolver == null) {
            resolver = ResolverDetector.detect(configString);
        }
        try {
            // 获取监听到的新配置，解析为Properties对象
            Properties properties = resolver.resolve(configString);

            ConfigUtils.parseConfig(configBean, properties, injectAnnotated);
            retrieved = true;
        } catch (Exception e) {
            log.error("resolve", e);
        }
    }



    //***************************NodeCacheListener接口*************************************

    @Override
    public void nodeChanged() throws Exception {
        String conf;
        byte[] data = nodeCache.getCurrentData().getData();
        if(data == null) {
            conf = "";
        } else {
            conf = new String(data);
        }

        log.debug("Config changed {}:{}", nodeCache.getCurrentData().getPath(), conf);
        if(autoMode) {
            configChange(conf);
            retrieved = true;
            //System.out.println(this.hashCode());
            if(changeCallback != null) {
                changeCallback.configChanged();
            }
        }
    }

    //***************************Object*************************************

    @Override
    public String toString() {
        return configName;
    }
}
