package top.yunzhitan.rpc;

import top.yunzhitan.common.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceImpl {

    /**
     * 服务版本
     */
    String version() default Constants.DEFAULT_VERSION;

}

