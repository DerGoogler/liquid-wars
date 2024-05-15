package com.dergoogler.liquidwars;

import android.opengl.GLSurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {
    private SurfaceCallbacks surfaceCallbacks;

    public MyGLSurfaceView(Context context) {
        super(context);
        setRenderer(new MyRenderer());
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRenderer(new MyRenderer());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(surfaceCallbacks != null)
            surfaceCallbacks.onTouch(event);

        return true;
    }

    public void setSurfaceCallbacks(SurfaceCallbacks sc) {
        surfaceCallbacks = sc;
    }

    public interface SurfaceCallbacks {
        public void onTouch(MotionEvent event);
    }
}
