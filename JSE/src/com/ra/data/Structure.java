package com.ra.data;

import java.awt.image.BufferedImage;

public class Structure implements Cloneable{
    public final String name;
    public ResourceGroup build,consume,produce;
    public final String desc;
    public final int time;
    public BufferedImage image;
    public Structure(String name,String desc,int time){
        this.name=name;
        this.desc=desc;
        this.time=time;
        build=new ResourceGroup();
        consume=new ResourceGroup();
        produce=new ResourceGroup();
    }
}
