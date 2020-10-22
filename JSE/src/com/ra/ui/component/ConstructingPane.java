package com.ra.ui.component;

import com.ra.ui.R;
import sun.font.FontDesignMetrics;

import javax.swing.*;
import java.awt.*;

public class ConstructingPane extends JPanel {
    public double progress=0;
    public ConstructingPane(){
        super();
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.white);
        g2.setFont(R.F);
        FontMetrics fm= FontDesignMetrics.getMetrics(R.F);
        int width=fm.stringWidth("建造中..");
        g2.drawString("建造中...",(getWidth()-width)/2,26);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Color.GREEN);
        g2.fillRect(1,getHeight()/2,(int)((getWidth()-2)*progress),10);
        g2.setColor(Color.WHITE);
        g2.drawRect(1,getHeight()/2,getWidth()-2,10);
    }
}
