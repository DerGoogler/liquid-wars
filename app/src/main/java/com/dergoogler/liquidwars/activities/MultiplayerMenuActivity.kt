package com.dergoogler.liquidwars.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.activities.ClientGameSetupActivity
import com.dergoogler.liquidwars.activities.MultiplayerGameSetupActivity
import com.dergoogler.liquidwars.server.NetInfo
import com.dergoogler.liquidwars.server.ServerFinder
import com.dergoogler.liquidwars.server.ServerFinder.ServerFinderCallbacks
import com.dergoogler.liquidwars.server.ServerFinder.ServerInfo
import kotlinx.coroutines.launch

class MultiplayerMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiplayerMenuScreenContent(context = this)
        }
    }
}

@Composable
fun MultiplayerMenuScreenContent(context: Context) {
    var serverInfoList by remember { mutableStateOf(listOf<ServerInfo>()) }
    var isSearching by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var ipAddress by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = {
                coroutineScope.launch {
                    isSearching = true
                    val broadcastAddress = NetInfo.getBroadcastAddress(context)
                    ServerFinder.search(
                        ServerFinderCallbacks { serverInfo ->
                            if (serverInfo != null) {
                                serverInfoList = serverInfoList.filter { it.ip != serverInfo.ip } + serverInfo
                            }
                        },
                        broadcastAddress,
                        StaticBits.PORT_NUMBER + 1
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search for Servers")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(serverInfoList.size) { index ->
                val serverInfo = serverInfoList[index]
                ServerItem(serverInfo) {
                    val intent = Intent(context, ClientGameSetupActivity::class.java)
                    intent.putExtra("ip", serverInfo.ip)
                    intent.putExtra("name", serverInfo.name)
                    context.startActivity(intent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, MultiplayerGameSetupActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start New Game")
        }

        if (isSearching) {
            ManualConnectDialog(
                onDismiss = { isSearching = false },
                onConnect = {
                    val intent = Intent(context, ClientGameSetupActivity::class.java)
                    intent.putExtra("ip", ipAddress)
                    intent.putExtra("name", ipAddress)
                    context.startActivity(intent)
                },
                ipAddress = ipAddress,
                onIpAddressChange = { ipAddress = it }
            )
        }
    }
}

@Composable
fun ServerItem(serverInfo: ServerInfo, onConnect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = serverInfo.name, style = MaterialTheme.typography.labelSmall)
            Button(onClick = onConnect) {
                Text("Connect")
            }
        }
    }
}

@Composable
fun ManualConnectDialog(
    onDismiss: () -> Unit,
    onConnect: () -> Unit,
    ipAddress: String,
    onIpAddressChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter IP Address") },
        text = {
            Column {
                TextField(
                    value = ipAddress,
                    onValueChange = onIpAddressChange,
                    label = { Text("IP Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = onConnect) {
                Text("Connect")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
