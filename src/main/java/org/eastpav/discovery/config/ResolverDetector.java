package org.eastpav.discovery.config;

/**
 * 配置解析器类型探测.
 * 根据配置字符串的格式选择并示例化匹配的配置解析器
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/22.
 */
public class ResolverDetector {

    public static ConfigResolve detect(String data) {
        if(data.startsWith("{")) {
            return new JsonConfigResolver();
        } else {
            return new PropConfigResolver();
        }
    }

}
