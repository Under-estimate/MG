package com.ra.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@GameContent
public class IntroPane extends GameContentPane{
    private String intro;
    private boolean skip=false;
    @LayoutParam(widthRate = 1,heightRate = 1)
    protected JTextArea text=new JTextArea();
    public IntroPane(){
        super();
        text.setEditable(false);
        text.setFont(R.F);
        text.setBackground(Color.BLACK);
        text.setForeground(Color.WHITE);
        text.setSelectionColor(Color.BLACK);
        text.setSelectedTextColor(Color.WHITE);
        text.setBorder(new EmptyBorder(20,20,20,20));
        try{
            BufferedReader br=new BufferedReader(new InputStreamReader(Objects.requireNonNull(R.loader.getResourceAsStream("Scripts/intro.txt")),StandardCharsets.UTF_8));
            String tmp;
            StringBuilder sb=new StringBuilder();
            while((tmp=br.readLine())!=null)
                sb.append(tmp).append("\r\n");
            intro=sb.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                skip=true;
            }
        });
        initLayout();
    }
    public void launch(){
        R.exec.execute(()->{
            try{
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            for (int i = 0; i < intro.length()&&!skip; i++) {
                text.append(intro.charAt(i)+"");
                try{
                    Thread.sleep(100);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < 51; i++) {
                text.setForeground(new Color(255,255,255,255-i*5));
                try{
                    Thread.sleep(50);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            R.M.switchToContent(GamePane.class);
            R.M.getContent(GamePane.class).launch();
        });
    }
}
