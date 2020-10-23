package com.ra.ui.component;

import com.ra.data.Resource;
import com.ra.data.ResourceGroup;
import com.ra.data.Structure;
import com.ra.data.Technology;
import com.ra.ui.GamePane;
import com.ra.ui.R;
import com.ra.ui.tooltip.TechDetailTip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;

public class TechnologyPane extends JPanel {
    private static final int elementWidth=200,elementHeight=50,xInterval=5,yInterval=30,
    blockWidth=elementWidth+xInterval*2,blockHeight=elementHeight+yInterval*2;
    private final Point offsets=new Point(0,0);
    private final HashMap<Point,Technology> distribution=new HashMap<>();

    private Point previous=null;
    private Point mouse=null;
    private boolean error=false;
    public String ongoingTechResearch=null;
    public int progress=0;
    public TechnologyPane(){
        super();
        setOpaque(false);
        initData();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p=calcMousePos(e.getPoint());
                if(mouse!=null) {
                    boolean satisfied=true;
                    Technology target=distribution.get(p);
                    for(String s:target.requirements)
                        satisfied=satisfied&&R.technologies.get(s).acquired;
                    GamePane src=R.M.getContent(GamePane.class);
                    if(satisfied&&!target.acquired&&ongoingTechResearch==null
                            &&src.labCount>0&&src.storagePane.hasSpace()) {
                        progress=0;
                        ongoingTechResearch=target.name;
                        src.storagePane.callStorage(target);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouse=null;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                previous=null;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(previous!=null){
                    offsets.x+=e.getX()-previous.x;
                    offsets.y+=e.getY()-previous.y;
                }
                offsets.y=Math.min(0,offsets.y);
                offsets.x=Math.min(0,offsets.x);
                previous=e.getPoint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouse=calcMousePos(e.getPoint());
                if(mouse!=null) {
                    Technology target=distribution.get(mouse);
                    if(target.satisfied||target.acquired)
                        setToolTipText(target.name);
                    else setToolTipText(null);
                }else
                    setToolTipText(null);
            }
        });
        reCalcAll();
    }
    private void initData(){
        for(Technology t:R.technologies.values())
            distribution.put(new Point(t.column,t.row),t);
    }

    @Override
    public JToolTip createToolTip() {
        return new TechDetailTip();
    }

    public void updateProgress(int increment){
        progress+=increment;
        Technology t=R.technologies.get(ongoingTechResearch);
        if(progress>=t.time){
            ongoingTechResearch=null;
            t.acquired=true;
            reCalcAll();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setColor(new Color(0,0,0,100));
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setColor(Color.CYAN);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(0,0,getWidth()-1,getHeight()-1);
        g2.setFont(R.F.deriveFont(15f));
        for(Technology t:R.technologies.values()){
            int xLoc=t.column*blockWidth+xInterval+offsets.x;
            int yLoc=t.row*blockHeight+yInterval+offsets.y;
            for(String s:t.requirements) {
                Technology target=R.technologies.get(s);
                if(target==null){
                    if(error)
                        return;
                    error=true;
                    JOptionPane.showMessageDialog(null,
                            "Script Error:\r\nFor technology \""+t.name+"\".",
                            "Script Error",JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                g2.setColor(target.acquired&&target.satisfied?Color.GREEN:Color.LIGHT_GRAY);
                g2.drawLine(target.column*blockWidth+xInterval+offsets.x+elementWidth/2,
                        target.row*blockHeight+yInterval+offsets.y+elementHeight,
                        xLoc+elementWidth/2,yLoc);
            }
            if(ongoingTechResearch!=null&&ongoingTechResearch.equals(t.name)){
                g2.setColor(new Color(0,255,0,100));
                g2.fillRect(xLoc,yLoc,(progress+1)*elementWidth/t.time,elementHeight);
            }
            if(mouse!=null&&(t.satisfied||t.acquired))
                if(mouse.x==t.column&&mouse.y==t.row) {
                    g2.setColor(new Color(255,255,255,100));
                    g2.fillRect(xLoc,yLoc,elementWidth,elementHeight);
                }
            g2.setColor(t.acquired?(t.satisfied?Color.GREEN:Color.RED):(t.satisfied?Color.YELLOW:Color.LIGHT_GRAY));
            if(ongoingTechResearch!=null&&ongoingTechResearch.equals(t.name))
                g2.setColor(Color.MAGENTA);
            g2.drawRect(xLoc,yLoc,elementWidth,elementHeight);
            if(!t.satisfied&&!t.acquired)
                g2.drawString("???",xLoc+elementWidth/2-5,yLoc+elementHeight-20);
            else
                g2.drawString(t.name,xLoc+5,yLoc+elementHeight-20);
        }
    }
    private Point calcMousePos(Point original){
        original.x-=offsets.x;
        original.y-=offsets.y;
        int xInternalOffset= original.x%blockWidth;
        int yInternalOffset= original.y%blockHeight;
        if(xInternalOffset>xInterval&&xInternalOffset<blockWidth-xInterval&&
                yInternalOffset>yInterval&&yInternalOffset<blockHeight-yInterval){
            Point p=new Point(original.x/blockWidth, original.y/blockHeight);
            Technology target=distribution.get(p);
            return target==null?null:p;
        }
        return null;
    }
    public void reCalcAll(){
        for(Technology t:R.technologies.values())
            calcSatisfied(t);
        for(Structure s:R.structures.values()){
            s.produce=(ResourceGroup) R.original_structures.get(s.name).produce.clone();
            s.consume=(ResourceGroup) R.original_structures.get(s.name).consume.clone();
        }
        for(Technology t:R.technologies.values()){
            if(t.acquired&&t.satisfied){
                Structure target=R.structures.get(t.modifier);
                for(String res:R.resources.keySet()){
                    target.consume.data.put(res,target.consume.data.get(res)+t.consume.data.get(res));
                    target.produce.data.put(res,target.produce.data.get(res)+t.produce.data.get(res));
                }
            }
        }
    }
    private boolean calcSatisfied(Technology t){
        t.satisfied=true;
        for(String s:t.requirements) {
            Technology target=R.technologies.get(s);
            if(!target.acquired||!calcSatisfied(target))
                t.satisfied=false;
        }
        return t.satisfied;
    }
}
