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
