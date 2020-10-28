package com.ra.data;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Structure implements Cloneable{
    public String name="";
    private HashMap<String,ResourceGroup> resources=new HashMap<>();
    public HashMap<Integer,BufferedImage> images=new HashMap<>();
    public HashMap<Integer,Integer> times=new HashMap<>();
    public HashMap<Integer,Boolean> unlock=new HashMap<>();
    public String desc="";
    public static final String BUILD="build",CONSUME="consume",PRODUCE="produce";
    public Structure(){
        resources.put(BUILD+1,new ResourceGroup());
        resources.put(CONSUME+1,new ResourceGroup());
        resources.put(PRODUCE+1,new ResourceGroup());
        resources.put(BUILD+2,new ResourceGroup());
        resources.put(CONSUME+2,new ResourceGroup());
        resources.put(PRODUCE+2,new ResourceGroup());
        unlock.put(1,false);
        unlock.put(2,false);
        unlock.put(3,false);
    }
    public Structure(String name,String desc){
        this.name=name;
        this.desc=desc;
        resources.put(BUILD+1,new ResourceGroup());
        resources.put(CONSUME+1,new ResourceGroup());
        resources.put(PRODUCE+1,new ResourceGroup());
        resources.put(BUILD+2,new ResourceGroup());
        resources.put(CONSUME+2,new ResourceGroup());
        resources.put(PRODUCE+2,new ResourceGroup());
        unlock.put(1,false);
        unlock.put(2,false);
        unlock.put(3,false);
    }
    @SuppressWarnings("unchecked")
    public Structure clone(){
        try {
            Structure s = (Structure) super.clone();
            s.name=name;
            s.desc=desc;
            s.times=(HashMap<Integer, Integer>) times.clone();
            s.resources=new HashMap<>();
            s.unlock=(HashMap<Integer, Boolean>) unlock.clone();
            for(int i=1;i<=2;i++) {
                s.resources.put(Structure.BUILD + i, (ResourceGroup) resources.get(Structure.BUILD+ i).clone());
                s.resources.put(Structure.PRODUCE + i, (ResourceGroup) resources.get(Structure.PRODUCE+ i).clone());
                s.resources.put(Structure.CONSUME + i, (ResourceGroup) resources.get(Structure.CONSUME+ i).clone());
            }
            return s;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public void copyRG(Structure copy){
        for(int i=1;i<=2;i++) {
            resources.put(Structure.BUILD + i, (ResourceGroup) copy.getRG(i,Structure.BUILD).clone());
            resources.put(Structure.PRODUCE + i, (ResourceGroup) copy.getRG(i,Structure.PRODUCE).clone());
            resources.put(Structure.CONSUME + i, (ResourceGroup) copy.getRG(i,Structure.CONSUME).clone());
        }
    }
    public ResourceGroup getRG(int level,String type){
        return resources.get(type+level);
    }
}
