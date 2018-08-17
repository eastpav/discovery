package org.eastpav.discovery.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置对象的属性是全局的。
 * 这里的全局是相对于整个集群而言。每种节点公共的参数在配置对象中的属性用该注解标识。
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/29.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalItem {
}
