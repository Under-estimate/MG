package com.ra.ui.component;

import com.ra.data.RealTimeData;
import com.ra.data.Structure;
import com.ra.ui.GamePane;
import com.ra.ui.R;
import com.ra.ui.tooltip.BuildingDetailTip;
import com.ra.ui.tooltip.MyToolTip;
import com.ra.ui.tooltip.StructureDetailTip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class BuildingOperation extends JPanel {
    private int mouse=-1;
    public int level=1;
    private final BufferedImage bg= R.getImageResource("Images/structure_option.png");
    public BuildingOperation(){
        super();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int option=calcMousePos(e.getPoint());
                GamePane src=R.M.getContent(GamePane.class);
                if(option==0) {
                    src.callDestroy();
                    R.sound.playButton();
                } else if(option==1&&src.building==null&&calcUpgradeAvailability()) {
                    src.callUpgrade();
                    R.sound.playButton();
                }else if(option==2) {
                    src.callShowResistOption();
                    R.sound.playButton();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouse=-1;
                setToolTipText(null);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouse=calcMousePos(e.getPoint());
                if(mouse==0)
                    setToolTipText("移除此建筑物。");
                else if(mouse==1){
                    GamePane src=R.M.getContent(GamePane.class);
                    RealTimeData target=src.info[src.optionX][src.optionY];
                    setToolTipText(target.structure.name);
                    if(target.level>=2) {
                        setToolTipText(null);
                        return;
                    }
                    level = target.level+1;
                    System.out.println(level);
                }
                else if(mouse==2)
                    setToolTipText("做好抗灾准备。");
                else
                    setToolTipText(null);
            }
        });
    }

    @Override
    public JToolTip createToolTip() {
        if(mouse==1) {
            StructureDetailTip tip=new StructureDetailTip();
            tip.bo=this;
            return tip;
        }
        return new MyToolTip();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        GamePane src=R.M.getContent(GamePane.class);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(bg,0,0,getWidth(),getHeight(),this);
        g2.setStroke(new BasicStroke(getWidth() / 4.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
        boolean upgrade=calcUpgradeAvailability()&&src.building==null;
        if(mouse>=0&&!(mouse==1&!upgrade)) {
            g2.setColor(new Color(255, 255, 255, 100));
            g2.drawArc(getWidth()/8,getWidth()/8,getWidth()*3/4,getHeight()*3/4,mouse*60,60);
        }
        if(!upgrade){
            g2.setColor(new Color(0,0,0,100));
            g2.drawArc(getWidth()/8,getWidth()/8,getWidth()*3/4,getHeight()*3/4,60,60);
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
        return deg/60;
    }
    private boolean calcUpgradeAvailability(){
        GamePane src=R.M.getContent(GamePane.class);
        RealTimeData target=src.info[src.optionX][src.optionY];
        if(target.level>=2)
            return false;
        if(!target.structure.unlock.get(target.level+1))
            return false;
        return src.resource.data.compare(target.structure.getRG(target.level+1, Structure.BUILD));
    }
}
