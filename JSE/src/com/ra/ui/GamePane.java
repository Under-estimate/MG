package com.ra.ui;

import com.ra.data.ResourceGroup;
import com.ra.data.Structure;
import com.ra.ui.component.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 游戏主界面。(仍在开发中)
 * @author Jingsen Zhou
 * */
@GameContent
public class GamePane extends GameContentPane {
    private Thread renderer;
    private final BufferedImage warning=R.getImageResource("Images/warning.png");
    private final Structure[][] state=new Structure[5][5];
    private final boolean[][] lack=new boolean[5][5];
    private int mouseX=-1,mouseY=-1;
    private int optionX=-1,optionY=-1;
    private boolean showOption=false;
    public Point building=null;
    public static int xOffset=100,yOffset=500,metric=100;
    protected ConstructionOption options=new ConstructionOption();
    protected BuildingOperation operation=new BuildingOperation();

    @LayoutParam(offsetX=10,offsetY=10,fixedWidth=100,fixedHeight=100)
    protected final MyButton technology=new MyButton("科技");
    @LayoutParam(offsetX=10,offsetY=120,fixedWidth=100,fixedHeight=100)
    protected final MyButton storage=new MyButton("存储");
    @LayoutParam(anchorX=1,anchorY=1,offsetX=-1000,offsetY=-60,fixedWidth = 990,fixedHeight = 50)
    public final ResourceDisplay resource;

    public GamePane(){
        super();
        setOpaque(false);
        ResourceGroup initial=new ResourceGroup();
        initial.data.put("人口",200);
        initial.data.put("食物",200);
        resource=new ResourceDisplay(initial);
        initComponents();
        initLayout();
    }
    private void initComponents(){
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                xOffset=(int)(0.23*getWidth());
                yOffset=(int)(0.57*getHeight());
                metric=(int)(0.055*getWidth());
            }
        });
        addImage("background",new ParameterizedImage("Images/back1.png",0,
                new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
        addImage("board",new ParameterizedImage("Images/board1.png",1,
                new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                lack[i][j]=false;
        technology.setForeground(Color.CYAN);
        storage.setForeground(Color.green);
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                state[i][j]=null;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p=calcTransformedPosition(e.getPoint(),xOffset,yOffset,metric);
                optionX=p.x;
                optionY=p.y;
                showOption=optionX>=0&&optionY>=0;
                double xMetric=metric*Math.cos(Math.atan(0.5));
                double yMetric=metric*Math.sin(Math.atan(0.5));
                double xBias=xOffset+(optionX+optionY)*xMetric,yBias=yOffset+(optionX-optionY)*yMetric;
                remove(options);
                remove(operation);
                if(showOption){
                    ConstraintLayout.LayoutParamClass param=new ConstraintLayout.LayoutParamClass
                            ((int)(xBias-xMetric*0.7),(int)(yBias-yMetric*3),metric*3,metric*3);
                    if(state[optionX][optionY]==null)
                        add(options,param);
                    else
                        add(operation,param);
                }
                revalidate();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                mouseX=-1;
                mouseY=-1;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e){
                Point p=calcTransformedPosition(e.getPoint(),xOffset,yOffset,metric);
                mouseX=p.x;
                mouseY=p.y;
            }
        });
        renderer=new Thread(()->{
            int counter=0;
            while(true){
                GamePane.this.repaint();
                counter++;
                if(counter>=20) {
                    R.exec.execute(this::calcResourceModification);
                    counter=0;
                }
                try {
                    Thread.sleep(50);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },"Renderer");
        renderer.setDaemon(true);
        renderer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int xMetric=(int)(metric*Math.cos(Math.atan(0.5)));
        int yMetric=(int)(metric*Math.sin(Math.atan(0.5)));
        int xBias=xOffset+(optionX+optionY)*xMetric,yBias=yOffset+(optionX-optionY)*yMetric;
        if(optionX>=0&&optionY>=0){
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(Color.white);
            g2.drawPolygon(new int[]{xBias,xBias+xMetric,xBias+xMetric*2,xBias+xMetric},
                    new int[]{yBias,yBias-yMetric,yBias,yBias+yMetric},4);
        }
        xBias=xOffset+(mouseX+mouseY)*xMetric;
        yBias=yOffset+(mouseX-mouseY)*yMetric;
        if(mouseX>=0&&mouseY>=0){
            g2.setColor(new Color(255,255,255,100));
            g2.fillPolygon(new int[]{xBias,xBias+xMetric,xBias+xMetric*2,xBias+xMetric},
                    new int[]{yBias,yBias-yMetric,yBias,yBias+yMetric},4);
        }
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++){
                xBias=xOffset+(i+j)*xMetric;
                yBias=yOffset+(i-j)*yMetric;
                if(state[i][j]!=null)
                    g2.drawImage(state[i][j].image,(int)(xBias+0.2*metric),(int)(yBias-metric*1.2),(int)(1.5*metric),(int)(1.5*metric),this);
                if(lack[i][j])
                    g2.drawImage(warning,(int)(xBias+0.5*metric),(int)(yBias-metric*0.5),(int)(0.5*metric),(int)(0.5*metric),this);
            }
        super.paintChildren(g);
    }
    public void callBuild(String structure){
        showOption=false;
        remove(options);
        R.exec.execute(()->{
            Point p=new Point(optionX,optionY);
            building=p;
            ConstructingPane construction=new ConstructingPane();
            double xMetric=metric*Math.cos(Math.atan(0.5));
            double yMetric=metric*Math.sin(Math.atan(0.5));
            double xBias=xOffset+(optionX+optionY)*xMetric,yBias=yOffset+(optionX-optionY)*yMetric;
            ConstraintLayout.LayoutParamClass param=new ConstraintLayout.LayoutParamClass(
                    (int)(xBias+0.2*metric),(int)(yBias-metric*1.2),metric,metric);
            add(construction,param);
            revalidate();
            Structure target=R.structures.get(structure);
            ResourceGroup group=new ResourceGroup();
            for(String s:R.resources.keySet())
                group.data.put(s,-target.build.data.get(s));
            resource.submitChange(group);
            for (int i = 0; i < target.time; i++) {
                construction.progress=(i+1)/(double)target.time;
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            remove(construction);
            state[p.x][p.y]=R.structures.get(structure);
            building=null;
        });
    }
    public void callDestroy(){
        state[optionX][optionY]=null;
        remove(operation);
        revalidate();
    }
    private void calcResourceModification(){
        boolean hasChange=true;
        ResourceGroup update=new ResourceGroup();
        ArrayList<Point> lacking=new ArrayList<>();
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                if(state[i][j]!=null)
                    lacking.add(new Point(i,j));
        while(hasChange) {
            hasChange=false;
            Iterator<Point> it=lacking.iterator();
            while(it.hasNext()){
                Point p=it.next();
                Structure target=state[p.x][p.y];
                boolean isLack=false;
                for(String s:target.consume.data.keySet()) {
                    if ( update.data.get(s) + resource.data.data.get(s) - target.consume.data.get(s)< 0) {
                        isLack=true;
                        break;
                    }
                }
                if(!isLack) {
                    for (String s : R.resources.keySet())
                        update.data.put(s, update.data.get(s) + target.produce.data.get(s) - target.consume.data.get(s));
                    it.remove();
                    hasChange=true;
                }
            }
        }
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                lack[i][j]=false;
        for(Point p:lacking)
            lack[p.x][p.y]=true;
        resource.submitChange(update);
    }
    /**
     * 计算鼠标点击处对应的区域坐标。
     * */
    public static Point calcTransformedPosition(Point origin,int xOffset,int yOffset,int metric){
        int x,y;
        double dist=Math.hypot(origin.x-xOffset,origin.y-yOffset);
        double angle=Math.atan((double)(origin.y-yOffset)/(double)(origin.x-xOffset));
        double lAngle=Math.atan(2)*2;
        angle+=Math.atan(0.5);
        if(angle<0||angle>2*Math.atan(0.5)){
            x=y=-1;
        }else {
            int ty = (int) (dist * Math.sin(angle) / Math.sin(lAngle));
            angle = Math.PI - angle - lAngle;
            int tx = (int) (dist * Math.sin(angle) / Math.sin(lAngle));
            tx /= metric;
            ty /= metric;
            y = tx >= 0 && tx < 5 ? tx : -1;
            x = ty >= 0 && ty < 5 ? ty : -1;
        }
        return new Point(x,y);
    }
}
