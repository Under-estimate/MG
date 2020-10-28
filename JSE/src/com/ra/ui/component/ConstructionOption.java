package com.ra.ui.component;

import com.ra.data.ResourceGroup;
import com.ra.data.Structure;
import com.ra.ui.GamePane;
import com.ra.ui.R;
import com.ra.ui.tooltip.StructureDetailTip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ConstructionOption extends JPanel {
    private int mouse=-1;
    private final BufferedImage bg= R.getImageResource("Images/construction_option.png");
    private final String[] correspondent={"矿场","农田","科研中心","数据中心","工厂","能源工厂","民居"};
    public ConstructionOption(){
        super();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(R.M.getContent(GamePane.class).building!=null)
                    return;
                int option=calcMousePos(e.getPoint());
                boolean[] availability=calcAvailability();
                if(option>=0&&availability[option]) {
                    R.M.getContent(GamePane.class).callBuild(correspondent[option]);
                    R.sound.playButton();
                }
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
                if(mouse>=0)
                    setToolTipText(R.structures.get(correspondent[mouse]).name);
            }
        });
    }

    @Override
    public JToolTip createToolTip() {
        return new StructureDetailTip();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(bg,0,0,getWidth(),getHeight(),this);
        g2.setStroke(new BasicStroke(getWidth() / 4.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
        if(R.M.getContent(GamePane.class).building!=null) {
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawArc(getWidth() / 8, getWidth() / 8, getWidth() * 3 / 4, getHeight() * 3 / 4, 0, 360);
        }else {
            boolean[] availability = calcAvailability();
            g2.setColor(new Color(0, 0, 0, 100));
            for (int i = 0; i < correspondent.length; i++)
                if (!availability[i])
                    g2.drawArc(getWidth() / 8, getWidth() / 8, getWidth() * 3 / 4, getHeight() * 3 / 4, (i*360-90)/7, 51);
            if(mouse>=0&&availability[mouse]) {
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawArc(getWidth()/8,getWidth()/8,getWidth()*3/4,getHeight()*3/4,(mouse*360-90)/7,51);
            }
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
        double deg=Math.toDegrees(angle);
        return (int)(deg/(360.0/7.0));
    }
    private boolean[] calcAvailability(){
        boolean[] availability=new boolean[correspondent.length];
        Arrays.fill(availability,true);
        for(int i=0;i<correspondent.length;i++){
            ResourceGroup comparator=R.structures.get(correspondent[i]).getRG(1,Structure.BUILD);
            ResourceGroup current=R.M.getContent(GamePane.class).resource.data;
            for(String st:R.resources.keySet()){
                if(current.data.get(st)<comparator.data.get(st))
                    availability[i]=false;
            }
            if(!R.structures.get(correspondent[i]).unlock.get(1))
                availability[i]=false;
        }
        return availability;
    }
}
