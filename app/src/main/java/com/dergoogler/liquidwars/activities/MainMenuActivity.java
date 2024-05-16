

package com.dergoogler.liquidwars.activities;

import static android.Manifest.permission_group.LOCATION;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import android.view.View;
import android.view.Window;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;

import com.dergoogler.liquidwars.server.NetInfo;
import com.dergoogler.liquidwars.R;

public class MainMenuActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_menu);
    }

    public void singlePlayerMenu(View view) {
        Intent intent = new Intent(this, SinglePlayerGameSetupActivity.class);
        startActivity(intent);
    }

    public void multiplayerMenu(View view) {
        String ip = NetInfo.getIPAddress(this);
        if (ip.compareTo("0.0.0.0") == 0) {
            Toast.makeText(this, "Need Wi-Fi connection for multiplayer game.", Toast.LENGTH_SHORT).show();
        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            } else {
                Intent intent = new Intent(this, MultiplayerMenuActivity.class);
                startActivity(intent);
            }
        }
    }

    public void instructions(View view) {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }
}
