package org.eastpav.discovery.node;

/**
 * 路由规则.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/22.
 */
public enum RouteRule {
    /**
     * 使用字符串作为一致性hash路由的键
     */
    BY_STRING,

    /**
     * 使用整数作为一致性hash路由的键
     */
    BY_INTEGER
}
