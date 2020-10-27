package com.ra.data;

import com.ra.ui.GamePane;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class RealTimeData {
    public boolean lack=false;
    public int level=1;
    public Structure structure=null;
    public HashMap<String,Integer> resistance=new HashMap<>();
    public RealTimeData(){
        resistance.put(GamePane.DROUGHT,0);
        resistance.put(GamePane.FREEZE,0);
        resistance.put(GamePane.EARTHQUAKE,0);
    }
    public BufferedImage getImage(){
        return structure.images.get(level);
    }
    public ResourceGroup getRG(String type){
        return structure.getRG(level,type);
    }
}
