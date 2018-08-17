package org.eastpav.discovery.config;

/**
 * 配置不支持的操作异常.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/21.
 */
public class ConfigNotSupportedException extends Exception {
    public ConfigNotSupportedException(String msg) {
        super(msg);
    }

    public ConfigNotSupportedException(Throwable e) {
        super(e);
    }
}
