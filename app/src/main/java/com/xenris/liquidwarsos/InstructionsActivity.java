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

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.view.Window;

public class InstructionsActivity extends Activity {
    private static final String instructions1 = "Aim:\n Convert the enemy's army to your own colour to win.";
    private static final String instructions2 = "How to play:\n Wherever you touch the screen your army will run to.\n Use up to five fingers to guide your army.\n Enemy liquid will be converted to your own when they collide.\n Try to surround the enemy for the best effect.";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.instructions);

        TextView tv;
        tv = (TextView)findViewById(R.id.instructions1);
        tv.setText(instructions1);
        tv = (TextView)findViewById(R.id.instructions2);
        tv.setText(instructions2);
    }
}
