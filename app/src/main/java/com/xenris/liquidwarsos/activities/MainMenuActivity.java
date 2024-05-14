

package com.xenris.liquidwarsos.activities;

import android.os.Bundle;
import android.widget.Toast;
import android.view.View;
import android.view.Window;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.xenris.liquidwarsos.server.NetInfo;
import com.xenris.liquidwarsos.R;

public class MainMenuActivity extends AppCompatActivity {
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
