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
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnCancelListener;
import java.io.InputStream;
import java.io.IOException;

public class MultiplayerGameSetupActivity extends Activity implements OnItemSelectedListener, OnLongClickListener, Server.ServerCallbacks {
    private Spinner teamSpinner;
    private Spinner mapSpinner;
    private Spinner timeoutSpinner;
    private Spinner teamSizeSpinner;
    private EditText nameEditText;
    private Context context;
    private TextView nametv;
    private int myID = 0;
    private int numberOfClients = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        StaticBits.multiplayerGameSetupActivity = this;
        StaticBits.clientGameSetupActivity = null;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.multi_game_setup);
        StaticBits.init();
        refreshMapImage();
        initSpinners();
        initButtons();
        ServerFinder.share(context, StaticBits.PORT_NUMBER+1, StaticBits.publicName);
        StaticBits.server = new Server(this, StaticBits.PORT_NUMBER);
        updateMessageTextView();
        nametv = (TextView)findViewById(R.id.public_name_textview);
        nametv.setText(StaticBits.publicName);
        TextView tv = (TextView)findViewById(R.id.team_textview);
        tv.setText(Util.teamToNameString(0));
    }

    @Override
    public void onResume() {
        super.onResume();
        StaticBits.newSeed();
        if(StaticBits.gameWasDisconnected) {
            if(StaticBits.client != null)
                StaticBits.client.destroy();
            StaticBits.client = null;
            myID = 0;
            for(int i = 1; i < 6; i++)
                StaticBits.teams[i] = StaticBits.AI_PLAYER;
            StaticBits.teams[0] = myID;
            StaticBits.gameWasDisconnected = false;
        }
        if(StaticBits.server != null) {
            ServerFinder.share(context, StaticBits.PORT_NUMBER+1, StaticBits.publicName);
            StaticBits.server.startAccepting();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StaticBits.server.destroy();
        ServerFinder.stopSharing();
    }

    public void changePublicName(View view) {
        OnClickListener clicker = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = nameEditText.getText().toString();
                    if((name.length() > 0) && (StaticBits.publicName.compareTo(name) != 0)) {
                        StaticBits.publicName = name;
                        ServerFinder.stopSharing();
                        ServerFinder.share(context, StaticBits.PORT_NUMBER+1, StaticBits.publicName);
                        byte[] nameBytes = name.getBytes();
                        int[] data = new int[1 + nameBytes.length];
                        data[0] = StaticBits.UPDATE_SERVER_NAME;
                        for(int i = 1; i < data.length; i++)
                            data[i] = nameBytes[i-1];
                        StaticBits.server.sendToAll(data.length, data);
                        nametv.setText(StaticBits.publicName);
                    }
                }
            };
        nameEditText = new EditText(this);
        nameEditText.setText(nametv.getText());
        new AlertDialog.Builder(this)
            .setTitle("Enter a name to identify your game:")
            .setPositiveButton("Done", clicker)
            .setNegativeButton("Cancel", null)
            .setView(nameEditText)
            .show();
    }

    private void initSpinners() {
        ArrayAdapter<CharSequence> adapter;
        final int simpleSpinnerItem = android.R.layout.simple_spinner_item;

        mapSpinner = (Spinner)findViewById(R.id.map_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.maps_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapSpinner.setAdapter(adapter);
        mapSpinner.setOnItemSelectedListener(this);

        timeoutSpinner = (Spinner)findViewById(R.id.timeout_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.timeout_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeoutSpinner.setAdapter(adapter);
        timeoutSpinner.setOnItemSelectedListener(this);
        timeoutSpinner.setSelection(2);

        teamSizeSpinner = (Spinner)findViewById(R.id.teamsize_spinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.teamsize_array, simpleSpinnerItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSizeSpinner.setAdapter(adapter);
        teamSizeSpinner.setOnItemSelectedListener(this);
        teamSizeSpinner.setSelection(2);
    }

    private void initButtons() {
        Button previousButton = (Button)findViewById(R.id.previous_button);
        previousButton.setOnLongClickListener(this);
        Button nextButton = (Button)findViewById(R.id.next_button);
        nextButton.setOnLongClickListener(this);
    }

    public void start(View view) {
        ServerFinder.stopSharing();
        StaticBits.server.stopAccepting();
        int[] args = {StaticBits.START_GAME, StaticBits.seed, StaticBits.map, StaticBits.dotsPerTeam};
        StaticBits.server.sendToAll(4, args);
        StaticBits.server.setCallbacks(null);
        StaticBits.team = Util.clientIdToPlayerNumber(myID);

        Intent intent = new Intent(this, GameServerActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view) {
        final int id = view.getId();
        if(id == R.id.next_button) {
            int pos = mapSpinner.getSelectedItemPosition();
            pos += 20;
            if(pos > StaticBits.NUMBER_OF_MAPS)
                pos = StaticBits.NUMBER_OF_MAPS;
            mapSpinner.setSelection(pos);
        } else if(id == R.id.previous_button) {
            int pos = mapSpinner.getSelectedItemPosition();
            pos -= 20;
            if(pos < 0)
                pos = 0;
            mapSpinner.setSelection(pos);
        }
        return true;
    }

    public void nextMap(View view) {
        int pos = mapSpinner.getSelectedItemPosition();
        pos++;
        if(pos > StaticBits.NUMBER_OF_MAPS)
            pos = StaticBits.NUMBER_OF_MAPS;
        mapSpinner.setSelection(pos);
    }

    public void previousMap(View view) {
        int pos = mapSpinner.getSelectedItemPosition();
        pos--;
        if(pos < 0)
            pos = 0;
        mapSpinner.setSelection(pos);
    }

    public void changeTeam(View view) {
        //TODO cycle through available teams.
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        final int spinnerId = parent.getId();
        if(spinnerId == R.id.map_spinner) {
            StaticBits.map = pos - 1;
            int m = mapSpinner.getSelectedItemPosition();
            StaticBits.server.sendToAll(StaticBits.SET_MAP, pos);
            refreshMapImage();
        } else if(spinnerId == R.id.timeout_spinner) {
            StaticBits.timeLimit = Util.intToTime(pos);
            int t = timeoutSpinner.getSelectedItemPosition();
            StaticBits.server.sendToAll(StaticBits.SET_TIME_LIMIT, pos);
        } else if(spinnerId == R.id.teamsize_spinner) {
            StaticBits.dotsPerTeam = Integer.parseInt(((TextView)view).getText() + "");
            StaticBits.server.sendToAll(StaticBits.SET_TEAM_SIZE, StaticBits.dotsPerTeam);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    private void refreshMapImage() {
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

    @Override
    public void onClientMessageReceived(int id, int argc, int[] args) {
         if(args[0] == StaticBits.SEND_VERSION_CODE) {
            if(args[1] != StaticBits.VERSION_CODE)
                checkVersionCompatibility(args[1]);
        }
    }

    private void checkVersionCompatibility(int v) {
        if(StaticBits.VERSION_CODE < v)
            toast("Liquid Wars needs updating.", Toast.LENGTH_LONG);
    }

    @Override
    public void onClientConnected(int id) {
        numberOfClients++;
        updateMessageTextView();
        for(int i = 0; i < 6; i++) {
            if(StaticBits.teams[i] == StaticBits.AI_PLAYER) {
                StaticBits.teams[i] = id;
                StaticBits.server.sendToOne(id, StaticBits.SET_TEAM, i);
                StaticBits.server.sendToOne(id, StaticBits.SEND_VERSION_CODE, StaticBits.VERSION_CODE);
                break;
            }
        }
        int t = timeoutSpinner.getSelectedItemPosition();
        StaticBits.server.sendToOne(id, StaticBits.SET_TIME_LIMIT, t);
        int m = mapSpinner.getSelectedItemPosition();
        StaticBits.server.sendToOne(id, StaticBits.SET_MAP, m);
        StaticBits.server.sendToAll(StaticBits.SET_TEAM_SIZE, StaticBits.dotsPerTeam);
    }

    @Override
    public void onClientDisconnected(int id) {
        numberOfClients--;
        updateMessageTextView();
        for(int i = 0; i < 6; i++) {
            if(StaticBits.teams[i] == id) {
                StaticBits.teams[i] = StaticBits.AI_PLAYER;
                break;
            }
        }
    }

    private void updateMessageTextView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView)findViewById(R.id.multi_game_textview);
                String ip = NetInfo.getIPAddress(context);
                String ssid = NetInfo.getSSID(context);
                tv.setText("Sharing game on " + ssid + ". IP Address: " + ip + ". Number of players: " + (numberOfClients+1));
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
