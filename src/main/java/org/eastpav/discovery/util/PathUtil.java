package org.eastpav.discovery.util;

/**
 * 工具集.
 *
 * 节点path的拓扑结构如下：
 * environment ---- {serverType1} -------- config
 *               |                 |
 *               |                 ------- nodes ----- {serverType1}-0000001
 *               |                                 |
 *               |                                 --- {serverType1}-0000002
 *               |                                 |
 *               |                                 --- {serverType1}-0000003
 *               |
 *               --- serverType2
 * config是持久化的无序号节点
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/17.
 */
public class PathUtil {

    /**
     * 根据服务类型名生成节点的path
     * @param environment 运行环境，系统中可以区分不同环境的字符串
     * @param serverType 服务类型，系统中可以区分不同服务或模块的字符串
     * @return 节点path
     */
    public static String makeNodePath(String environment, String serverType) {
        return "/" + environment + "/" + serverType + "/nodes/" + serverType + "-";
    }

    public static String makeNodeListenPath(String environment, String serverType) {
        return "/" + environment + "/" + serverType + "/nodes";
    }

    public static String makeNodeFullPath(String environment, String serverType, String nodeName) {
        return "/" + environment + "/" + serverType + "/nodes/" + nodeName;
    }

    /**
     * 根据服务类型生成节点配置path
     * @param environment 运行环境，系统中可以区分不同环境的字符串
     * @return 节点配置path
     */
    public static String makeConfigPath(String environment) {
        return "/" + environment + "/config";
    }

    /**
     * 根据服务类型生成节点配置path
     * @param environment 运行环境，系统中可以区分不同环境的字符串
     * @param serverType 服务类型，系统中可以区分不同服务或模块的字符串
     * @return 节点配置path
     */
    public static String makeConfigPath(String environment, String serverType) {
        return "/" + environment + "/" + serverType + "/config";
    }

    /**
     * 根据服务类型、环境生产节点类型名
     * @param environment 运行环境
     * @param serverType 服务类型
     * @return 节点类型名
     */
    public static String makeNodeTypeName(String environment, String serverType) {
        return "/" + environment + "/" + serverType;
    }

    /**
     * 从path中解析出serverType
     * @param path 节点路径
     * @return PathInfo对象（从路径中解析的PathInfo对象data属性为null）
     */
    public static PathInfo getPathInfo(String path) {
        return new PathInfo();
    }

    // /test/deviceManager/xxx
    public static String getServerType(String nodePath) {
        int idx0 = nodePath.indexOf("/", 0);
        if(idx0 == -1) {
            return null;
        }

        idx0 = nodePath.indexOf("/", idx0+1);
        if(idx0 == -1) {
            return null;
        }

        int idx1 = nodePath.indexOf("/", idx0+1);
        if(idx1 == -1) {
            return null;
        }

        return nodePath.substring(idx0+1, idx1);
    }

    public static String getSimpleName(String nodePath) {
        String[] segments = nodePath.split("/");
        if(segments.length != 4 && segments.length != 5) {
            return null;
        }

        if(segments.length == 4) {
            return segments[3];
        } else {
            return segments[4];
        }
    }

    public static PathInfo parsePathInfo(String nodePath, byte[] data) {
        // /test/deviceManager/config
        // /test/deviceManager/nodes/deviceManager-00000001

        String[] segments = nodePath.split("/");
        if(segments.length != 4 && segments.length != 5) {
            return null;
        }

        PathInfo pathInfo = new PathInfo();
        pathInfo.setEnvironment(segments[1]);
        pathInfo.setNodeType(segments[2]);
        pathInfo.setFullName(nodePath);
        pathInfo.setData(data == null ? null : new String(data));
        pathInfo.setConfigPath(segments.length == 4);

        return pathInfo;
    }

    public static String getNodeConfigPath(String nodePath) {
        String[] segments = nodePath.split("/");
        if(segments.length != 4 && segments.length != 5) {
            return null;
        }

        return segments[0] + "/" + segments[1] + "/" + segments[2] + "/config";
    }
}
