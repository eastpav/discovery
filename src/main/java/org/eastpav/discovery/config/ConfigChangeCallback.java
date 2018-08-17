package org.eastpav.discovery.config;

/**
 * 节点配置改变回调.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/27.
 */
public interface ConfigChangeCallback {
    /**
     * 当节点配置改变时，由节点Config的实现调用
     */
    void configChanged();
}
