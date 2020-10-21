package com.ra.ui;

import java.awt.*;
import java.util.HashMap;

/**
 * 自定义的布局管理器，以实现在不同大小的窗口中的自适应。
 * 每个组件需要8个参数来定义其大小和位置。
 * 例如在定义组件的x坐标时，需要用到anchorX和offsetX两个参数，
 * 当父容器的宽度为W时，组件的x坐标为(anchorX*W+offsetX)。
 * 通过传入LayoutParam或LayoutParam实现指定参数。
 * @author Jingsen Zhou
 * @see LayoutParam
 * @see LayoutParamClass
 * */
public class ConstraintLayout implements LayoutManager2 {
    public HashMap<Component, LayoutParamClass> layoutInfo=new HashMap<>();
    /**
     * 添加组件并给定布局参数。
     * @param constraints 应为LayoutParam类型或LayoutParamClass类型。
     * */
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if(constraints instanceof LayoutParam) {
            LayoutParam param=(LayoutParam)constraints;
            LayoutParamClass clazz=new LayoutParamClass(
                    param.offsetX(),param.anchorX(),
                    param.offsetY(),param.anchorY(),
                    param.fixedWidth(),param.widthRate(),
                    param.fixedHeight(),param.heightRate());
            layoutInfo.put(comp,clazz);
        }else if(constraints instanceof LayoutParamClass){
            layoutInfo.put(comp,(LayoutParamClass)constraints);
        }else{
            layoutInfo.put(comp,new LayoutParamClass());
            throw new IllegalArgumentException("Incompatible constraint type:" + constraints.getClass().getName());
        }
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    @Override
    public void invalidateLayout(Container target) {

    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        addLayoutComponent(comp,new LayoutParamClass());
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        layoutInfo.remove(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        int xMax=0,yMax=0;
        for(Component key:layoutInfo.keySet()){
            LayoutParamClass param=layoutInfo.get(key);
            xMax=Math.max(xMax,param.offsetX+param.fixedWidth);
            yMax=Math.max(yMax,param.offsetY+param.fixedHeight);
        }
        return new Dimension(xMax,yMax);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        for(Component c:layoutInfo.keySet()){
            LayoutParamClass param=layoutInfo.get(c);
            int w=parent.getWidth();
            int h=parent.getHeight();
            c.setBounds((int)(w*param.anchorX+param.offsetX),
                    (int)(h*param.anchorY+param.offsetY),
                    (int)(w*param.widthRate+param.fixedWidth),
                    (int)(h*param.heightRate+param.fixedHeight));
        }
    }
    public static class LayoutParamClass{
        double anchorX=0,anchorY=0,widthRate=0,heightRate=0;
        int offsetX=0,offsetY=0,fixedWidth=0,fixedHeight=0;
        public LayoutParamClass(){

        }
        public LayoutParamClass(int offsetX,int offsetY,int fixedWidth,int fixedHeight){
            this.offsetX=offsetX;
            this.offsetY=offsetY;
            this.fixedWidth=fixedWidth;
            this.fixedHeight=fixedHeight;
        }
        public LayoutParamClass(double anchorX,double anchorY,double widthRate,double heightRate){
            this.anchorX=anchorX;
            this.anchorY=anchorY;
            this.widthRate=widthRate;
            this.heightRate=heightRate;
        }
        public LayoutParamClass(int offsetX,double anchorX,int offsetY,double anchorY,
                                int fixedWidth,double widthRate,int fixedHeight,double heightRate){
            this(offsetX, offsetY, fixedWidth, fixedHeight);
            this.anchorX=anchorX;
            this.anchorY=anchorY;
            this.widthRate=widthRate;
            this.heightRate=heightRate;
        }
    }
}
