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
import android.widget.Toast;
import android.view.View;
import android.view.Window;
import android.content.Intent;

public class MainMenuActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main_menu);
    }

    public void singlePlayerMenu(View view) {
        Intent intent = new Intent(this, SinglePlayerGameSetupActivity.class);
        startActivity(intent);
    }

    public void multiplayerMenu(View view) {
        String ip = NetInfo.getIPAddress(this);
        if((ip == null) || (ip.compareTo("0.0.0.0") == 0)) {
            Toast.makeText(this, "Need Wi-Fi connection for multiplayer game.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, MultiplayerMenuActivity.class);
            startActivity(intent);
        }
    }

    public void instructions(View view) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }
}
