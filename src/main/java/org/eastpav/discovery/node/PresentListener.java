package org.eastpav.discovery.node;

/**
 * 节点出席监听器.
 * 当节点是延迟出席时，可从该监听器中获得节点出席事件。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/19.
 */
public interface PresentListener {

    /**
     * 节点出席事件回调
     * @param node 发生出席事件的节点
     */
    void onPresentEvent(Node node);
}