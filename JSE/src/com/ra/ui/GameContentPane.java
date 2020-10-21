package com.ra.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 所有游戏界面的父类，新定义游戏界面时应继承此类。
 * 用例如下：
 * <pre>
 * &#64;GameContent
 * public class Menu extends GameContentPane{
 *      &#64;LayoutParam(offsetX=10, offsetY=10, widthRate=0.1, heightRate=0.1)
 *      private JButton button=new JButton("Test");
 *      public Menu(){
 *          super();
 *          initLayout();
 *      }
 * }
 * </pre>
 * 以上代码定义了一个游戏界面，该界面会被自动添加到主窗体可切换的界面列表中，
 * 变量JButton将会按照指定的布局参数添加到该界面中。
 * @author Jingsen Zhou
 * @see LayoutParam
 * @see GameContent
 * @see #initLayout()
 * */
public class GameContentPane extends JPanel {
    private final java.util.Map<String,ParameterizedImage> imageRenderList= Collections.synchronizedMap(new HashMap<>());
    protected GameContentPane(){
        super();
        setLayout(new ConstraintLayout());
    }
    /**
     * 反射子类中所有带有LayoutParam的变量，并添加到布局中。
     * 子类应当在初始化完成所有带有LayoutParam的变量之后调用此方法。
     * @see LayoutParam
     * */
    protected void initLayout(){
        Field[] f=getClass().getDeclaredFields();
        try {
            for (Field tmp : f) {
                if (tmp.isAnnotationPresent(LayoutParam.class)) {
                    add((Component)tmp.get(this),tmp.getAnnotation(LayoutParam.class));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected synchronized void addImage(String name,ParameterizedImage image){
        imageRenderList.put(name,image);
    }
    protected synchronized ConstraintLayout.LayoutParamClass getImageParameter(String name){
        return imageRenderList.get(name).param;
    }
    protected synchronized void updateImageParameter(String name, ConstraintLayout.LayoutParamClass param){
        ParameterizedImage im=imageRenderList.get(name);
        im.param=param;
        imageRenderList.put(name,im);
    }
    protected synchronized void removeImage(String name){
        imageRenderList.remove(name);
    }
    @Override
    public synchronized void paint(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        ArrayList<ParameterizedImage> images=new ArrayList<>(imageRenderList.values());
        images.sort(Comparator.comparing(ParameterizedImage::getLayer));
        for (ParameterizedImage im:images) {
            g.drawImage(im.image,
                    (int) (im.param.anchorX * getWidth() + im.param.offsetX),
                    (int) (im.param.anchorY * getHeight() + im.param.offsetY),
                    (int) (im.param.widthRate * getWidth() + im.param.fixedWidth),
                    (int) (im.param.heightRate * getHeight() + im.param.fixedHeight), this);
        }
    }
    /**
     * 带有坐标参数的图像。
     * @see GameContentPane#imageRenderList
     * */
    public static class ParameterizedImage{
        public BufferedImage image;
        public ConstraintLayout.LayoutParamClass param;
        public int layer;
        public ParameterizedImage(String resource,int layer,ConstraintLayout.LayoutParamClass param){
            this.image=R.getImageResource(resource);
            this.layer=layer;
            this.param=param;
        }
        public int getLayer(){
            return layer;
        }
    }
}
