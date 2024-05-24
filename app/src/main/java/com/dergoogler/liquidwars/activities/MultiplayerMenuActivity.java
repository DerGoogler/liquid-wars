

package com.dergoogler.liquidwars.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.EditText;
import android.view.View;
import android.view.Window;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnCancelListener;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.dergoogler.liquidwars.server.NetInfo;
import com.dergoogler.liquidwars.R;
import com.dergoogler.liquidwars.server.ServerFinder;
import com.dergoogler.liquidwars.StaticBits;

import java.util.ArrayList;

public class MultiplayerMenuActivity extends LiquidCompatActivity {
    private Context context;
    private ArrayAdapter serverList;
    private static EditText ipEditText;
    private AlertDialog searchAlertDialog;
    ArrayList<ServerFinder.ServerInfo> serverInfoList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.multiplayer_menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ServerFinder.stopSharing();
    }

    public void connectToGame(View view) {
        serverList = new ArrayAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        ListView listview = new ListView(this);
        listview.setAdapter(serverList);
        listview.setOnItemClickListener((parent, view1, position, id) -> {
            searchAlertDialog.cancel();
            String ip = serverInfoList.get(position).ip;
            String name = serverInfoList.get(position).name;
            Intent intent = new Intent(context, ClientGameSetupActivity.class);
            intent.putExtra("ip", ip);
            intent.putExtra("name", name);
            startActivity(intent);
        });

        ServerFinder.ServerFinderCallbacks sfc = serverInfo -> runOnUiThread(() -> {
            for(ServerFinder.ServerInfo si : serverInfoList) {
                if(si.ip.compareTo(serverInfo.ip) == 0) {
                    if(si.name.compareTo(serverInfo.name) != 0) {
                        int index = serverInfoList.indexOf(si);
                        serverInfoList.add(index, serverInfo);
                        serverInfoList.remove(si);
                        Object s = serverList.getItem(index);
                        serverList.remove(s);
                        serverList.insert(serverInfo.name, index);
                    }
                    return;
                }
            }
            serverInfoList.add(serverInfo);
            serverList.add(serverInfo.name);
        });

        OnClickListener clicker = (dialog, which) -> {
            ServerFinder.stopSearching();
            if(which == DialogInterface.BUTTON_POSITIVE) {
                OnClickListener clicker1 = (dialog1, which1) -> {
                    String ip = ipEditText.getText().toString();
                    String name = ip;
                    Intent intent = new Intent(context, ClientGameSetupActivity.class);
                    intent.putExtra("ip", ip);
                    intent.putExtra("name", name);
                    startActivity(intent);
                };
                EditText tempEditText = ipEditText;
                ipEditText = new EditText(context);
                if(tempEditText != null)
                    ipEditText.setText(tempEditText.getText());
                String ip = NetInfo.getIPAddress(context);
                ipEditText.setHint("e.g. " + ip);
                ipEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                new AlertDialog.Builder(context)
                    .setTitle("Enter IP Address")
                    .setPositiveButton("Connect", clicker1)
                    .setNegativeButton("Cancel", null)
                    .setView(ipEditText)
                    .show();
            }
        };
        OnCancelListener cancelListener = dialog -> ServerFinder.stopSearching();

        serverInfoList = new ArrayList<>();
        String broadcastAddress = NetInfo.getBroadcastAddress(context);
        ServerFinder.search(sfc, broadcastAddress, StaticBits.PORT_NUMBER+1);
        String ssid = NetInfo.getSSID(this);

        searchAlertDialog = new AlertDialog.Builder(this)
            .setTitle("Searching on " + ssid + "...")
            .setPositiveButton("Manual Connect", clicker)
            .setNegativeButton("Cancel", clicker)
            .setOnCancelListener(cancelListener)
            .setView(listview)
            .show();
    }

    public void startNewGame(View view) {
        Intent intent = new Intent(this, MultiplayerGameSetupActivity.class);
        startActivity(intent);
    }
}
