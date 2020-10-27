package com.ra.ui.component;

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
    public static String[] nextPhase={"建筑：机械式工厂","建筑：机械式农场","建筑：机械式矿场","建筑：公寓式民居","建筑：现代能源工厂"};
    public static HashMap<String,String> resist=new HashMap<>();
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
        resist.put("抗震建筑材料I",GamePane.EARTHQUAKE+1);
        resist.put("保温建筑材料I",GamePane.FREEZE+1);
        resist.put("储水设备研究I",GamePane.DROUGHT+1);
        resist.put("抗震建筑材料II",GamePane.EARTHQUAKE+2);
        resist.put("保温建筑材料II",GamePane.FREEZE+2);
        resist.put("储水设备研究II",GamePane.DROUGHT+2);
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
        for(String str:R.structures.keySet()){
            R.structures.get(str).copyRG(R.original_structures.get(str));
            R.structures.get(str).unlock.putAll(R.original_structures.get(str).unlock);
        }
        for(Technology t:R.technologies.values()){
            if(t.acquired&&t.satisfied){
                Structure target=R.structures.get(t.modifier);
                for(int i=1;i<=2;i++){
                    target.getRG(i,Structure.CONSUME).add(t.consume,true);
                    target.getRG(i,Structure.PRODUCE).add(t.produce,true);
                }
                if(t.unlockLevel>0)
                    target.unlock.put(t.unlockLevel,true);
            }
        }
        boolean next=true;
        for(String s:nextPhase){
            next=next&&R.technologies.get(s).acquired&&R.technologies.get(s).satisfied;
        }
        if(R.M!=null) {
            GamePane src = R.M.getContent(GamePane.class);
            src.callPhaseChange(next ? 2 : 1);
            for(String s:resist.keySet()){
                src.resist.unlock.put(resist.get(s),R.technologies.get(s).acquired&&R.technologies.get(s).satisfied);
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
