package com.ra.ui.component;

import com.ra.data.ResourceGroup;
import com.ra.data.Structure;
import com.ra.data.Technology;
import com.ra.ui.GamePane;
import com.ra.ui.R;

import javax.swing.*;
import java.awt.*;

public class BuildingDetailTip extends JToolTip {
    String structure;
    public BuildingDetailTip(){
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
        structure=tipText;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0,0,0,100));
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setFont(R.F);
        Structure target=R.structures.get(structure);
        g2.setColor(Color.CYAN);
        g2.drawString(target.name,0,20);
        g2.setColor(Color.WHITE);
        paintValidData(target.consume,g2,0,"每秒消耗");
        paintValidData(target.produce,g2,getWidth()/2,"每秒产出");
    }
    private void paintValidData(ResourceGroup data, Graphics2D g, int xOffset, String title){
        g.drawString(title,xOffset,50);
        int i=1;
        for(String s:R.resources.keySet()){
            if(data.data.get(s)==0)
                continue;
            g.drawImage(R.resources.get(s).image,(int)(xOffset+getWidth()*0.05),i*40+15,40,40,this);
            g.drawString(Integer.toString(data.data.get(s)),(int)(xOffset+getWidth()*0.2),i*40+40);
            i++;
        }
    }
}