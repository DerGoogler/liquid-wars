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

package com.xenris.liquidwarsos.server;

import java.lang.Thread;
import java.lang.Runnable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.io.IOException;
import android.content.Context;

public class Server implements SubServer.SubServerCallbacks, Runnable {
    public ServerCallbacks serverCallbacks;
    private int port;
    private ServerSocket serverSocket;
    private ArrayList<SubServer> subServers;
    private Context context;
    private boolean accepting = false;
    private static int MAX_CLIENTS = 5;
    public static int SET_MAP_IMAGE_COMMAND = 0x56;
    public static int TEAM_LIST_COMMAND = 0x57;
    public static int REQUEST_CHANGE_TEAM_COMMAND = 0x58;
    public static int START_GAME_COMMAND = 0x59;

    public Server(Context context, int port) {
        this.context = context;
        this.serverCallbacks = (ServerCallbacks)context;
        this.port = port;
        subServers = new ArrayList<SubServer>();
    }

    public void startAccepting() {
        if(!accepting)
            new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            int id = 1;
            accepting = true;
            while(accepting) {
                Socket socket = serverSocket.accept();
                if(subServers.size() < MAX_CLIENTS) {
                    SubServer subServer = new SubServer(this, socket, id);
                    subServer.start();
                    subServers.add(subServer);
                    if(serverCallbacks != null)
                        serverCallbacks.onClientConnected(id);
                    id++;
                } else {
                    socket.close();
                }
            }
        } catch(UnknownHostException u) { } catch(IOException e) { }
        serverSocket = null;
        accepting = false;
    }

    @Override
    public void onSubServerMessageReceived(int id, int argc, int[] args) {
        if(serverCallbacks != null)
            serverCallbacks.onClientMessageReceived(id, argc, args);
    }

    @Override
    public void onSubServerDisconnect(int id) {
        synchronized(subServers) {
            for(SubServer subServer : subServers) {
                if(subServer.id == id) {
                    subServers.remove(subServer);
                    break;
                }
            }
        }
        if(serverCallbacks != null)
            serverCallbacks.onClientDisconnected(id);
    }

    public void stopAccepting() {
        accepting = false;
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch(IOException e) { }
        }
    }

    public void sendToOne(int id, int argc, int[] args) {
        for(SubServer subServer : subServers) {
            if(subServer.id == id) {
                subServer.send(argc, args);
                break;
            }
        }
    }

    public void sendToOne(int id, int arg1, int arg2) {
        int[] args = {arg1, arg2};
        sendToOne(id, 2, args);
    }

    public void sendToAll(int argc, int[] args) {
        for(SubServer subServer : subServers)
            subServer.send(argc, args);
    }

    public void sendToAll(int arg1, int arg2) {
        int[] args = {arg1, arg2};
        sendToAll(2, args);
    }

    public void sendToAll(int arg1, int arg2, int arg3) {
        int[] args = {arg1, arg2, arg3};
        sendToAll(3, args);
    }

    public void destroy() {
        stopAccepting();
        synchronized(subServers) {
            for(SubServer s : subServers)
                s.destroy();
        }
    }

    public void setCallbacks(ServerCallbacks sc) {
        serverCallbacks = sc;
    }

    public interface ServerCallbacks {
        public void onClientMessageReceived(int id, int argc, int[] args);
        public void onClientConnected(int id);
        public void onClientDisconnected(int id);
    }
}
