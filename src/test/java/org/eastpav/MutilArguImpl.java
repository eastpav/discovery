package org.eastpav;

/**
 * 类实现的描述.
 *
 * @author Yao Zhang
 *         <p>
 *         Created on 2018/7/17.
 */
public class MutilArguImpl implements MutilArgu {
    @Override
    public void present() {
        presentWithNodeData(null);
    }

    @Override
    public void presentWithNodeData(String nodeData, String... topic) {
        if(topic == null) {
            System.out.println("topic null");
        } else {
            System.out.println(topic.getClass().getCanonicalName());
        }
    }
}
