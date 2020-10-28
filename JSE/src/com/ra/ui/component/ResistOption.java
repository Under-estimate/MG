package com.ra.ui.component;

import com.ra.data.RealTimeData;
import com.ra.data.Structure;
import com.ra.ui.GamePane;
import com.ra.ui.R;
import com.ra.ui.tooltip.ResistDetailTip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ResistOption extends JPanel {
    private int mouse=-1;
    private final BufferedImage bg= R.getImageResource("Images/resistance_option.png");
    private String[] coordinate={GamePane.FREEZE,GamePane.EARTHQUAKE,GamePane.DROUGHT};
    public final HashMap<String,Boolean> unlock=new HashMap<>();
    public ResistOption(){
        super();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mouse=calcMousePos(e.getPoint());
                GamePane src=R.M.getContent(GamePane.class);
                if(mouse>=0){
                    boolean[] available=calcAvailability();
                    if(available[mouse]) {
                        R.sound.playButton();
                        src.callResistanceUpdate(coordinate[mouse]);
                    }
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
                GamePane src=R.M.getContent(GamePane.class);
                RealTimeData target=src.info[src.optionX][src.optionY];
                if(mouse>=0){
                    boolean[] available=calcAvailability();
                    if(available[mouse])
                        setToolTipText(Integer.toString(target.resistance.get(coordinate[mouse])));
                    else
                        setToolTipText("-1");
                }
            }
        });
        for (int i = 1; i <= 4; i++) {
            unlock.put(GamePane.DROUGHT+i,false);
            unlock.put(GamePane.FREEZE+i,false);
            unlock.put(GamePane.EARTHQUAKE+i,false);
        }
    }

    @Override
    public JToolTip createToolTip() {
        return new ResistDetailTip();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(bg,0,0,getWidth(),getHeight(),this);
        g2.setStroke(new BasicStroke(getWidth() / 4.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
        g2.setColor(new Color(0,0,0,100));
        boolean[] availability=calcAvailability();
        for (int i = 0; i < 3; i++) {
            if(!availability[i])
                g2.drawArc(getWidth()/8,getWidth()/8,getWidth()*3/4,getHeight()*3/4,i*60,60);
            else if(mouse==i){
                g2.setColor(new Color(255,255,255,100));
                g2.drawArc(getWidth()/8,getWidth()/8,getWidth()*3/4,getHeight()*3/4,mouse*60,60);
                g2.setColor(new Color(0,0,0,100));
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
        int deg=(int)Math.toDegrees(angle);
        if(deg>180)
            return -1;
        return deg/60;
    }
    private boolean[] calcAvailability(){
        GamePane src=R.M.getContent(GamePane.class);
        RealTimeData target=src.info[src.optionX][src.optionY];
        boolean[] res=new boolean[3];
        Structure resist=R.structures.get("灾难预防");
        for (int i = 0; i < 3; i++) {
            res[i]=unlock.get(coordinate[i]+(target.resistance.get(coordinate[i])+1))
                    &&src.resource.data.compare(resist.getRG(target.level,Structure.BUILD));
        }
        return res;
    }
}
