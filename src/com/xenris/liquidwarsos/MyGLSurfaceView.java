//    This file is part of Liquid Wars.
//
//    Copyright (C) 2013 Henry Shepperd (hshepperd@gmail.com)
//
//    Liquid Wars is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Liquid Wars is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Liquid Wars.  If not, see <http://www.gnu.org/licenses/>.

package com.xenris.liquidwarsos;

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
