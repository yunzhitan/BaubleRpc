package top.yunzhitan.rpc.service;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Service {

    /**
     * 服务组别
     * @return
     */
    String group();

    /**
     * 服务名称
     * @return
     */
    String name() default "";

}
