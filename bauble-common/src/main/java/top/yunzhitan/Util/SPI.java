package top.yunzhitan.Util;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {

    String name() default "";
    int property() default 0;
}
