package com.ra.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

/**
 * 主窗体，并不实现任何功能，只作为各个游戏界面的容器，可以自由切换游戏界面。
 * @author Jingsen Zhou
 * */
public class MainFrame extends JFrame {
    private final HashMap<Class<? extends GameContentPane>,GameContentPane> content=new HashMap<>();
    private final CardLayout cardManager=new CardLayout();
    private final JPanel sizedPane=new JPanel();
    public Point offset=new Point(0,0);
    public static final int WIDTH=1280,HEIGHT=768;
    public MainFrame(){
        super("游戏名叫啥呀? alpha-1.0.0-EA - Team Rise Again");
        R.loading.setText("Initializing main frame...");
        setMinimumSize(new Dimension(WIDTH,HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((d.width-WIDTH)/2,(d.height-HEIGHT)/2,WIDTH,HEIGHT);
        initComponents();
    }
    private void initComponents(){
        setLayout(null);
        add(sizedPane);
        getContentPane().setBackground(Color.BLACK);
        sizedPane.setLayout(cardManager);
        sizedPane.setBackground(Color.BLACK);
        setIconImage(R.getImageResource("ra.png"));
        try {
            for (Class<? extends GameContentPane> c : R.getGameContents()) {
                GameContentPane panel=c.newInstance();
                sizedPane.add(panel,c.getName());
                content.put(c,panel);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateContentSize();
            }
        });
        addWindowStateListener(e-> {
            updateContentSize();
            sizedPane.revalidate();
            sizedPane.repaint();
        });
        updateContentSize();
    }
    /**
     * 更新内容板块的大小，以保证比例固定。
     * */
    private void updateContentSize(){
        int pw=getContentPane().getWidth()*91;
        int ph=getContentPane().getHeight()*160;
        if(pw>=ph){
            int w=ph/91;
            sizedPane.setBounds((getContentPane().getWidth()-w)/2+offset.x,offset.y,w,getContentPane().getHeight());
        }else{
            int h=pw/160;
            sizedPane.setBounds(offset.x,(getContentPane().getHeight()-h)/2+offset.y,getContentPane().getWidth(),h);
        }

    }
    /**
     * 获取指定的游戏界面对象。
     * @param c 指定的游戏界面类，必须带有GameContent注解，且为JPanel的子类。
     * @see GameContent
     * */
    @SuppressWarnings("unchecked")
    public <T> T getContent(Class<T> c){
        return (T)content.get(c);
    }

    /**
     * 切换到指定的游戏界面。
     * @param c 指定的游戏界面类，必须带有GameContent注解，且为JPanel的子类。
     * @see GameContent
     * */
    public void switchToContent(Class<? extends JPanel> c){
        cardManager.show(sizedPane,c.getName());
    }

    public void setContentOffset(int x,int y){
        offset=new Point(x,y);
        updateContentSize();
    }
}
