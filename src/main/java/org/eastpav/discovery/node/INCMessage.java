package org.eastpav.discovery.node;

import lombok.Getter;
import lombok.Setter;

/**
 * 节点点消息类.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/22.
 */
public class INCMessage {
    @Getter @Setter private String peerType;
    @Getter @Setter private Object routeKey;
    @Getter @Setter private byte[] data;
}
