package org.eastpav.discovery.config;


import org.eastpav.discovery.annotation.GlobalItem;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 配置对象解析.
 * 从Properties属性解析出对应的配置对象
 *
 * @author Yao Zhang
 *
 *  Created on 2018/1/25.
 */

public class ConfigUtils {

    @SuppressWarnings({"unchecked"})
    public static void parseConfig(Object configBean, Properties properties, boolean injectAnnotated) throws Exception {

        Class clazz = configBean.getClass();
        /*
         * 暂存更新的配置的中间对象，避免参数错误导致部分配置更新。
         * 只有当配置解析完全成功后，才更新节点的配置对象（configBean）
         */
        Object ghost = clazz.newInstance();

        ArrayList<String> availableFields = new ArrayList<>(properties.size());

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {

            /*
             * 只有当isAnnotationPresent和injectAnnotated同为true或同为false时，才注入。
             * 当他们值不同时，意味着：
             * 1、field包含@GlobalItem注解，但现在是注入非注解属性，则该属性不注入。
             * 2、field不包含@GlobalItem注解，但现在是注入@GlobalItem属性，则该属性不注入。
             */
            if(field.isAnnotationPresent(GlobalItem.class) != injectAnnotated) {
                continue;
            }

            String line = properties.getProperty(field.getName());
            if(line == null) {
                // 该field没有配置
                continue;
            }

            availableFields.add(field.getName());

            Class type = field.getType();
            field.setAccessible(true);

            if(type.isArray()) {
                String[] arrays = line.split(",");
                Class componentType = type.getComponentType();
                Object objArr = Array.newInstance(componentType, arrays.length);
                int i = 0;
                for (String array : arrays) {
                    Object o = convertTo(componentType, array);
                    Array.set(objArr, i++, o);
                }

                field.set(ghost, objArr);

            } else if(type == List.class) {

                /*
                 * 获取类成员的泛型类型
                 * 对于List，仅有一个泛型类型
                 */
                Class[] c = getParameterActualType(field);
                if(c.length != 1) {
                    throw new IllegalArgumentException("List must be only one generic Type");
                }

                Collection list = createCollection(List.class, c[0], 16);

                String[] props = line.split(";");

                for(String prop : props) {
                    // 根据List的泛型类型转换数据
                    Object o = convertTo(c[0], prop);
                    list.add(o);
                }

                field.set(ghost, list);
            } else if(type == Map.class) {

                /*
                 * 获取类成员的泛型类型
                 * 对于Map，有两个泛型类型，第一个为Key的类型，第二个为value的类型
                 */
                Class[] c = getParameterActualType(field);
                if(c.length != 2) {
                    throw new IllegalArgumentException("Map must be have two generic Type");
                }

                Map map = createMap(Map.class, c[0], 16);

                String[] kvs = line.split(";");

                for(String kv : kvs) {
                    String[] items = kv.split("-");
                    if(items.length != 2) {
                        throw new IllegalArgumentException("Map k-v must be split by \'-\'");
                    }

                    map.put(convertTo(c[0], items[0]), convertTo(c[1], items[1]));
                }

                field.set(ghost, map);
            } else {
                /*
                 * 设置基本类型及其包装类属性和枚举属性
                 */
                field.set(ghost, convertTo(type, line));
            }
        }

        //return ghost;

        /*
         * 将出现的中间配置对象的field值拷贝到节点配置对象中
         */
        ObjectUtils.copyFields(ghost, configBean, availableFields);
    }

    @SuppressWarnings({"unchecked", "cast"})
    private static Object convertTo(Class c, String value) {
        if(c == Integer.class || c == int.class) {
            return Integer.parseInt(value);
        } else if(c == Long.class || c == long.class) {
            return Long.parseLong(value);
        } else if(c == Double.class || c == double.class) {
            return Double.parseDouble(value);
        } else if(c == Float.class || c == float.class) {
            return Float.parseFloat(value);
        } else if(c == Boolean.class || c == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if(c == String.class) {
            return value;
        } else if(c.isEnum()) {
            return Enum.valueOf(c, value);
        } else {
            throw new IllegalArgumentException("Unsupported Type");
        }
    }

    public static Class<?>[] getParameterActualType(Field field) throws ClassNotFoundException {
        Type t = field.getGenericType();

        if(ParameterizedType.class.isAssignableFrom(t.getClass())) {
            Type[] t1 = ((ParameterizedType) t).getActualTypeArguments();
            if (t1.length == 0) {
                throw new IllegalArgumentException("Must have generic type");
            }

            Class<?>[] cc = new Class[t1.length];
            for(int i = 0; i < t1.length; i++) {
                cc[i] = Class.forName(t1[i].getTypeName());
            }

            return cc;
        } else {
            throw new IllegalArgumentException("Error field");
        }
    }

    @SuppressWarnings({ "unchecked", "cast" })
    public static <K, V> Map<K, V> createMap(Class<?> mapType, Class<?> keyType, int capacity) {
        if(mapType == null) {
            throw new IllegalArgumentException("Map type must be not null");
        }

        if (mapType.isInterface()) {
            if (Map.class == mapType) {
                return new LinkedHashMap<K, V>(capacity);
            }
            else if (SortedMap.class == mapType || NavigableMap.class == mapType) {
                return new TreeMap<K, V>();
            }
            else {
                throw new IllegalArgumentException("Unsupported Map interface: " + mapType.getName());
            }
        }
        else {
            if (!Map.class.isAssignableFrom(mapType)) {
                throw new IllegalArgumentException("Unsupported Map type: " + mapType.getName());
            }
            try {
                return (Map<K, V>) mapType.newInstance();
            }
            catch (Throwable ex) {
                throw new IllegalArgumentException("Could not instantiate Map type: " + mapType.getName(), ex);
            }
        }
    }


    @SuppressWarnings({ "unchecked", "cast" })
    public static <E> Collection<E> createCollection(Class<?> collectionType, Class<?> elementType, int capacity) {
        if(collectionType == null) {
            throw new IllegalArgumentException("Collection type must not be null");
        }
        if (collectionType.isInterface()) {
            if (Set.class == collectionType || Collection.class == collectionType) {
                return new LinkedHashSet<E>(capacity);
            }
            else if (List.class == collectionType) {
                return new ArrayList<E>(capacity);
            }
            else if (SortedSet.class == collectionType || NavigableSet.class == collectionType) {
                return new TreeSet<E>();
            }
            else {
                throw new IllegalArgumentException("Unsupported Collection interface: " + collectionType.getName());
            }
        }
        else {
            if (!Collection.class.isAssignableFrom(collectionType)) {
                throw new IllegalArgumentException("Unsupported Collection type: " + collectionType.getName());
            }
            try {
                return (Collection<E>) collectionType.newInstance();
            }
            catch (Throwable ex) {
                throw new IllegalArgumentException(
                        "Could not instantiate Collection type: " + collectionType.getName(), ex);
            }
        }
    }
}
