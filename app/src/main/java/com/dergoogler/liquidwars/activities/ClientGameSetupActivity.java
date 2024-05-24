

package com.dergoogler.liquidwars.activities;

import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.dergoogler.liquidwars.Client;
import com.dergoogler.liquidwars.R;
import com.dergoogler.liquidwars.StaticBits;
import com.dergoogler.liquidwars.Util;

import java.io.InputStream;
import java.io.IOException;

public class ClientGameSetupActivity extends LiquidCompatActivity implements Client.ClientCallbacks {
    private EditText nameEditText;
    private Context context;
    private String serverIP;
    private String serverName;
    private int retries;
    private int myID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        StaticBits.multiplayerGameSetupActivity = null;
        StaticBits.clientGameSetupActivity = this;
        retries = 4;
        setContentView(R.layout.client_game_setup);
        StaticBits.init();
        refreshMapImage();

        Bundle extras = getIntent().getExtras();
        serverIP = extras.getString("ip");
        serverName = extras.getString("name");
        setTextView(R.id.client_game_textview, "Connecting to " + serverName + "...");
        StaticBits.client = new Client(this, serverIP, StaticBits.PORT_NUMBER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StaticBits.client.destroy();
    }

    private void refreshMapImage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                try {
                    if(StaticBits.map == -1)
                        is = getAssets().open("maps/random-map.png");
                    else
                        is = getAssets().open("maps/" + StaticBits.map +"-image.png");
                } catch(IOException e) {
                    try {
                        is = getAssets().open("maps/" + StaticBits.map +"-map.png");
                    } catch(IOException ex) { }
                }
                Drawable d = Drawable.createFromStream(is, null);
                ImageView iv = (ImageView)findViewById(R.id.map_imageview);
                iv.setImageDrawable(d);
            }
        });
    }

    @Override
    public void onServerMessageReceived(int argc, int[] args) {
        if(args[0] == StaticBits.UPDATE_SERVER_NAME) {
            serverName = new String(args, 1, argc-1);
            setTextView(R.id.client_game_textview, "Connected to " + serverName);
        } else if(args[0] == StaticBits.SET_TEAM) {
            setTextView(R.id.team_textview, Util.teamToNameString(args[1]));
            StaticBits.team = args[1];
        } else if(args[0] == StaticBits.SET_TIME_LIMIT) {
            setTextView(R.id.timeout_textview, Util.getTimeoutString(this, args[1]));
            StaticBits.timeLimit = Util.intToTime(args[1]);
        } else if(args[0] == StaticBits.SET_MAP) {
            setTextView(R.id.map_textview, Util.getMapName(this, args[1]));
            StaticBits.map = args[1] - 1;
            refreshMapImage();
        } else if(args[0] == StaticBits.START_GAME) {
            StaticBits.seed = args[1];
            StaticBits.map = args[2];
            StaticBits.dotsPerTeam = args[3];
            StaticBits.client.setCallbacks(null);
            startClientGame();
        } else if(args[0] == StaticBits.SEND_VERSION_CODE) {
            if(args[1] != StaticBits.VERSION_CODE)
                checkVersionCompatibility(args[1]);
        } else if(args[0] == StaticBits.SET_TEAM_SIZE) {
            StaticBits.dotsPerTeam = args[1];
            setTextView(R.id.teamsize_textview, args[1] + "");
        }
    }

    @Override
    public void onServerConnectionMade(int id, String ip) {
        myID = id;
        setTextView(R.id.client_game_textview, "Connected to " + serverName + "");
        StaticBits.client.send(StaticBits.SEND_VERSION_CODE, StaticBits.VERSION_CODE);
    }

    @Override
    public void onServerConnectionFailed(String ip) {
        myID = 0;
        toast("Failed to connect to " + serverName, Toast.LENGTH_SHORT);
        finish();
    }

    @Override
    public void onServerConnectionClosed(String ip) {
        myID = 0;
        toast("Lost connection with " + serverName, Toast.LENGTH_SHORT);
        finish();
    }

    private void checkVersionCompatibility(int v) {
        if(StaticBits.VERSION_CODE < v) {
            toast("Liquid Wars needs updating.", Toast.LENGTH_LONG);
            finish();
        } else if(StaticBits.VERSION_CODE > v) {
            toast("Server needs updating.", Toast.LENGTH_LONG);
            finish();
        }
    }

    private void startClientGame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, GameClientActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setTextView(final int id, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)findViewById(id);
                tv.setText(message);
            }
        });
    }

    private void toast(final String message, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, length).show();
            }
        });
    }
}
