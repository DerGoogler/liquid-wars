//    This file is part of Liquid Wars.
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

import java.lang.Thread;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Client extends Thread {
    private ClientCallbacks clientCallbacks;
    private String ip;
    private int port;
    private Socket socket;
    private int id = -1;
    private boolean sendLocked = false;
    private boolean destroyCalled;

    public Client(ClientCallbacks clientCallbacks, String ip, int port) {
        this.clientCallbacks = clientCallbacks;
        this.ip = ip;
        this.port = port;
        this.start();
    }

    @Override
    public void run() {
        destroyCalled = false;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByName(ip), port), 3000);
            id = socket.getInputStream().read();
            clientCallbacks.onServerConnectionMade(id, ip);
            byte[] buffer = new byte[512];
            int[] intBuffer = new int[128];
            IntBuffer inIB = ByteBuffer.wrap(buffer).asIntBuffer();
            InputStream is = socket.getInputStream();
            while(true) {
                int count = is.read();
                if(count == -1)
                    break;
                is.read(buffer, 0, count);
                inIB.get(intBuffer);
                inIB.rewind();
                if(clientCallbacks != null)
                    clientCallbacks.onServerMessageReceived(count/4, intBuffer);
            }
        } catch(UnknownHostException u) {
            if((clientCallbacks != null) && (!destroyCalled))
                clientCallbacks.onServerConnectionFailed(ip);
            destroy();
            return;
        } catch(IOException e) {
            if((clientCallbacks != null) && (!destroyCalled))
                clientCallbacks.onServerConnectionFailed(ip);
            destroy();
            return;
        }
        if(clientCallbacks != null)
            clientCallbacks.onServerConnectionClosed(ip);
        destroy();
    }

    public void send(int argc, int[] args) {
        while(sendLocked) {
            try { Thread.sleep(10); } catch (InterruptedException ie) { }
        }
        sendLocked = true;
        try {
            ByteBuffer bb = ByteBuffer.allocate(argc*4);
            IntBuffer ib = bb.asIntBuffer();
            ib.put(args, 0, argc);
            OutputStream os = socket.getOutputStream();
            os.write(argc*4);
            os.write(bb.array());
            os.flush();
        } catch(IOException e) { }
        sendLocked = false;
    }

    public void send(int arg1, int arg2) {
        int[] args = {arg1, arg2};
        send(2, args);
    }

    public void destroy() {
        destroyCalled = true;
        if(socket != null) {
            try {
                socket.close();
            } catch(IOException e) { }
            socket = null;
        }
    }

    public void setCallbacks(ClientCallbacks cc) {
        clientCallbacks = cc;
    }

    public interface ClientCallbacks {
        public void onServerMessageReceived(int argc, int[] args);
        public void onServerConnectionMade(int id, String ip);
        public void onServerConnectionFailed(String ip);
        public void onServerConnectionClosed(String ip);
    }
}
