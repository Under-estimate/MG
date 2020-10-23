package com.ra.data;

import com.ra.ui.R;

import java.util.HashMap;

public class ResourceGroup implements Cloneable{
    public HashMap<String,Integer> data;
    public ResourceGroup(){
        data=new HashMap<>();
        for(String s:R.resources.keySet())
            data.put(s,0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone(){
        try {
            ResourceGroup rg = (ResourceGroup) super.clone();
            rg.data=(HashMap<String, Integer>) data.clone();
            return rg;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
