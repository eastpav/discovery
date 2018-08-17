package org.eastpav.discovery.config;

import java.util.Properties;

/**
 * 配置解析器接口.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/21.
 */
public interface ConfigResolve {

    /**
     * 从data字符串解析出配置存放到JSON对象中
     * @param data 配置字符串
     * @return 转换后的JSON对象
     */
    Properties resolve(String data) throws Exception;
}
