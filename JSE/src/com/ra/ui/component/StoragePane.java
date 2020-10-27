package com.ra.ui.component;

import com.ra.data.Technology;
import com.ra.ui.GamePane;
import com.ra.ui.R;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;

public class StoragePane extends JPanel {
    public static final int ORIGINAL_STORAGE=0;
    public static final int STORAGE_FULL=1;
    public static final int STORAGE_AVAILABLE=2;
    private static final int itemHeight=50;
    private final HashMap<Point,Integer> capacity=new HashMap<>();
    private final HashMap<Point, ArrayList<Technology>> storage=new HashMap<>();
    private final HashMap<Technology,Point> reverseIndex=new HashMap<>();
    private final ArrayList<Technology> listPos=new ArrayList<>();
    private int offsetY=0;
    private Point previous=null;
    private Point mouse=null;
    private Technology transferring=null;
    public StoragePane(){
        super();
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p=calcMousePos(e.getPoint());
                if(p==null||p.x!=2)
                    return;
                GamePane src=R.M.getContent(GamePane.class);
                setVisible(false);
                HashMap<Point,Integer> map=new HashMap<>();
                Technology t=listPos.get(p.y);
                Point original=reverseIndex.get(t);
                for(Point px:storage.keySet()){
                    if(px.equals(original))
                        map.put(px, ORIGINAL_STORAGE);
                    else
                        map.put(px, storage.get(px).size()>=capacity.get(px)?STORAGE_FULL:STORAGE_AVAILABLE);
                }
                src.storageSelectMode=map;
                src.storage.setText("取消");
                transferring=t;
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouse=null;
                GamePane src=R.M.getContent(GamePane.class);
                src.storageShowX=-1;
                src.storageShowY=-1;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                previous=null;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(previous!=null)
                    offsetY+=e.getY()-previous.y;
                offsetY= Math.min(offsetY, 0);
                offsetY= Math.max(Math.min(0,getHeight()-itemHeight*storage.size()*5),offsetY);
                previous=e.getPoint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouse=calcMousePos(e.getPoint());
                GamePane src=R.M.getContent(GamePane.class);
                if(mouse==null||mouse.x!=1){
                    src.storageShowX=-1;
                    src.storageShowY=-1;
                    return;
                }
                Point loc=reverseIndex.get(listPos.get(mouse.y));
                src.storageShowX=loc.x;
                src.storageShowY=loc.y;
            }
        });
    }
    public void callStorageConstructed(Point p){
        capacity.put(p,5);
        storage.put(p,new ArrayList<>());
    }
    public void callStorageUpgraded(Point p){
        capacity.put(p,10);
    }
    public Point callStorage(Technology t){
        for(Point px:storage.keySet()){
            if(storage.get(px).size()<capacity.get(px)){
                storage.get(px).add(t);
                reverseIndex.put(t,px);
                listPos.add(t);
                return px;
            }
        }
        throw new IllegalStateException("On storage: no storage space available");
    }
    public boolean hasSpace(){
        for(Point px:storage.keySet())
            if(storage.get(px).size()<capacity.get(px))
                return true;
        return false;
    }
    public void callDataTransfer(Point dest){
        Point original=reverseIndex.get(transferring);
        R.M.getContent(GamePane.class).storage.setText("存储");
        storage.get(original).remove(transferring);
        storage.get(dest).add(transferring);
        reverseIndex.put(transferring,dest);
    }
    public Technology[] callStorageDestructed(Point p){
        Technology[] ta=storage.remove(p).toArray(new Technology[0]);
        for(Technology t:ta) {
            t.acquired=false;
            reverseIndex.remove(t);
            listPos.remove(t);
        }
        return ta;
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0,0,0,100));
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setColor(Color.GREEN);
        g2.setStroke(new BasicStroke(2f));
        g2.setFont(R.F.deriveFont(30f));
        g2.drawRect(0,0,getWidth()-1,getHeight()-1);
        g2.drawLine((int)(getWidth()*0.7),0,(int)(getWidth()*0.7),getHeight());
        g2.drawLine((int)(getWidth()*0.85),0,(int)(getWidth()*0.85),getHeight());
        int x1=(int)(getWidth()*0.7);
        int x2=(int)(getWidth()*0.85);
        for(int i=0;i<listPos.size();i++){
            Technology t=listPos.get(i);
            int yb=i*itemHeight+offsetY;
            String display="显示位置";
            if(mouse!=null&&i==mouse.y){
                g2.setColor(new Color(255,255,255,100));
                if(mouse.x==1) {
                    g2.fillRect(x1, yb, x2 - x1, itemHeight);
                    display="正在显示";
                }else if(mouse.x==2)
                    g2.fillRect(x2,yb,getWidth()-x2,itemHeight);
            }
            g2.setColor(Color.GREEN);
            g2.drawString(t.name,5,yb+40);
            g2.drawString(display,x1+5,yb+40);
            g2.drawString("数据迁移",x2+5,yb+40);
            g2.drawLine(0,yb+itemHeight,getWidth(),yb+itemHeight);
        }
    }
    private Point calcMousePos(Point original){
        int x=original.x<getWidth()*0.7?0:original.x<getWidth()*0.85?1:2;
        int y=(original.y+offsetY)/itemHeight;
        if(y>=listPos.size())
            return null;
        return new Point(x,y);
    }
}
