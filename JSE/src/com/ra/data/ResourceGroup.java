package com.ra.data;

import com.ra.ui.R;

import java.util.HashMap;

public class ResourceGroup {
    public HashMap<String,Integer> data;
    public ResourceGroup(){
        data=new HashMap<>();
        for(String s:R.resources.keySet())
            data.put(s,0);
    }
}
