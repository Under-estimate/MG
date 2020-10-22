package com.ra.ui.component;

import com.ra.data.ResourceGroup;
import com.ra.ui.R;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Iterator;
import java.util.concurrent.Future;

/**
 * 显示当前的资源数据。
 * @author Jingsen Zhou
 * */
public class ResourceDisplay extends JPanel {
    public final ResourceGroup data;
    public ResourceGroup change;
    private int progress=255;
    private int mouse=-1;
    private Future<?> animating=null;
    private final Object interruptLock=new Object();
    public ResourceDisplay(ResourceGroup initial){
        super();
        setOpaque(false);
        data=initial;
        change=new ResourceGroup();
        initComponents();
    }
    private void initComponents(){
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int x=e.getX();
                mouse=x/(getWidth()/R.resources.size());
                int i=0;
                for(String s:R.resources.keySet()){
                    if(i==mouse)
                        setToolTipText(R.resources.get(s).desc);
                    i++;
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                mouse=-1;
            }
        });
    }
    public void submitChange(ResourceGroup change){
        progress=0;
        this.change=change;
        for (String s:R.resources.keySet()){
            data.data.put(s,data.data.get(s)+change.data.get(s));
        }
        if(animating!=null){
            try {
                synchronized (interruptLock) {
                    animating=null;
                    interruptLock.wait();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        animating=R.exec.submit(()->{
            for (int i = 0; i <= 51; i++) {
                progress=i*5;
                try{
                    Thread.sleep(10);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(animating==null){
                    synchronized (interruptLock) {
                        interruptLock.notify();
                    }
                    break;
                }
            }
            animating=null;
        });
    }

    @Override
    public JToolTip createToolTip() {
        return new MyToolTip();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(new Color(0,0,0,100));
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setFont(R.F);
        g2.setStroke(new BasicStroke(1f));
        int i=0;
        for (String s:R.resources.keySet()) {
            if(i==mouse){
                g2.setColor(new Color(255,255,255,100));
                g2.fillRect((int)(getWidth()*0.2*i),0,(int)(getWidth()*0.2),getHeight());
            }
            int ci=change.data.get(s);
            if(ci!=0) {
                String cs = Integer.toString(ci);
                cs = (ci > 0 ? "+" : "") + cs;
                Color c = new Color(ci < 0 ? 255 : 0, ci > 0 ? 255 : 0, 0, 255 - progress);
                g2.setColor(c);
                g2.fillRect((int) (getWidth() * (0.2 * i + 0.15)), 0, (int) (getWidth() * 0.05), getHeight());
                g2.setColor(Color.WHITE);
                g2.drawString(cs, (int) (getWidth() * (0.2 * i + 0.15)), (int) (getHeight() * 0.7));
            }
            g2.setColor(Color.WHITE);
            g2.drawImage(R.resources.get(s).image,(int)(getWidth()*i*0.2),5,40,40,this);
            if(i<4)
                g2.drawLine((int)(getWidth()*0.2*(i+1)),0,(int)(getWidth()*0.2*(i+1)),getHeight());
            g2.drawString(Integer.toString(data.data.get(s)),(int)(getWidth()*(0.2*i+0.05)),(int)(getHeight()*0.7));
            i++;
        }
    }
}
