

package com.xenris.liquidwarsos.server;

import android.content.Context;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.lang.Thread;

public class ServerFinder {
    private static DatagramSocket datagramSocket = null;
    private static final String queryMessage = "9287349213846";
    private static final String responseMessage = "2138479287348";
    private static boolean isSearching = false;

    public static void share(final Context context, final int port, final String serverName) {
        if(datagramSocket != null)
            return;
        new Thread() {
            @Override
            public void run() {
                try {
                    datagramSocket = new DatagramSocket(port);
                    byte[] receiveData = new byte[32];
                    byte[] sendData = (responseMessage + serverName).getBytes();
                    String broadcastAddress = NetInfo.getBroadcastAddress(context);
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    while(true) {
                        datagramSocket.receive(receivePacket);
                        String s = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if(s.compareTo(queryMessage) == 0) {
                            InetAddress inIP = receivePacket.getAddress();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, inIP, receivePacket.getPort());
                            datagramSocket.send(sendPacket);
                        }
                    }
                } catch (SocketException s) { } catch (IOException i) { }
            }
        }.start();
    }

    public static void stopSharing() {
        if(datagramSocket != null)
            datagramSocket.close();
        datagramSocket = null;
    }

    public static void search(final ServerFinderCallbacks sfc, final String ip, final int port) {
        if(isSearching)
            return;
        new Thread() {
            @Override
            public void run() {
                isSearching = true;
                try {
                    datagramSocket = new DatagramSocket(port);
                    datagramSocket.setSoTimeout(1000);
                    byte[] receiveData = new byte[128];
                    byte[] sendData = queryMessage.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ip), port);
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    while(isSearching) {
                        datagramSocket.send(sendPacket);
                        while(true) {
                            try {
                                datagramSocket.receive(receivePacket);
                            } catch (IOException i) {
                                break;
                            }
                            byte[] data = receivePacket.getData();
                            int l = responseMessage.getBytes().length;
                            String s = new String(data, 0, l);
                            if(s.compareTo(responseMessage) == 0) {
                                String name = new String(data, l, receivePacket.getLength() - l);
                                String address = receivePacket.getAddress().getHostAddress();
                                sfc.onServerFound(new ServerInfo(name, address));
                            }
                        }
                    }
                } catch (SocketException s) {
                } catch (IOException i) {
                }
                stopSearching();
            }
        }.start();
    }

    public static class ServerInfo {
        public String name;
        public String ip;

        public ServerInfo(String n, String i) {
            name = n;
            ip = i;
        }
    }

    public static void stopSearching() {
        if(datagramSocket != null)
            datagramSocket.close();
        datagramSocket = null;
        isSearching = false;
    }

    public interface ServerFinderCallbacks {
        public void onServerFound(ServerInfo serverInfo);
    }
}
