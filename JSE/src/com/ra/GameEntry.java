package com.ra;

import com.ra.ui.IntroPane;
import com.ra.ui.R;
/**
 * 游戏入口点。
 * @author Jingsen Zhou
 * */
public class GameEntry {
    public static void main(String[] args) throws Exception {
        R.initResources();
        R.M.switchToContent(IntroPane.class);
        R.M.getContent(IntroPane.class).launch();
    }
}
