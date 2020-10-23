package com.ra.data;

import java.awt.image.BufferedImage;

public class Structure implements Cloneable{
    public String name;
    public ResourceGroup build,consume,produce;
    public String desc;
    public int time;
    public BufferedImage image;
    public Structure(){

    }
    public Structure(String name,String desc,int time){
        this.name=name;
        this.desc=desc;
        this.time=time;
        build=new ResourceGroup();
        consume=new ResourceGroup();
        produce=new ResourceGroup();
    }
    public Structure clone(){
        try {
            Structure s = (Structure) super.clone();
            s.name=name;
            s.desc=desc;
            s.time=time;
            s.build = build;
            s.consume = consume;
            s.produce = produce;
            s.image = image;
            return s;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
