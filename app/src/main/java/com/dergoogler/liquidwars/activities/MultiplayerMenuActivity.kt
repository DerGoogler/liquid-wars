package com.dergoogler.liquidwars.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import com.dergoogler.liquidwars.R
import com.dergoogler.liquidwars.StaticBits
import com.dergoogler.liquidwars.server.NetInfo
import com.dergoogler.liquidwars.server.ServerFinder
import com.dergoogler.liquidwars.server.ServerFinder.ServerFinderCallbacks
import com.dergoogler.liquidwars.server.ServerFinder.ServerInfo

class MultiplayerMenuActivity : LiquidCompatActivity() {
    private var context: Context? = null
    private var serverList: ArrayAdapter<*>? = null
    private var searchAlertDialog: AlertDialog? = null
    var serverInfoList: ArrayList<ServerInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.multiplayer_menu)
        setAdsBanner(R.id.multiplayer_ads_banner)
    }

    override fun onDestroy() {
        super.onDestroy()
        ServerFinder.stopSharing()
    }

    fun connectToGame(view: View?) {
        serverList =
            ArrayAdapter<Any?>(this, android.R.layout.simple_list_item_1,
                ArrayList<String?>() as List<Any?>
            )
        val listview = ListView(this)
        listview.adapter = serverList
        listview.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>?, view1: View?, position: Int, id: Long ->
                searchAlertDialog!!.cancel()
                val ip = serverInfoList!![position].ip
                val name = serverInfoList!![position].name
                val intent = Intent(context, ClientGameSetupActivity::class.java)
                intent.putExtra("ip", ip)
                intent.putExtra("name", name)
                startActivity(intent)
            }

        val sfc = ServerFinderCallbacks { serverInfo: ServerInfo ->
            runOnUiThread {
                for (si in serverInfoList!!) {
                    if (si.ip.compareTo(serverInfo.ip) == 0) {
                        if (si.name.compareTo(serverInfo.name) != 0) {
                            val index = serverInfoList!!.indexOf(si)
                            serverInfoList!!.add(index, serverInfo)
                            serverInfoList!!.remove(si)
                            val s = serverList?.getItem(index)
                            serverList!!.remove(s as Nothing?)
                            serverList!!.insert(serverInfo.name as Nothing?, index)
                        }
                        return@runOnUiThread
                    }
                }
                serverInfoList!!.add(serverInfo)
                serverList!!.add(serverInfo.name as Nothing?)
            }
        }

        val clicker = DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
            ServerFinder.stopSearching()
            if (which == DialogInterface.BUTTON_POSITIVE) {
                val clicker1 =
                    DialogInterface.OnClickListener { dialog1: DialogInterface?, which1: Int ->
                        val ip = ipEditText!!.text.toString()
                        val name = ip
                        val intent = Intent(context, ClientGameSetupActivity::class.java)
                        intent.putExtra("ip", ip)
                        intent.putExtra("name", name)
                        startActivity(intent)
                    }
                val tempEditText = ipEditText
                ipEditText = EditText(context)
                if (tempEditText != null) ipEditText!!.setText(tempEditText.text)
                val ip = NetInfo.getIPAddress(context)
                ipEditText!!.hint = "e.g. $ip"
                ipEditText!!.inputType = InputType.TYPE_CLASS_PHONE
                AlertDialog.Builder(context)
                    .setTitle("Enter IP Address")
                    .setPositiveButton("Connect", clicker1)
                    .setNegativeButton("Cancel", null)
                    .setView(ipEditText)
                    .show()
            }
        }
        val cancelListener =
            DialogInterface.OnCancelListener { dialog: DialogInterface? -> ServerFinder.stopSearching() }

        serverInfoList = ArrayList()
        val broadcastAddress = NetInfo.getBroadcastAddress(context)
        ServerFinder.search(sfc, broadcastAddress, StaticBits.PORT_NUMBER + 1)
        val ssid = NetInfo.getSSID(this)

        searchAlertDialog = AlertDialog.Builder(this)
            .setTitle("Searching on $ssid...")
            .setPositiveButton("Manual Connect", clicker)
            .setNegativeButton("Cancel", clicker)
            .setOnCancelListener(cancelListener)
            .setView(listview)
            .show()
    }

    fun startNewGame(view: View?) {
        val intent = Intent(this, MultiplayerGameSetupActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private var ipEditText: EditText? = null
    }
}
