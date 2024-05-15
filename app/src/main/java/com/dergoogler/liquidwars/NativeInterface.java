package com.dergoogler.liquidwars;

import android.content.res.AssetManager;

public class NativeInterface {
    static {
        System.loadLibrary("nativeinterface");
    }

    public static native void onSurfaceCreated();
    public static native void onDrawFrame();
    public static native void onSurfaceChanged(int width, int height);
    public static native void init(AssetManager assetManager);
    public static native void uninit();
    public static native void stepDots();
    public static native void createGame(int team, int map, int seed, int dotsPerTeam);
    public static native void destroyGame();
    public static native void setPlayerPosition(int team, short[] xs, short[] ys);
    public static native int getNearestDot(int p, short px, short py);
    public static native int teamScore(int p);
    public static native void setTimeSidebar(float t);
}
