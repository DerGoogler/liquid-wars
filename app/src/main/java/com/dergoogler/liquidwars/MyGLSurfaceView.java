package com.dergoogler.liquidwars;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MyGLSurfaceView extends GLSurfaceView implements View.OnHoverListener {
    private SurfaceCallbacks surfaceCallbacks;

    private void init(Context context) {
        setRenderer(new MyRenderer());
        this.setOnGenericMotionListener(this::onHover);
    }

    public MyGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (surfaceCallbacks != null)
            surfaceCallbacks.onTouch(event);

        return true;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (surfaceCallbacks != null)
            surfaceCallbacks.onHover(v, event);
        return false;
    }

    public void setSurfaceCallbacks(SurfaceCallbacks sc) {
        surfaceCallbacks = sc;
    }


    public interface SurfaceCallbacks {
        void onTouch(MotionEvent event);

        void onHover(View v, MotionEvent event);
    }
}
