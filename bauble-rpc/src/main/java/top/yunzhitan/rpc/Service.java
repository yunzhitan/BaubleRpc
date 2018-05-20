package top.yunzhitan.rpc;

import top.yunzhitan.common.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

    /**
     * 服务组别
     */
    String group() default Constants.DEFAULT_GROUP;

    /**
     * 服务名称
     */
    String name() default "";
}
