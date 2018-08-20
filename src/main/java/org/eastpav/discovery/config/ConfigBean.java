package org.eastpav.discovery.config;

/**
 * @描述: 节点配置bean。全局配置是由注解@GlobalItem修饰的bean属性
 * @作者: 张尧
 * @创建时间: 2018-08-20 10:28:39
 */
public interface ConfigBean {
    /**
     * 转换为节点全局配置字符串
     * @return 配置字符串
     */
    String getGlobalConfigString();

    /**
     * 转换为全局配置除外的配置字符串
     * @return 配置字符串
     */
    String getConfigString();
}
