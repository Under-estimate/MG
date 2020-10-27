package com.ra.ui.tooltip;

import com.ra.data.ResourceGroup;
import com.ra.data.Structure;
import com.ra.ui.R;

import javax.swing.*;
import java.awt.*;

public class ResistDetailTip extends JToolTip {
    int level;
    public ResistDetailTip(){
        super();
        setOpaque(false);
        setBackground(new Color(0,0,0));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(150,100);
    }

    @Override
    public void setTipText(String tipText) {
        super.setTipText(tipText);
        level=Integer.parseInt(tipText);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0,0,0));
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setFont(R.F);
        if(level<0){
            g2.setColor(Color.RED);
            g2.drawString("未解锁",50,60);
        }else {
            g2.setColor(Color.WHITE);
            g2.drawString("提升对灾难的抗性", 0, 55);
            Structure target=R.structures.get("灾难预防");
            paintValidData(target.getRG(level+1,Structure.BUILD), g2, 0, "消耗资源");
        }
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