package com.ra.ui;

import com.ra.data.Resource;
import com.ra.data.Structure;
import com.ra.data.Technology;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 共享资源类。
 * @author Jingsen Zhou
 * */
public class R {
    public static LoadingFrame loading;
    /**主窗体*/
    public static MainFrame M;
    /**主字体*/
    public static Font F=new Font("Microsoft YaHei",Font.PLAIN,20);
    public static ScheduledThreadPoolExecutor exec;
    /**所有资源类型*/
    public static HashMap<String, Resource> resources;
    /**所有建筑类型*/
    public static HashMap<String, Structure> structures;
    public static HashMap<String, Structure> original_structures;
    /**所有科技类型*/
    public static HashMap<String, Technology> technologies;

    public static ClassLoader loader=ClassLoader.getSystemClassLoader();


    public static void initResources(){
        loading=new LoadingFrame();
        loading.setVisible(true);
        exec=new ScheduledThreadPoolExecutor(3);
        loadXMLs();
        M=new MainFrame();
        loading.setVisible(false);
        R.M.setVisible(true);
    }
    private static void forEachElement(Element root, String name, Consumer<Element> c){
        NodeList list=root.getElementsByTagName(name);
        for (int i = 0; i < list.getLength(); i++) {
            c.accept((Element)list.item(i));
        }
    }
    private static void loadXMLs(){
        try {
            loading.setText("Loading resource.xml");
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document d=builder.parse(loader.getResourceAsStream("Scripts/resource.xml"));
            resources=new HashMap<>();
            forEachElement(d.getDocumentElement(),"resource", e->{
                Resource r=new Resource(e.getTextContent(),e.getAttribute("description"));
                r.image=getImageResource("Images/"+r.name+".png");
                r.instant=Boolean.parseBoolean(e.getAttribute("instant"));
                resources.put(r.name,r);
            });
            loading.setText("Loading structure.xml");
            d=builder.parse(loader.getResourceAsStream("Scripts/structure.xml"));
            structures=new HashMap<>();
            original_structures=new HashMap<>();
            String[] types={Structure.BUILD,Structure.CONSUME,Structure.PRODUCE};
            forEachElement(d.getDocumentElement(), "structure", e -> {
                Structure s=new Structure(e.getAttribute("name"),e.getAttribute("description"));
                s.images.put(1,getImageResource("Images/"+s.name+"1.png"));
                s.images.put(2,getImageResource("Images/"+s.name+"2.png"));
                forEachElement(e,"level",el->{
                    s.times.put(Integer.parseInt(el.getAttribute("num")),
                            Integer.parseInt(el.getAttribute("time")));
                    for (String type : types)
                        forEachElement((Element) e.getElementsByTagName(type).item(0), "resource",
                                ele -> s.getRG(Integer.parseInt(el.getAttribute("num")), type).data.put(ele.getAttribute("name")
                                        , Integer.parseInt(ele.getTextContent())));
                });
                structures.put(s.name,s);
                original_structures.put(s.name,s.clone());
            });
            loading.setText("loading technology.xml");
            d=builder.parse(loader.getResourceAsStream("Scripts/technology.xml"));
            technologies=new HashMap<>();
            forEachElement(d.getDocumentElement(),"technology",e->{
                Technology t=new Technology(e.getAttribute("name"),e.getAttribute("target"),Integer.parseInt(e.getAttribute("time")));
                t.column=Integer.parseInt(e.getAttribute("column"));
                t.row=Integer.parseInt(e.getAttribute("row"));
                t.era=Integer.parseInt(e.getAttribute("era"));
                ArrayList<String> trl=new ArrayList<>();
                forEachElement((Element)e.getElementsByTagName("requirements").item(0),"requirement",
                        ele->trl.add(ele.getTextContent()));
                t.requirements=trl.toArray(new String[0]);
                if(t.requirements.length<=0)
                    t.acquired=true;
                forEachElement((Element)e.getElementsByTagName("consume").item(0),"resource",
                        ele->t.consume.data.put(ele.getAttribute("name"),Integer.parseInt(ele.getTextContent())));
                forEachElement((Element)e.getElementsByTagName("produce").item(0),"resource",
                        ele->t.produce.data.put(ele.getAttribute("name"),Integer.parseInt(ele.getTextContent())));
                NodeList list=e.getElementsByTagName("unlock");
                if(list.getLength()==1)
                    t.unlockLevel=Integer.parseInt(list.item(0).getTextContent());
                technologies.put(t.name,t);
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static BufferedImage getImageResource(String name){
        try{
            InputStream is=loader.getResourceAsStream(name);
            if(is==null)
                return ImageIO.read(Objects.requireNonNull(loader.getResourceAsStream("Images/null.png")));
            else
                return ImageIO.read(is);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取所有带有GameContent注解的类的Class对象。
     * @see com.ra.ui.GameContent
     * */
    @SuppressWarnings("unchecked")
    static ArrayList<Class<? extends GameContentPane>> getGameContents(){
        JarFile jar;
        ArrayList<Class<? extends GameContentPane>> classes=new ArrayList<>();
        String packageName;
        try {
            String currentJarLoc=R.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File currentJar=new File(currentJarLoc);
            jar = new JarFile(currentJar);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.charAt(0) == '/')
                    name = name.substring(1);
                int idx = name.lastIndexOf('/');
                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                    if (name.endsWith(".class") && !entry.isDirectory()) {
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        try {
                            Class<?> c=Class.forName(packageName + '.' + className);
                            if(c.isAnnotationPresent(GameContent.class)&&c.getSuperclass().equals(GameContentPane.class))
                                classes.add((Class<? extends GameContentPane>)c);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }
}
