package org.eastpav.discovery;

import lombok.Getter;
import lombok.Setter;

/**
 * 服务节点类.
 * 节点表示一个具体的server实例。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/17.
 */

public class Node {
    /**
     * 节点所运行的环境
     * 节点运行环境应该为以下几种之一：
     * 1. 生产环境 product
     * 2. 测试环境 test
     * 3. 开发环境 develop
     * 表示环境的字符串可自定义，需实现者设置。若不设置则为null
     * 该项并不会影响程序的运行。
     */
    @Setter @Getter private String environment;
    @Setter @Getter private String nodeType;
    @Setter @Getter private String nodeName;
}
