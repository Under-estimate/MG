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
            for(String res:R.resources.keySet())
                rg.data.put(res,data.get(res).intValue());
            return rg;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public ResourceGroup add(ResourceGroup rg){
        return add(rg,false);
    }
    public ResourceGroup sub(ResourceGroup rg){
        return sub(rg,false);
    }
    public ResourceGroup negate(){
        return negate(false);
    }
    public ResourceGroup add(ResourceGroup rg,boolean inline){
        if(inline){
            for(String res:R.resources.keySet())
                data.put(res,data.get(res)+rg.data.get(res));
            return this;
        }
        ResourceGroup resource=(ResourceGroup) clone();
        for(String res:R.resources.keySet())
            resource.data.put(res,data.get(res)+rg.data.get(res));
        return resource;
    }

    public ResourceGroup sub(ResourceGroup rg,boolean inline){
        if(inline){
            for(String res:R.resources.keySet())
                data.put(res,data.get(res)-rg.data.get(res));
            return this;
        }
        ResourceGroup resource=(ResourceGroup) clone();
        for(String res:R.resources.keySet())
            resource.data.put(res,data.get(res)-rg.data.get(res));
        return resource;
    }

    public ResourceGroup negate(boolean inline){
        if(inline){
            for(String res:R.resources.keySet())
                data.put(res,-data.get(res));
            return this;
        }
        ResourceGroup resource=(ResourceGroup) clone();
        for(String res:R.resources.keySet())
            resource.data.put(res,-data.get(res));
        return resource;
    }
    /**
     * @param rg the resource group to be subtracted.
     * @return true if this resource group can successfully subtract the given one without negative values.
     * */
    public boolean compare(ResourceGroup rg){
        for(String res:R.resources.keySet())
            if(data.get(res)<rg.data.get(res))
                return false;
        return true;
    }
    public boolean isEmpty(){
        for(String res:R.resources.keySet())
            if(data.get(res)!=0)
                return false;
        return true;
    }
}
