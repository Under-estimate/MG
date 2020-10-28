package com.ra;

import com.ra.ui.GamePane;
import com.ra.ui.R;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Future;

public class SoundManager {
    Clip button;
    HashMap<String,String> players;
    Future<?> future=null;
    String playing=null;
    Player p;

    public SoundManager(){
        try {
            button=AudioSystem.getClip();
            button.open(AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(R.loader.getResourceAsStream("Sounds/button.wav")))));
            players=new HashMap<>();
            players.put("1","Sounds/music1.mp3");
            players.put("2","Sounds/music2.mp3");
            players.put(GamePane.FREEZE,"Sounds/严寒.mp3");
            players.put(GamePane.DROUGHT,"Sounds/旱灾.mp3");
            players.put(GamePane.EARTHQUAKE,"Sounds/地震.mp3");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void playButton(){
        button.setFramePosition(0);
        button.start();
    }
    public void playMusic(String music){
        if(music.equals(playing))
            return;
        playing=music;
        if(p!=null) {
            p.close();
            p=null;
        }
        future=R.exec.submit(()-> {
            try {
                while(playing.equals(music)) {
                    p = new Player(R.loader.getResourceAsStream(players.get(music)));
                    p.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
