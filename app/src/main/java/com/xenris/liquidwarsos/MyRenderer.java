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
