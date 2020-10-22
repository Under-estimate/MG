package com.ra.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 提示加载中的窗体。
 * @author zjs
 * */
public class LoadingFrame extends JFrame {
    JLabel l;
    public LoadingFrame(){
        super("Loading...");
        initComponents();
    }
    private void initComponents(){
        setUndecorated(true);
        setIconImage(R.getImageResource("ra.png"));
        setLayout(new BorderLayout());
        Dimension d= Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((d.width-300)/2,(d.height-100)/2,300,100);
        l=new JLabel("Loading...");
        l.setFont(R.F);
        add(new JPanel(),BorderLayout.NORTH);
        add(l,BorderLayout.CENTER);
        JProgressBar prog=new JProgressBar();
        prog.setIndeterminate(true);
        add(prog,BorderLayout.SOUTH);
    }
    public void setText(String text){
        l.setText(text);
    }
}
