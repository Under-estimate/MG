package com.ra.ui.component;

import com.ra.data.ResourceGroup;
import com.ra.data.Technology;
import com.ra.ui.GamePane;
import com.ra.ui.R;

import javax.swing.*;
import java.awt.*;

public class TechDetailTip extends JToolTip {
    String tech;
    public TechDetailTip(){
        super();
        setOpaque(false);
        setBackground(new Color(0,0,0,100));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }

    @Override
    public void setTipText(String tipText) {
        super.setTipText(tipText);
        tech=tipText;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0,0,0,100));
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setFont(R.F);
        Technology target= R.technologies.get(tech);
        int labCount=R.M.getContent(GamePane.class).labCount;
        if(target.acquired){
            g2.setColor(Color.GREEN);
            g2.drawString("已获得！",0,25);
        }else if(labCount<=0){
            g2.setColor(Color.RED);
            g2.drawString("缺少科研中心",0,25);
        }else {
            g2.setColor(Color.WHITE);
            g2.drawString("预计所需时间：" + target.time / labCount, 0, 25);
        }
        g2.setColor(Color.WHITE);
        g2.drawString("对于\""+target.modifier+"\"",0,55);
        paintValidData(target.consume,g2,0,"每秒消耗");
        paintValidData(target.produce,g2,getWidth()/2,"每秒产出");
    }
    private void paintValidData(ResourceGroup data, Graphics2D g, int xOffset, String title){
        g.drawString(title,xOffset,80);
        int i=2;
        for(String s:R.resources.keySet()){
            if(data.data.get(s)==0)
                continue;
            g.drawImage(R.resources.get(s).image,(int)(xOffset+getWidth()*0.05),i*40+5,40,40,this);
            String str=(data.data.get(s)>0?"+":"")+data.data.get(s);
            g.drawString(str,(int)(xOffset+getWidth()*0.2),i*40+30);
            i++;
        }
    }
}