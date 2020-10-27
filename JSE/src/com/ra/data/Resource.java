package com.ra.data;

import java.awt.image.BufferedImage;

public class Resource {
    public final String name;
    public final String desc;
    public BufferedImage image;
    public boolean instant=false;
    public Resource(String name,String desc){
        this.name=name;
        this.desc=desc;
    }
}
