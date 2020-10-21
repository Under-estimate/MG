package com.ra.ui;

import java.lang.annotation.*;

/**
 * 用于继承了JPanel的类，表明这个类是一个游戏界面。
 * @author Jingsen Zhou
 * @see MainFrame#getContent(Class)
 * @see MainFrame#switchToContent(Class)
 * */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface GameContent {

}
