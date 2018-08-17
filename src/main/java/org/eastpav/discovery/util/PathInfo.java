package org.eastpav.discovery.util;

import lombok.Getter;
import lombok.Setter;

/**
 * 节点path信息.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/18.
 */
public class PathInfo {
    @Getter @Setter private String environment;
    @Getter @Setter private String nodeType;
    @Getter @Setter private boolean configPath;
    @Getter @Setter private String fullName;
    @Getter @Setter private String data;

    @Override
    public String toString() {
        return environment + ":" + nodeType + " is a " + (configPath ? "Config" : "Node");
    }
}
