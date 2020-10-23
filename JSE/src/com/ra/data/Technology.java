package com.ra.data;

public class Technology {
    public String name;
    public String[] requirements;
    public int time;
    public String modifier;
    public ResourceGroup consume,produce;

    public int column=0,row=0;

    public boolean acquired=false;
    public boolean satisfied=false;
    public Technology(String name,String modifier,int time){
        this.name=name;
        this.modifier=modifier;
        this.time=time;
        consume=new ResourceGroup();
        produce=new ResourceGroup();
    }
}
