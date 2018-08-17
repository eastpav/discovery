package org.eastpav.discovery.config;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 对象属性拷贝工具.
 *
 * @author Yao Zhang
 *
 * Created on 2018/1/25.
 */
public class ObjectUtils {
    /**
     * 将src对象的属性值拷贝到dst对象的对应属性中
     * @param src 源对象
     * @param dst 目标对象
     * @throws Exception 对象不匹配
     */
    public static void copyFields(Object src, Object dst) throws Exception {
        copyFields(src, dst, null);
    }

    /**
     * 将src对象的属性值拷贝到dst对象的对应属性中,出现在include中的field才会拷贝
     * @param src 源对象
     * @param dst 目标对象
     * @param include 需要拷贝的field列表，若列表为空表示拷贝所有field
     * @throws Exception 对象不匹配
     */
    public static void copyFields(Object src, Object dst, List<String> include) throws Exception {
        if (src == null || dst == null) {
            throw new Exception((src == null ? "src" : "dst") + "must not be null");
        }

        if(src.getClass() != dst.getClass()) {
            throw new Exception("src and dst must be the same class type");
        }

        Field[] fields = src.getClass().getDeclaredFields();
        for(Field field : fields) {

            if(include != null) {
                if(!include.contains(field.getName())) {
                    continue;
                }
            }

            field.setAccessible(true);
            field.set(dst, field.get(src));
            field.setAccessible(false);
        }
    }
}
