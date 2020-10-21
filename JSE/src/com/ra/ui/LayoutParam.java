package com.ra.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为即将添加到ConstraintLayout的组件设定布局参数。
 * @see ConstraintLayout
 *
 * @author Jingsen Zhou
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface LayoutParam {
    double anchorX() default 0;
    double anchorY() default 0;
    double widthRate() default 0;
    double heightRate() default 0;
    int offsetX() default 0;
    int offsetY() default 0;
    int fixedWidth() default 0;
    int fixedHeight() default 0;
}
