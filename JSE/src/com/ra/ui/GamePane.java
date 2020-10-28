package com.ra.ui;

import com.ra.data.RealTimeData;
import com.ra.data.ResourceGroup;
import com.ra.data.Structure;
import com.ra.ui.component.*;
import com.ra.ui.tooltip.BuildingDetailTip;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 游戏主界面。(仍在开发中)
 * @author Jingsen Zhou
 * */
@GameContent
public class GamePane extends GameContentPane {
    public static final String DROUGHT="旱灾",FREEZE="严寒",EARTHQUAKE="地震";
    public static String[] disasters={DROUGHT,FREEZE,EARTHQUAKE};
    public static final String[] basicTechs={"建筑：作坊式工厂","建筑：农田","建筑：矿场","建筑：民居","建筑：能源工厂","存储建筑：方尖碑","灾难研究","建筑：科研中心"};
    public static final ArrayList<String> basic=new ArrayList<>();

    /**提示建筑停工的图像*/
    private final BufferedImage warning=R.getImageResource("Images/warning.png");
    /**前景城市*/
    private BufferedImage city=R.getImageResource("Images/city1.png");
    private BufferedImage city_trans=R.getImageResource("Images/city1_transparent.png");
    /**场上的建筑物信息*/
    public final RealTimeData[][] info=new RealTimeData[6][6];
    /**鼠标所处的网格坐标*/
    public int mouseX=-1,mouseY=-1;
    /**选中的网格坐标*/
    public int optionX=-1,optionY=-1;
    /**展示存储位置的网格坐标*/
    public int storageShowX=-1,storageShowY=-1;
    /**数据迁移时一些网格的属性*/
    public HashMap<Point,Integer> storageSelectMode=null;
    /**是否显示选项*/
    private boolean showOption=false;
    /**正在建造的坐标*/
    public Point building=null;
    public ConstructingPane construction=null;
    /**科研中心计数*/
    public int labCount=0;
    /**上一次资源结算后经过的tick数*/
    private int resourceCounter=0;
    /**当前建筑进度（秒）*/
    private int currentBuildProgress=0;
    /**正在建筑的进程*/
    private ScheduledFuture<?> buildFuture=null;
    private int currentPhase=0;
    public Color paintOver=new Color(0,0,0);
    public String nextDisaster=null;
    public int nextDisasterLevel=-1;
    public int beforeDisaster=120;
    public boolean pauseResourceModification=false;
    /**网格参数*/
    public static int xOffset=100,yOffset=500,metric=100;


    protected ConstructionOption options=new ConstructionOption();
    protected BuildingOperation operation=new BuildingOperation();
    public ResistOption resist=new ResistOption();

    @LayoutParam(offsetX=10,offsetY=10,fixedWidth=100,fixedHeight=100)
    protected final MyButton technology=new MyButton("科技");
    @LayoutParam(offsetX=10,offsetY=120,fixedWidth=100,fixedHeight=100)
    public final MyButton storage=new MyButton("存储");
    @LayoutParam(anchorX=1,anchorY=1,offsetX=-1000,offsetY=-60,fixedWidth = 990,fixedHeight = 50)
    public final ResourceDisplay resource;
    @LayoutParam(offsetX=130,offsetY=80,widthRate=1,heightRate=1,fixedWidth=-140,fixedHeight=-160)
    public final TechnologyPane techPane=new TechnologyPane();
    @LayoutParam(offsetX=130,offsetY=80,widthRate=1,heightRate=1,fixedWidth=-140,fixedHeight=-160)
    public final StoragePane storagePane=new StoragePane();
    @LayoutParam(anchorX=1,offsetX=-510,offsetY=10,fixedWidth=500,fixedHeight=50)
    public final JTextField information=new JTextField();

    public GamePane(){
        super();
        setOpaque(false);
        ResourceGroup initial=new ResourceGroup();
        initial.data.put("产能",1000);
        initial.data.put("食物",100);
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
                metric=(int)(0.05*getWidth());
            }
        });
        for (int i = 0; i < info.length; i++)
            for (int j = 0; j < info[i].length; j++)
                info[i][j] = new RealTimeData();
        info[5][5].structure=R.structures.get("数据中心");
        info[5][5].resistance.put(DROUGHT,3);
        info[5][5].resistance.put(FREEZE,3);
        info[5][5].resistance.put(EARTHQUAKE,3);
        info[5][5].level=1;
        storagePane.callStorageConstructed(new Point(5,5));
        storagePane.callStorageUpgraded(new Point(5,5));
        basic.addAll(Arrays.asList(basicTechs));
        for(String s:basicTechs) {
            R.technologies.get(s).acquired=true;
            storagePane.callStorage(R.technologies.get(s));
        }
        techPane.reCalcAll();
        info[0][5].structure=R.structures.get("农田");
        info[1][5].structure=R.structures.get("民居");

        information.setBackground(Color.BLACK);
        information.setForeground(Color.WHITE);
        information.setSelectedTextColor(Color.WHITE);
        information.setSelectionColor(Color.BLACK);
        information.setEditable(false);
        information.setBorder(new LineBorder(Color.WHITE));
        information.setFont(R.F);
        information.setText("多么美好的一天啊");
        technology.setForeground(Color.CYAN);
        techPane.setVisible(false);
        storage.setForeground(Color.green);
        storagePane.setVisible(false);
        technology.setActionListener(e-> {
            techPane.setVisible(!techPane.isVisible());
            storagePane.setVisible(false);
            storage.setText("存储");
            technology.setText(techPane.isVisible()?"关闭":"科技");
        });
        storage.setActionListener(e->{
            if(storageSelectMode!=null) {
                storageSelectMode = null;
                storage.setText("存储");
                return;
            }
            storagePane.setVisible(!storagePane.isVisible());
            techPane.setVisible(false);
            technology.setText("科技");
            storage.setText(storagePane.isVisible()?"关闭":"存储");
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(pauseResourceModification)
                    return;
                Point p=calcTransformedPosition(e.getPoint(),xOffset,yOffset,metric);
                optionX=p.x;
                optionY=p.y;
                if(storageSelectMode!=null){
                    switch (storageSelectMode.get(p)){
                        case StoragePane.ORIGINAL_STORAGE:
                        case StoragePane.STORAGE_FULL:
                            storageSelectMode=null;
                            break;
                        case StoragePane.STORAGE_AVAILABLE:
                            storagePane.callDataTransfer(p);
                            storageSelectMode=null;
                            break;
                    }
                    return;
                }
                showOption=optionX>=0&&optionY>=0;
                double xMetric=metric*Math.cos(Math.atan(0.5));
                double yMetric=metric*Math.sin(Math.atan(0.5));
                double xBias=xOffset+(optionX+optionY)*xMetric,yBias=yOffset+(optionX-optionY)*yMetric;
                remove(options);
                remove(operation);
                remove(resist);
                if(showOption){
                    R.sound.playButton();
                    ConstraintLayout.LayoutParamClass param=new ConstraintLayout.LayoutParamClass
                            ((int)(xBias-xMetric*0.7),(int)(yBias-yMetric*3),metric*3,metric*3);
                    if(info[optionX][optionY].structure==null)
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
                if(mouseX>=0&&mouseY>=0&&info[mouseX][mouseY].structure!=null)
                    setToolTipText(info[mouseX][mouseY].structure.name);
                else
                    setToolTipText(null);
            }
        });
    }
    public void launch(){
        callPhaseChange(1);
        R.exec.scheduleAtFixedRate(()->{
            try {
                GamePane.this.repaint();
                resourceCounter++;
                if (resourceCounter >= 20) {
                    calcResourceModification();
                    resourceCounter = 0;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        },0,50, TimeUnit.MILLISECONDS);
        R.exec.execute(()->{
            paintOverTransition(Color.BLACK,255,0);
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int xMetric=(int)(metric*Math.cos(Math.atan(0.5)));
        int yMetric=(int)(metric*Math.sin(Math.atan(0.5)));
        int xBias,yBias;
        if(storageSelectMode!=null){
            for(Point p:storageSelectMode.keySet()){
                xBias=xOffset+(p.x+p.y)*xMetric;
                yBias=yOffset+(p.x-p.y)*yMetric;
                int stat=storageSelectMode.get(p);
                g2.setColor(stat==StoragePane.ORIGINAL_STORAGE?Color.BLUE:stat==StoragePane.STORAGE_FULL?Color.RED:Color.CYAN);
                g2.fillPolygon(new int[]{xBias,xBias+xMetric,xBias+xMetric*2,xBias+xMetric},
                        new int[]{yBias,yBias-yMetric,yBias,yBias+yMetric},4);
            }
        }
        if(storageShowX>=0&&storageShowY>=0){
            xBias=xOffset+(storageShowX+storageShowY)*xMetric;
            yBias=yOffset+(storageShowX-storageShowY)*yMetric;
            g2.setColor(Color.MAGENTA);
            g2.fillPolygon(new int[]{xBias,xBias+xMetric,xBias+xMetric*2,xBias+xMetric},
                    new int[]{yBias,yBias-yMetric,yBias,yBias+yMetric},4);
        }
        if(optionX>=0&&optionY>=0){
            xBias=xOffset+(optionX+optionY)*xMetric;
            yBias=yOffset+(optionX-optionY)*yMetric;
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(Color.white);
            g2.drawPolygon(new int[]{xBias,xBias+xMetric,xBias+xMetric*2,xBias+xMetric},
                    new int[]{yBias,yBias-yMetric,yBias,yBias+yMetric},4);
        }
        if(mouseX>=0&&mouseY>=0){
            xBias=xOffset+(mouseX+mouseY)*xMetric;
            yBias=yOffset+(mouseX-mouseY)*yMetric;
            g2.setColor(new Color(255,255,255,100));
            g2.fillPolygon(new int[]{xBias,xBias+xMetric,xBias+xMetric*2,xBias+xMetric},
                    new int[]{yBias,yBias-yMetric,yBias,yBias+yMetric},4);
        }
        for (int i = 0; i < info.length; i++)
            for (int j = 0; j < info.length; j++){
                xBias=xOffset+(i+j)*xMetric;
                yBias=yOffset+(i-j)*yMetric;
                if(info[i][j].structure!=null)
                    g2.drawImage(info[i][j].getImage(),(int)(xBias-0.1*metric),(int)(yBias-metric*1.4),2*metric,2*metric,this);
                if(info[i][j].lack)
                    g2.drawImage(warning,(int)(xBias+0.5*metric),(int)(yBias-metric*0.5),(int)(0.5*metric),(int)(0.5*metric),this);
            }
        if(mouseY==0)
            g2.drawImage(city_trans,0,0,getWidth(),getHeight(),this);
        else
            g2.drawImage(city,0,0,getWidth(),getHeight(),this);
        g2.setColor(paintOver);
        g2.fillRect(0,0,getWidth(),getHeight());
        super.paintChildren(g);
    }

    @Override
    public JToolTip createToolTip() {
        return new BuildingDetailTip();
    }
    public void callBuild(String structure){
        showOption=false;
        remove(options);
        Point p=new Point(optionX,optionY);
        building=p;
        construction=new ConstructingPane();
        double xMetric=metric*Math.cos(Math.atan(0.5));
        double yMetric=metric*Math.sin(Math.atan(0.5));
        double xBias=xOffset+(optionX+optionY)*xMetric,yBias=yOffset+(optionX-optionY)*yMetric;
        ConstraintLayout.LayoutParamClass param=new ConstraintLayout.LayoutParamClass(
                (int)(xBias+0.2*metric),(int)(yBias-metric*1.2),metric,metric);
        add(construction,param);
        revalidate();
        Structure target=R.structures.get(structure);
        ResourceGroup group=target.getRG(1,Structure.BUILD).negate();
        resource.submitVariable(group);
        buildFuture=R.exec.scheduleAtFixedRate(()->{
            construction.progress=(currentBuildProgress+1)/(double)target.times.get(1);
            currentBuildProgress++;
            if(currentBuildProgress<target.times.get(1))
                return;
            remove(construction);
            info[p.x][p.y].structure=R.structures.get(structure);
            if(structure.equals("科研中心"))
                labCount++;
            else if(structure.equals("数据中心")) {
                storagePane.callStorageConstructed(p);
            }
            building=null;
            buildFuture.cancel(false);
            buildFuture=null;
            currentBuildProgress=0;
        },0,1,TimeUnit.SECONDS);
    }
    public void callDestroy(){
        callDestroy(optionX,optionY);
    }
    public void callDestroy(int x,int y){
        if(info[x][y].structure.name.equals("科研中心"))
            labCount--;
        else if(info[x][y].structure.name.equals("数据中心")) {
            storagePane.callStorageDestructed(new Point(x,y));
            techPane.reCalcAll();
        }
        info[x][y].structure=null;
        remove(operation);
        revalidate();
    }
    public void callUpgrade(){
        showOption=false;
        remove(operation);
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
        RealTimeData target=info[optionX][optionY];
        ResourceGroup group=target.structure.getRG(target.level+1,Structure.BUILD).negate();
        resource.submitVariable(group);
        buildFuture=R.exec.scheduleAtFixedRate(()->{
            int time=target.structure.times.get(target.level+1);
            construction.progress=(currentBuildProgress+1)/(double)time;
            currentBuildProgress++;
            if(currentBuildProgress<time)
                return;
            remove(construction);
            info[p.x][p.y].level++;
            if(info[p.x][p.y].structure.name.equals("数据中心"))
                storagePane.callStorageUpgraded(p);
            building=null;
            buildFuture.cancel(false);
            buildFuture=null;
            currentBuildProgress=0;
        },0,1,TimeUnit.SECONDS);
    }
    private void calcResourceModification(){
        if(pauseResourceModification)
            return;
        boolean hasChange=true;
        int remainResearchProgress=0;
        if(techPane.ongoingTechResearch!=null)
            remainResearchProgress=R.technologies.get(techPane.ongoingTechResearch).time-techPane.progress;
        ResourceGroup update=new ResourceGroup();
        ArrayList<Point> lacking=new ArrayList<>();
        for (int i = 0; i < info.length; i++)
            for (int j = 0; j < info[i].length; j++)
                if (info[i][j].structure != null)
                    lacking.add(new Point(i, j));
        int requiredLab=Math.min(labCount,remainResearchProgress);
        while(hasChange) {
            hasChange=false;
            Iterator<Point> it=lacking.iterator();
            while(it.hasNext()){
                Point p=it.next();
                RealTimeData target=info[p.x][p.y];
                boolean isLack=false;
                for(String s:target.getRG(Structure.CONSUME).data.keySet()) {
                    if ( update.data.get(s) + resource.data.data.get(s) - target.getRG(Structure.CONSUME).data.get(s)< 0) {
                        isLack= !target.structure.name.equals("科研中心") || requiredLab > 0;
                        break;
                    }
                }
                if(!isLack) {
                    if(!target.structure.name.equals("科研中心")){
                        update.add(target.getRG(Structure.PRODUCE),true).sub(target.getRG(Structure.CONSUME),true);
                    }else if(requiredLab > 0){
                        update.add(target.getRG(Structure.PRODUCE),true).sub(target.getRG(Structure.CONSUME),true);
                        requiredLab--;
                    }
                    it.remove();
                    hasChange=true;
                }
            }
        }
        for (RealTimeData[] realTimeData : info)
            for (RealTimeData realTimeDatum : realTimeData)
                realTimeDatum.lack = false;
        for(Point p:lacking)
            info[p.x][p.y].lack=true;
        resource.submitChange(update);
        if(techPane.ongoingTechResearch!=null)
            techPane.updateProgress(labCount-requiredLab);
        beforeDisaster--;
        if(nextDisaster!=null) {
            if(beforeDisaster>60)
                information.setText("下一次灾难将在" + beforeDisaster + "s后降临。");
            else if(beforeDisaster>30)
                information.setText("下一次"+nextDisasterLevel+"级灾难将在"+beforeDisaster+"s后降临。");
            else
                information.setText("下一次"+nextDisasterLevel+"级"+nextDisaster+"将在"+beforeDisaster+"s后降临。");
        }
        if(beforeDisaster<=0&&nextDisaster!=null){
            R.exec.execute(()->{
                pauseResourceModification=true;
                remove(operation);
                remove(options);
                remove(resist);
                if(buildFuture!=null) {
                    buildFuture.cancel(false);
                    building = null;
                }
                if(construction!=null)
                    remove(construction);
                try{
                    Color over=null;
                    String image=null;
                    switch (nextDisaster){
                        case DROUGHT:
                            R.sound.playMusic(DROUGHT);
                            information.setBackground(Color.YELLOW);
                            information.setForeground(Color.RED);
                            information.setText("警告：过于炎热");
                            over=new Color(255,255,200);
                            image="Images/atmos_hot.png";
                        case FREEZE:
                            if(over==null) {
                                R.sound.playMusic(FREEZE);
                                information.setBackground(Color.BLACK);
                                information.setForeground(Color.CYAN);
                                information.setText("警告：异常寒冷");
                                over = new Color(0, 0, 50);
                                image = "Images/atmos_cold.png";
                            }
                            paintOverTransition(over,0,255);
                            addImage("atmos",new ParameterizedImage(image,2,
                                    new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
                            paintOverTransition(over,255,100);
                            Thread.sleep(3000);
                            paintOverTransition(over,100,255);
                            calcDisasterLoss();
                            addImage("atmos",new ParameterizedImage("Images/atmos"+currentPhase+".png",2,
                                    new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
                            paintOverTransition(over,255,0);
                            paintOver=new Color(0,0,0,0);
                            break;
                        case EARTHQUAKE:
                            R.sound.playMusic(EARTHQUAKE);
                            information.setBackground(Color.BLACK);
                            information.setForeground(Color.YELLOW);
                            information.setText("警告：大陆不稳定");
                            for (int i = 0; i < 40; i++) {
                                R.M.setContentOffset((int)(20*Math.random()-10),(int)(20*Math.random()-10));
                                Thread.sleep(50);
                            }
                            calcDisasterLoss();
                            for (int i = 0; i < 40; i++) {
                                R.M.setContentOffset((int)(20*Math.random()-10),(int)(20*Math.random()-10));
                                Thread.sleep(50);
                            }
                            R.M.setContentOffset(0,0);
                            break;
                    }
                    R.sound.playMusic(Integer.toString(currentPhase));
                }catch (Exception e){
                    e.printStackTrace();
                }
                nextDisaster=disasters[(int)(3*Math.random())];
                beforeDisaster=120+(int)(240*Math.random());
                nextDisasterLevel=(int)((1+currentPhase)*Math.random())+1;
                pauseResourceModification=false;
            });
        }
        if(beforeDisaster<=0&&nextDisaster==null){
            beforeDisaster=120+(int)(240*Math.random());
            nextDisaster=disasters[(int)(3*Math.random())];
            nextDisasterLevel=(int)((1+currentPhase)*Math.random())+1;
        }
        information.setBackground(Color.BLACK);
        information.setForeground(Color.white);
    }
    private void calcDisasterLoss() {
        for (int i = 0; i < info.length; i++) {
            for (int j = 0; j < info[i].length; j++) {
                if(info[i][j].structure==null)
                    continue;
                int delta=nextDisasterLevel-info[i][j].resistance.get(nextDisaster);
                if(delta<=0)
                    continue;
                if(Math.random()<(1.0/(delta+1)))
                    callDestroy(i,j);
            }
        }
    }
    private void paintOverTransition(Color base,int from,int to){
        try{
            if(from<to)
                for (int i = from; i<=to; i+=10) {
                    paintOver=new Color(base.getRed(),base.getGreen(),base.getBlue(),Math.max(0,Math.min(i,255)));
                    Thread.sleep(50);
                }
            else
                for (int i = from; i>=to; i-=10) {
                    paintOver=new Color(base.getRed(),base.getGreen(),base.getBlue(),Math.max(0,Math.min(i,255)));
                    Thread.sleep(50);
                }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void callPhaseChange(int phase){
        if(phase==currentPhase)
            return;
        R.sound.playMusic(Integer.toString(phase));
        currentPhase=phase;
        addImage("background",new ParameterizedImage("Images/back"+phase+".png",0,
                new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
        addImage("board",new ParameterizedImage("Images/board"+phase+".png",3,
                new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
        addImage("cloud",new ParameterizedImage("Images/cloud"+phase+".png",1,
                new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
        addImage("atmos",new ParameterizedImage("Images/atmos"+phase+".png",2,
                new ConstraintLayout.LayoutParamClass(0.0,0.0,1.0,1.0)));
        city=R.getImageResource("Images/city"+phase+".png");
        city_trans=R.getImageResource("Images/city"+phase+"_transparent.png");
    }
    public void callShowResistOption(){
        remove(operation);
        double xMetric=metric*Math.cos(Math.atan(0.5));
        double yMetric=metric*Math.sin(Math.atan(0.5));
        double xBias=xOffset+(optionX+optionY)*xMetric,yBias=yOffset+(optionX-optionY)*yMetric;
        ConstraintLayout.LayoutParamClass param=new ConstraintLayout.LayoutParamClass
                ((int)(xBias-xMetric*0.7),(int)(yBias-yMetric*3),metric*3,metric*3);
        add(resist,param);
        revalidate();
    }
    public void callResistanceUpdate(String type){
        showOption=false;
        remove(resist);
        RealTimeData target=info[optionX][optionY];
        resource.submitVariable(R.structures.get("灾难预防").getRG(target.resistance.get(type)+1,Structure.BUILD).negate());
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
        if(origin.x<xOffset||angle<0||angle>2*Math.atan(0.5)){
            x=y=-1;
        }else {
            int ty = (int) (dist * Math.sin(angle) / Math.sin(lAngle));
            angle = Math.PI - angle - lAngle;
            int tx = (int) (dist * Math.sin(angle) / Math.sin(lAngle));
            tx /= metric;
            ty /= metric;
            y = tx >= 0 && tx < 6 ? tx : -1;
            x = ty >= 0 && ty < 6 ? ty : -1;
        }
        return new Point(x,y);
    }
}
