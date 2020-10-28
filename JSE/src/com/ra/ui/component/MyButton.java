package com.ra.ui.component;

import com.ra.ui.R;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 好看的按钮。
 * @author 周敬森
 * */
public class MyButton extends JPanel {
    private final JLabel label=new JLabel();
    private final String text;
    private ActionListener listener;
    private boolean press=false,hover=false;
    public MyButton(String text){
        super();
        this.text=text;
        setOpaque(false);
        setLayout(new BorderLayout());
        initComponents();
    }
    @Override
    public void paint(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        if(hover) {
            g2.setColor(new Color(255, 255, 255, 100));
            if(press)
                g2.setColor(Color.white);
            g2.fillRect(0,0,getWidth(),getHeight());
        }
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(getForeground());
        g2.drawRect(0,0,getWidth()-1,getHeight()-1);
        super.paint(g);
    }
    @Override
    public void setForeground(Color foreground){
        if(label!=null)
            label.setForeground(foreground);
        super.setForeground(foreground);
    }
    private void initComponents(){
        label.setText(text);
        label.setFont(R.F.deriveFont(30f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label,BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                press=true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                press=false;
                if(listener!=null) {
                    R.sound.playButton();
                    listener.actionPerformed(null);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hover=true;
                setBackground(Color.DARK_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover=false;
                setBackground(Color.BLACK);
            }
        });
    }
    public void setActionListener(ActionListener l){
        this.listener=l;
    }
    public void setText(String text){
        label.setText(text);
    }
}
