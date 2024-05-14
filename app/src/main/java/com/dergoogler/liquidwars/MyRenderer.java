package com.dergoogler.liquidwars;

import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

public class MyRenderer implements GLSurfaceView.Renderer {
    public static int displayWidth;
    public static int displayHeight;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 480;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        NativeInterface.onSurfaceCreated();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        NativeInterface.onDrawFrame();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayWidth = width;
        displayHeight = height;
        NativeInterface.onSurfaceChanged(width, height);
    }
}
