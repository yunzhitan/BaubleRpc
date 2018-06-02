package top.yunzhitan.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class JSON {

    public static final String CLASS_KEY = "@type";

    /**
     * 对象转为json字符串
     *
     * @param object 对象
     * @return json字符串
     */
    public static String toJSONString(Object object) {
        return JSONSerializer.serialize(object);
    }

    /**
     * 序列化json基本类型（自定义对象需要先转换成Map）
     *
     * @param object  需要序列化的对象
     * @param addType 是否增加自定义对象标记
     * @return Json格式字符串
     */
    public static String toJSONString(Object object, boolean addType) {
        return JSONSerializer.serialize(object, addType);
    }

    /**
     * 解析为指定对象
     *
     * @param text  json字符串
     * @param clazz 指定类
     * @param <T>   指定对象
     * @return 指定对象
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        Object obj = JSONSerializer.deserialize(text);
        return BeanSerializer.deserializeByType(obj, clazz);
    }

    /**
     * 获取需要序列化的字段，跳过
     *
     * @param targetClass 目标类
     * @return Field list
     */
    protected static List<Field> getSerializeFields(Class targetClass) {
        List<Field> all = new ArrayList<Field>();
        for (Class<?> c = targetClass; c != Object.class && c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();

            for (Field f : fields) {
                int mod = f.getModifiers();
                // transient, static,  @JSONIgnore : skip
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
                    continue;
                }
                JSONIgnore ignore = f.getAnnotation(JSONIgnore.class);
                if (ignore != null) {
                    continue;
                }

                f.setAccessible(true);
                all.add(f);
            }
        }
        return all;
    }
}

