package com.ra.ui.component;

import com.ra.ui.GamePane;
import com.ra.ui.R;
import com.ra.ui.tooltip.MyToolTip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class BuildingOperation extends JPanel {
    private int mouse=-1;
    private final BufferedImage bg= R.getImageResource("Images/structure_option.png");
    public BuildingOperation(){
        super();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int option=calcMousePos(e.getPoint());
                if(option==0)
                    R.M.getContent(GamePane.class).callDestroy();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouse=-1;
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouse=calcMousePos(e.getPoint());
                if(mouse==0)
                    setToolTipText("移除此建筑物。");
                else if(mouse==1)
                    setToolTipText("指定建筑物进行抗灾操作。（还没做好QAQ）");
            }
        });
    }

    @Override
    public JToolTip createToolTip() {
        return new MyToolTip();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(bg,0,0,getWidth(),getHeight(),this);
        g2.setStroke(new BasicStroke(getWidth() / 4.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
        if(mouse>=0) {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.drawArc(getWidth()/8,getWidth()/8,getWidth()*3/4,getHeight()*3/4,mouse*90,90);
        }
    }
    private int calcMousePos(Point p){
        p.y-=getHeight()/2;
        p.x-=getWidth()/2;
        p.y=-p.y;
        double r=Math.hypot(p.x,p.y);
        if(r<getWidth()/4f||r>getWidth()/2f)
            return -1;
        double angle=Math.acos(p.x/r);
        if(p.y<0)
            angle=2*Math.PI-angle;
        int deg=(int)Math.toDegrees(angle);
        if(deg>180)
            return -1;
        return deg/90;
    }
}
