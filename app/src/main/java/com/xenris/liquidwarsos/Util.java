package com.xenris.liquidwarsos;

import android.os.Handler;
import android.app.Dialog;
import android.content.Context;

public class Util {
    public static void loadPlayerInitialPositions(short[][] xs, short[][] ys) {
        for(int p = 0; p < 6; p++)
            for(int i = 0; i < 5; i++)
                xs[p][i] = ys[p][i] = -1;
        final int ydis = 50;
        final int xdis = 20;
        xs[0][0] = xdis;
        ys[0][0] = ydis;
        xs[1][0] = xdis;
        ys[1][0] = MyRenderer.HEIGHT - ydis;
        xs[2][0] = MyRenderer.WIDTH / 2;
        ys[2][0] = ydis;
        xs[3][0] = MyRenderer.WIDTH / 2;
        ys[3][0] = MyRenderer.HEIGHT - ydis;
        xs[4][0] = MyRenderer.WIDTH - xdis;
        ys[4][0] = ydis;
        xs[5][0] = MyRenderer.WIDTH - xdis;
        ys[5][0] = MyRenderer.HEIGHT - ydis;
    }

    public static int teamToColour(int p) {
        switch(p) {
            case 0: return 0xff00ff00;
            case 1: return 0xff2020ff;
            case 2: return 0xffff0000;
            case 3: return 0xff00ffff;
            case 4: return 0xffffff00;
            case 5: return 0xffff00ff;
        }
        return 0xffffffff;
    }

    public static String teamToNameString(int p) {
        switch(p) {
            case 0: return "Green";
            case 1: return "Blue";
            case 2: return "Red";
            case 3: return "Cyan";
            case 4: return "Yellow";
            case 5: return "Magenta";
        }
        return "Unknown";
    }

    public static String getMapName(Context c, int m) {
        String[] maps = c.getResources().getStringArray(R.array.maps_array);
        return maps[m];
    }

    public static String getTimeoutString(Context c, int t) {
        String[] timeout = c.getResources().getStringArray(R.array.timeout_array);
        return timeout[t];
    }

    public static int clientIdToPlayerNumber(int id) {
        for(int i = 0; i < 6; i++)
            if(StaticBits.teams[i] == id)
                return i;
        return -1;
    }

    public static int intToTime(int i) {
        switch(i) {
            case 0: return 30;
            case 1: return 60;
            case 2: return 60*2;
            case 3: return 60*3;
            case 4: return 60*5;
            case 5: return 60*10;
            case 6: return 60*60*24*23;
        }
        return 60*3;
    }

    public static void regulateSpeed(long previousTime, int totalDelay) {
        final long currentTime = System.nanoTime();
        final long timeDiff = currentTime - previousTime;

        long nanoDelay = (totalDelay*1000) - timeDiff;
        long milliDelay = nanoDelay / 1000000;

        nanoDelay -= (milliDelay*1000000);
        if(nanoDelay < 0)
            nanoDelay = 0;
        if(milliDelay < 0)
            milliDelay = 0;

        try { Thread.sleep(milliDelay, (int)nanoDelay); } catch (InterruptedException ie) { }
    }

    public static void makeDialogCancelableIn(final Dialog dialog, int millis) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
            dialog.setCanceledOnTouchOutside(true);
        }}, millis);
    }

    public static void makeDialogDismissIn(final Dialog dialog, int millis) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialog.dismiss();
        }}, millis);
    }
}
