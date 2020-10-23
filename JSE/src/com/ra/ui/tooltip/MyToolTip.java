package com.ra.ui.tooltip;

import com.ra.ui.R;
import sun.font.FontDesignMetrics;

import javax.swing.*;
import java.awt.*;

public class MyToolTip extends JToolTip {
    String text="";
    public MyToolTip(){
        super();
        setOpaque(false);
        setBackground(new Color(0,0,0));
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm=FontDesignMetrics.getMetrics(R.F);
        return new Dimension(fm.stringWidth(text)+20,30);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0,0,0));
        g.fillRect(0,0,getWidth(),getHeight());
        g.setFont(R.F);
        g.setColor(Color.WHITE);
        g.drawString(text,10,22);
    }

    @Override
    public void setTipText(String tipText) {
        super.setTipText(tipText);
        text=tipText;
    }
}
